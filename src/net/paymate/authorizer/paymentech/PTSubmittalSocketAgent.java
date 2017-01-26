package net.paymate.authorizer.paymentech;

import net.paymate.authorizer.*; // AuthSocketAgent, etc.
import net.paymate.net.*; // IPSpec
import net.paymate.database.*; // PayMateDBDispense
import net.paymate.util.*; // ErrorLogStream
import net.paymate.util.timer.*; // TimeBomb, etc.
import java.io.*; // socket stream stuff
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*; // packet
import net.paymate.terminalClient.PosSocket.paymentech.PTTransactionCodes;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PTSubmittalSocketAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.34 $
 */

// This class is currently just PT, but may be base-d into Authorizer to handle differences between PT and CS!
// Single thread coming in on sendSubmittal().
// Mutexing is handled in AuthTerminalAgent, so no 2 threads should ever enter here simultaneously.  No worries, mate!

public class PTSubmittalSocketAgent extends SubmittalSocketAgent implements TimeBomb {

  private static final Tracer dbg = new Tracer(PTSubmittalSocketAgent.class, ErrorLogStream.VERBOSE);

  boolean hconly = false;

  public PTSubmittalSocketAgent(Packet vb, PaymentechAuth handler, boolean hostCaptureOnly) {
    super(vb, handler);
    hconly = hostCaptureOnly;
  }

