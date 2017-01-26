package net.paymate.authorizer.npc;

import net.paymate.authorizer.*; // AuthSocketAgent, etc.
import net.paymate.net.*; // IPSpec
import net.paymate.database.*; // PayMateDBDispenser
import net.paymate.util.*; // ErrorLogStream
import net.paymate.util.timer.*; // TimeBomb, etc.
import java.io.*; // socket stream stuff
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*; // packet
import net.paymate.text.Formatter;
import net.paymate.io.ByteFifo;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCSubmittalSocketAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.29 $
 * TODO: We need a way of removing stuff from the batch that doesn't get sent to the authorizer/submitter!!!
 */

// Mutexing is handled in AuthTerminalAgent, so no 2 threads should ever enter here simultaneously.  No worries, mate!

public class NPCSubmittalSocketAgent extends SubmittalSocketAgent implements TimeBomb {

  private static final Tracer dbg = new Tracer(NPCSubmittalSocketAgent.class, ErrorLogStream.WARNING);

  public NPCSubmittalSocketAgent(Packet vb, Authorizer handler) {
    super(vb, handler);
  }

  byte [] JUSTACK = {
    Ascii.ACK,
  };

  // since there is only ONE connection to the authorizer (only one modem), mutex on the authorizer module!
  public boolean sendSubmittal(AuthSubmitTransaction txn) {
    return ((NPCAuth)handler).sendSubmittal(this, txn);
  }

  /* package */ boolean sendMutexedSubmittal(AuthSubmitTransaction txn) {
    handler.connectionAttempts.incr();
    if(txn == null) {
      handler.PANIC("Unable to sendSubmittal()!  Txn is null!");
      return false;
    }
    if((txn.request == null) || !(txn.request instanceof NPCAuthSubmitRequest)) {
      handler.PANIC("Unable to sendSubmittal()!  Request is either null or not an NPCAuthSubmitRequest!");
      return false;
    }
    if((txn.response == null) || !(txn.response instanceof NPCAuthSubmitResponse)) {
      handler.PANIC("Unable to sendSubmittal()!  Response is either null or not an NPCAuthSubmitResponse!");
      return false;
    }
    try {
      handler.println("sendSubmittal starting run method");
      alarmum = Alarmer.New(handler.submitTimeout, (TimeBomb)this);
      Alarmer.Defuse(alarmum); // don't want it to go off yet
      // AuthSubmitRequest has an authid and terminalid in it.  Use the authorizer's db, if needed.
      // run the submission to try to get the actioncode & authrespmsg back
      // if couldn't do the submittal, PANIC !!!
      NPCAuthSubmitRequest  request  = (NPCAuthSubmitRequest) txn.request;
      NPCAuthSubmitResponse response = (NPCAuthSubmitResponse)txn.response;
      response.vb = (VisaBuffer)vb;
      request.records = txn.records;
      boolean ret = false;
      try {
        // 1) Build the pure data detail records [no STX/ETX/LRC, etc.] into one big byte array [cannot exceed 61680 bytes -- stop at 61400 to prevent overflow]:
        //    exactly: <rs>detail0<rs>detail1<rs>detail2<rs>...<rs>detailN
//        ByteFifo byf = new ByteFifo(61400);
        ByteArrayOutputStream os = new ByteArrayOutputStream(61400);
        while(txn.records.next()) {
          os.reset();
          TxnRow row = txn.records; // shorter name for less typing
          handler.println("this row = ["+row+"]");
          // FOR NOW: send all DB and CR that are SA and RE, but not that are VOIDED
          // +++ TODO: EVENTUALLY send the VOIDs, as well!!!
          dbg.WARNING("record["+row.txnid+"]: isReversal="+row.isReversal()+
                      ", wasVoided="+row.isVoided()+", wasAuthApproved="+
                      row.wasAuthApproved()+", isQuery="+row.isQuery()+
                      ", responded="+row.responded()+", settle="+row.settle());
          if(row.isReversal() || row.isVoided() || !row.wasAuthApproved() ||
             !row.responded() || row.isQuery() || !row.settle()) {
            continue; // skip it
          }
          if(!row.isCredit()) {
            handler.PANIC("SUBMIT,notSupported,txn="+row.txnid+",type="+row.paytype);
            continue; // +++ needs to be removed from the batch !!! ???
          }
          txn.request.records = row;
          byte [] detail = request.detail();
          if(detail != null) {
//            OutputStream os = byf.getOutputStream();
            os.write(detail);
            handler.println("ADDED ONE ["+row.txnid+"]: "+Ascii.bracket(detail));
            byte [] byfbytes = os.toByteArray();
          } else {
            handler.PANIC("SUBMIT,detailNotGenerated,txn="+row.txnid);
          }
        }
        // stamp the database with the totals from the request object
        // +++ which needs to be the totals per what we see on the screen, not per what we sent to the auth
        // +++ Need a separate table/fields for what we sent to the authorizer and what we show the customers
        PayMateDBDispenser.getPayMateDB().setBatchTotals(request.batchid(), request.postxncount()+request.negtxncount(), request.postxntotal()+request.negtxntotal());
        // 2) Prepend the header and append the footer
        //    header<rs>DETAILS<us>
        ByteArrayOutputStream os2 = new ByteArrayOutputStream(61400);
//        OutputStream os = all.getOutputStream();
byte [] tell = request.header();
dbg.ERROR("header="+Ascii.bracket(tell));
        os2.write(tell);
tell = os2.toByteArray();
dbg.ERROR("details="+Ascii.bracket(tell));
        os2.write(tell);
tell = request.trailer();
dbg.ERROR("trailer="+Ascii.bracket(tell));
        os2.write(tell);
tell = os2.toByteArray();
        handler.println("Complete batch ready to send: " + Ascii.bracket(tell));
        // 3) Slice them up into 250 byte blocks, wrapping them: <stx>BLOCK<etx><lrc>.
        byte [][] byters = null;
        int max = 0;
        { // to release ram when done
          ByteArrayFIFO bafifo = new ByteArrayFIFO();
          bafifo.putSplit(tell, request.maxRequestSize());
          byters = bafifo.getArray();
          max = byters.length;
          // this just for debug
          for(int i = 0; i < max; i++) {
            handler.println(Formatter.ratioText("Ready to send packet[",i,max)+":"+Ascii.bracket(byters[i]));
          }
        }
        // it is now split into N pieces!
        // get the socket and streams prepped
        if(!request.justSimulate) {
          getSettleSocket(txn); // throws on error
//          /* check ret val??? */ sendSubmittalPiece("wait for ENQ", new byte[0], response);
        }
        dbg.mark("Detail");
        // skip through the records, sending each detail
        // +++ redo without using max in the loop +++
        for(int i = 0; i < max; i++) {
          boolean last = (i == (max - 1));
          VisaBuffer vb = VisaBuffer.NewSender(request.maxRequestSize()+4);//+_+ why 4?
          vb.append(byters[i]);
          vb.end();
          byte [] block = vb.packet();
          String posname = Formatter.ratioText("Detail# ",(i+1),max);
          dbg.mark(posname); // for each one, set its record number, maybe off of txnid
          // Check its reply +++ --- might be wrong
          if(request.justSimulate) {
            simulate(posname, block, response, !(i+1 == max));
          } else if(!sendSubmittalPiece(posname, block, response)) {
            vb = (VisaBuffer)response.vb;
            if(vb.isComplete() && vb.wasAckNaked() && !vb.isOk()) { // was NAKed, try again
              i--;
              continue;
            } else {
              return ret = false;
            }
          }
          if(!response.wasGood()) {
            break;
          } else {
            if(last && vb.isComplete() && vb.wasAckNaked() && vb.isOk()) {
              // last packet should not get an ACK, but a full message
              return ret = false;
            }
          }
        }
        // now, send a final ACK and hang up
        String posname = "FinalACK";
        NPCAuthSubmitResponse hackresponse = new NPCAuthSubmitResponse();
        hackresponse.vb = VisaBuffer.NewReceiver(1);
        if(request.justSimulate) {
          simulate(posname, JUSTACK, hackresponse, true);
        } else {
          sendSubmittalPiece(posname, JUSTACK, hackresponse);
        }
        dbg.mark("done");
        handler.connections.incr();
        if(response.wasGood() && response.wasAckNaked) { // should not be acked/naked at this level
          response.markFailed("ACKed but not approved");
        }
        return ret = response.wasGood() && !response.wasAckNaked;
      } catch(Exception exception2) {
        handler.println("NPCSubmittalSocketAgent had possible error ["+dbg.location+"]: " + exception2);
        handler.println("In case it was in a read, the following had been received thus far: " + Ascii.bracket(response.packetImage()));
        dbg.Caught(exception2);
        response.markFailed("Exception during transmission");
        return ret = false;
      } finally {
        if(!shouldDie) {
  //          handler.reads.add(reply.length);
          handler.println("NPCSubmittalSocketAgent response = " + response);
  //          authTransaction.response.finish(vb);
        }
        handler.println("NPCSubmittalSocketAgent is closing.");
        kill();
        if(!ret) {
          handler.PANIC("Submittal [" + txn.request.batchid() + "] failed!", "Txn=["+txn+"]");
          handler.settleIps.thisFailed(txn.host); // +_+ should this be here?
        }
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      handler.PANIC("Exception attempting to sendSubmittal()! "+t, "Txn=["+txn+"]");
      return false;
    }
  }


  private Alarmum alarmum = null;

  private final boolean sendSubmittalPiece(String piecename, byte [] toSend, NPCAuthSubmitResponse response) throws IOException {
    handler.println("transmitting "+piecename+": ["+Ascii.image(toSend)+"]");
    response.vb.reset();
    /*sent =*/ writeBytes(toSend);
    // only timeout the read in this class, not the write, too
    boolean ret = false;
    Alarmer.reset(handler.submitTimeout, alarmum);  // setup an alarmer to kill me if I don't come back within TIMEOUT seconds!
    try {
      ret = readBytes(response.vb);
    } catch (IOException e) {
      throw e;
    } catch (Exception ex) {
      dbg.Caught(ex);
      ret=false;
    } finally {
      Alarmer.Defuse(alarmum); // kill the alarmer (in case it didn't go off)
    }
    response.reset();
    if(ret) {
      response.process(vb);
    }
    handler.println("received: "+response.packetImage() + " - " + response);
    return ret;
  }

  private final boolean simulate(String piecename, byte [] toSend, NPCAuthSubmitResponse response, boolean justAck) {
    response.vb.reset();
    if(justAck) {
      response.vb.append(JUSTACK);
    } else {
      response.simulateApproval();
    }
    response.process(response.vb);
    handler.println("Complete packet ready to send ["+piecename+"]: " + Ascii.bracket(toSend));
    handler.println("Simulated response: " + response.packetImage() + " =\n" + response);
    return true;
  }

  // called by the alarmum when read+write times out (just do on read? +++)
  public void onTimeout() {
    handler.println("onTimeout(): about to kill the socket!");
    kill();
  }
}