  public boolean sendSubmittal(AuthSubmitTransaction txn) {
    if(txn == null) {
      handler.PANIC("Unable to sendSubmittal()!  Txn is null!");
      return false;
    }
    if((txn.request == null) || !(txn.request instanceof PTAuthSubmitRequest)) {
      handler.PANIC("Unable to sendSubmittal()!  Request is either null or not an PTAuthSubmitRequest!");
      return false;
    }
    if((txn.response == null) || !(txn.response instanceof PTAuthSubmitResponse)) {
      handler.PANIC("Unable to sendSubmittal()!  Response is either null or not an PTAuthSubmitResponse!");
      return false;
    }
    try {
      handler.println("sendSubmittal starting run method");
      alarmum = Alarmer.New(handler.submitTimeout, (TimeBomb)this);
      Alarmer.Defuse(alarmum); // don't want it to go off yet
      // AuthSubmitRequest has an authid and terminalid in it.  Use the authorizer's db, if needed.
      // run the submission to try to get the actioncode & authrespmsg back
      // if couldn't do the submittal, PANIC !!!
      PTAuthSubmitRequest  request  = (PTAuthSubmitRequest) txn.request;
      PTAuthSubmitResponse response = (PTAuthSubmitResponse)txn.response;
      response.vb = (VisaBuffer)vb;
      request.records = txn.records;
      boolean ret = false;
      try {
        byte [] piece = null;
        // first, make all of the vb's and calculate the totals
        ByteArrayFIFO barf = new ByteArrayFIFO();
        byte [] detail = {};
        boolean next = txn.records.next();
        if(next) {
          do {
            txn.request.records = txn.records.copy(); // make a copy so we can test the next one
            TxnRow row = txn.request.records; // then make its name shorter so we don't havbe to type so much here
            next = txn.records.next();
handler.println("next = ["+row+"]");
            // FOR NOW ...
            // must determine if we WANT this item!
            // SV: should not send VOIDEDs or VOIDS.
            // DB: send all SA and RE, but there shouldn't be any voids or voideds
            // CR: send all SA and RE, but nothing VOID or VOIDED
            // QR: do not send queries
            // SUMMARY: all DB, CR, and SV that are SA and RE, but not that are VOIDED
            dbg.ERROR("record["+row.txnid+
                      "]: isReversal="+row.isReversal()+
                      ", wasVoided="+row.isVoided()+
                      ", wasAuthApproved="+row.wasAuthApproved()+
                      ", isQuery="+row.isQuery()+
                      ", responded="+row.responded()+
                      ", settleAmountIsZero="+row.rawSettleAmount().isZero()+
                      ", isDebit="+row.isDebit()+
                      ", settle="+row.settle());
            if(row.isReversal() ||
               row.isVoided() ||
               !row.wasAuthApproved() ||
               !row.responded() ||
               row.isQuery() ||
               row.rawSettleAmount().isZero() ||
               !row.settle()) {
              continue; // skip it
            }
            detail = request.detail(!next);
            // MAX for a detail seems to be MAX(CR=158B, DB=229B, SV=93B) = 229B (includes STX/ETX)
            // since a packet can be up to 4096, and a header is 6
            // and since MAX=229 includes the STX/ETX, which will come out of the remaining 4090
            // we can put about 4088 / 227 records in a packet = 18 records (however, we need to leave off the STX/ETX except for the front and back records!)
            // +++ @@@ DO THAT !!!                                   ^^^
            barf.put(detail);
handler.println("ADDED ONE ["+row.txnid+"]: ["+new String(detail)+"]");
          } while(next);
        } else {
          return false; // can't do this if there aren't any records!
        }
        // stamp the database with the totals from the request object
        PayMateDBDispenser.getPayMateDB().setBatchTotals(request.batchid(), request.txncount(), request.txntotal()); /// ++++ @@@ move to PaymentechAuth or something - put where we stamp approval ???
        if( ! hconly) {
          byte [] resplitter = barf.getCatAll();
          barf.putSplit(resplitter, handler.bufferSize-PaymentechConstants.HEADERLENGTH-2 /* 2 for STX+ETX*/);
          // get the socket and streams prepped
          getSettleSocket(txn); // throws on error
          // Beverly said that if we are using TCS, we can't do balance inquiries.  :(
          // Build the request object for the first header & transmit it
          dbg.mark("Header1");
          // Check the reply to see if we can continue (etc.)
          if(sendSubmittalPiece("header1", request.header(PTTransactionCodes.BatchRelease), response)) {
            if(response.wasGood(PTAuthSubmitResponse.WASHEADER)) { // }}
              // Build the request object for the second header & Transmit it
              dbg.mark("Header2");
              // Check its reply
              if(sendSubmittalPiece("header2", request.header(PTTransactionCodes.BatchUploadHeader), response)) {
                if(response.wasGood(PTAuthSubmitResponse.WASHEADER)) { // }}
                  // Send the detail
                  dbg.mark("Detail");
                  // skip through the records, sending each detail
                  int i = 0;
                  PaymentechAuth pta = (PaymentechAuth)handler;
                  while( (piece = barf.nextByteArray()) != null) {
                    String posname = "Detail# " + (++i);
                    dbg.mark(posname); // for each one, set its record number, maybe off of txnid
                    // Check its reply +++ --- might be wrong
                    if(!sendSubmittalPiece(posname, pta.addHeaderAndSTXETX(piece), response)) {
                      handler.println("PTSubmittalSocketAgent Detail# "+i+" is NOT okay.");
                      return ret = false;
                    }
                  }
                  // Send the trailer
                  dbg.mark("Trailer");
                  // check its return value
                  response.vb = (VisaBuffer) vb;
                  if(sendSubmittalPiece("trailer", request.trailer(), response)) {
                    ret = response.wasGood(PTAuthSubmitResponse.WASNOTHEADER);
                    if(!ret) {
                      handler.println("PTSubmittalSocketAgent trailer is okay, but response was not good.");
                    }
                  } else {
                    handler.println("PTSubmittalSocketAgent trailer is NOT okay.");
                  }
                  dbg.mark("done");
                } else {
                  handler.println("PTSubmittalSocketAgent header2 is okay, but response was not good.");
                }
              } else {
                handler.println("PTSubmittalSocketAgent header2 is NOT okay.");
              }
            } else {
              handler.println("PTSubmittalSocketAgent header1 is okay, but response was not good.");
            }
          } else {
            handler.println("PTSubmittalSocketAgent header1 is NOT okay.");
          }
          return ret;
        } else {// host capture means gateway, so we will internally approve this
          response.markApproved("internal");
          return ret = true;
        }
      } catch(Exception exception2) {
        handler.println("PTSubmittalSocketAgent had possible error ["+dbg.location+"]: " + exception2);
        dbg.Caught(exception2);
        String msg = "";
        if(handler instanceof net.paymate.authorizer.paymentech.netconnect.NetConnectPTAuth) {
          msg = "Exception during transmission: " + exception2.getMessage();
        } else {
          msg = "Exception during transmission";
        }
        response.markFailed(msg);
        return ret = false;
      } finally {
        if(!shouldDie) {
//          handler.reads.add(reply.length);
          handler.println("PTSubmittalSocketAgent response = " + response);
//          authTransaction.response.finish(vb);
        }
        handler.println("PTSubmittalSocketAgent is closing.");
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

  private final boolean sendSubmittalPiece(String piecename, byte [] toSend, PTAuthSubmitResponse response) throws IOException {
    handler.println("transmitting "+piecename+": ["+Ascii.image(toSend)+"]");
    response.vb.reset();
    Alarmer.reset(handler.submitTimeout, alarmum);  // setup an alarmer to kill me if I don't come back within TIMEOUT seconds!
    /*sent =*/ writeBytes(toSend);
    boolean ret = readBytes(response.vb); // receive the reply
    Alarmer.Defuse(alarmum); // kill the alarmer (in case it didn't go off)
    response.reset();
    if(ret) {
      response.process(vb);
    }
    handler.println("received: "+response.packetImage() + " - " + response);
    return ret;
  }

  // called by the alarmum when read+write times out (just do on read? +++)
  public void onTimeout() {
    handler.println("onTimeout(): about to kill the socket!");
    kill();
  }
}



