package net.paymate.authorizer.cardSystems;

import net.paymate.authorizer.*; // AuthSocketAgent, etc.
import net.paymate.net.*; // IPSpec
import net.paymate.database.*; // PayMateDBDispenser
import net.paymate.util.*; // ErrorLogStream
import net.paymate.util.timer.*; // TimeBomb, etc.
import java.io.*; // socket stream stuff
import java.util.*; // vector
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.VisaBuffer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CSSubmittalSocketAgent.java,v $
 * Description: manage submission of batches
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.37 $
 */

// This class is currently just CS, but may be base-d into Authorizer to handle differences between PT and CS!
// Single thread coming in on sendSubmittal().
// Mutexing is handled in AuthTerminalAgent, so no 2 threads should ever enter here simultaneously.  No worries, mate!

public class CSSubmittalSocketAgent extends SubmittalSocketAgent implements TimeBomb {

  private static final Tracer dbg = new Tracer(CSSubmittalSocketAgent.class, ErrorLogStream.VERBOSE);

  //int u2size=36+63+5; //response+data+margin

  public CSSubmittalSocketAgent(Authorizer handler) {
    super(VisaBuffer.NewReceiver(500 /*what is this for real?*/).setClipLRC(), handler);
  }

  public boolean sendSubmittal(AuthSubmitTransaction txn) {
    if(txn == null) {
      handler.PANIC("Unable to sendSubmittal()!  Txn is null!");
      return false;
    }
    if((txn.request == null) || !(txn.request instanceof CSAuthSubmitRequest)) {
      handler.PANIC("Unable to sendSubmittal()!  Request is either null or not an CSAuthSubmitRequest!");
      return false;
    }
    if((txn.response == null) || !(txn.response instanceof CSAuthSubmitResponse)) {
      handler.PANIC("Unable to sendSubmittal()!  Response is either null or not an CSAuthSubmitResponse!");
      return false;
    }
    try {
      handler.println("sendSubmittal starting run method");
      // AuthSubmitRequest has an authid and terminalid in it.  Use the authorizer's db, if needed.
      // run the submission to try to get the actioncode & authrespmsg back
      // if couldn't do the submittal, PANIC !!!
      CSAuthSubmitRequest  request  = (CSAuthSubmitRequest) txn.request;
      CSAuthSubmitResponse response = (CSAuthSubmitResponse)txn.response;

      request.records = txn.records;
      boolean ret = false;
      try {
        byte [] piece = null;
        // first, make all of the packets and calculate the totals
        byte [] detail = new byte[0];
        // we have to know the last record IS the last record before we MAKE the last record !!!!!
        Vector details = new Vector(100, 10);
        while(txn.records.next()) {
          TxnRow current = txn.records.copy();
          dbg.ERROR("record["+current.txnid+"]: isReversal="+current.isReversal()+
                    ", wasVoided="+current.isVoided()+", wasAuthApproved="+
                    current.wasAuthApproved()+", responded="+current.responded()+
                    ", settle="+current.settle());
          if(current.isReversal() || current.isVoided() ||
             !current.wasAuthApproved() || !current.responded() ||
             !current.settle()) {
            // skip it
          } else {
            // add it
            details.add(current);
            request.addToTotals(current);
          }
        }
        if(details.size() == 0) {
          return ret=true; // can't do this if there aren't any records!, but that isn't an error!
        }
        // stamp the database with the totals from the request object
        PayMateDBDispenser.getPayMateDB().setBatchTotals(request.batchid(), request.txncount(), request.txntotal()); /// +++ move to Auth module or something - put where we stamp approval ???
        // get the socket and streams prepped
        getSettleSocket(txn); // throws on error
        // Build the request object for the first header & transmit it
        dbg.mark("Header");
        // Check the reply to see if we can continue (etc.)
        byte [] header = request.header();
handler.println("Header: ["+new String(header)+"]");
        response.reply=vb;//might have to autoswitch between acknakc and u2buffer
        if(sendSubmittalPiece("header", header, response)) { // will get ack 6 or nak 15.
          boolean headerGood = response.headerGood();
          dbg.ERROR("Headergood = " + headerGood);
          if(headerGood) {
            // if the response to the header was just an ACK, send the detail, otherwise, use it as the REAL approval!
            dbg.ERROR("wasacknaked = " + response.wasAckNaked);
            if(!response.wasAckNaked) { // real approval
              handler.println("CS settled without detail: " + txn);
              // just drop out, I think ...
              return ret = true;
            } else { // send the detail
              // Send the detail
              dbg.mark("Detail");
              // skip through the records, sending each detail
              int last = details.size() - 1;
              for(int i = 0; i <= last; i++) {
                request.records = (TxnRow)details.elementAt(i);
                piece = request.detail(i == last);
                String posname = "Detail# "+i;
                handler.println(posname+" ["+request.records.txnid+"]: "+Ascii.bracket(piece));
                dbg.mark(posname); // for each one, set its record number, maybe off of txnid
                if(!sendSubmittalPiece(posname, piece, response)) {
                  handler.println("failed sending detail!");
                  return ret=false;
                }
                if(!response.detailGood(i == last)){
                  //ret=false causes a PANIC later
                  handler.println("detail refused at chunk:"+posname);
                  return ret=false;
                  // there is a resend rule somewhere, use it ? +++
                }
              }
            }
          } else {
            handler.println("header refused!");
          }
        } else {
          handler.println("Unable to send header!");
        }
        return ret=true;
      } catch(Exception exception2) {
        handler.println("CSSubmittalSocketAgent had possible error ["+dbg.location+"]: " + exception2);
        dbg.Caught(exception2);
        response.markFailed("Exception during transmission");
        return ret=false;
      } finally {
        if(!shouldDie) {
//          handler.reads.add(reply.length);
          handler.println("CSSubmittalSocketAgent response = " + response);
//          authTransaction.response.finish(vb);
        }
        handler.println("CSSubmittalSocketAgent is closing.");
        kill();
        if(!ret) {
          handler.PANIC("Submittal [" + request.batchid() + "] failed!", "Txn=["+txn+"].");
          handler.settleIps.thisFailed(txn.host); // +_+ should this be here?
        }
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      handler.PANIC("Exception attempting to sendSubmittal()! "+t, "Txn=["+txn+"].");
      return false;
    } finally {
      dbg.mark("done");
    }
  }

  private final boolean sendSubmittalPiece(String piecename, byte [] toSend, CSAuthSubmitResponse response) throws Exception {
    handler.println("transmitting "+piecename+": ["+(toSend == null ? "null" : Ascii.image(toSend).toString()) +"]");
    response.reply.reset();
    Alarmer.reset(handler.submitTimeout, alarmum);  // setup an alarmer to kill me if I don't come back within TIMEOUT seconds!
    if(toSend != null) {
      /*sent =*/ writeBytes(toSend);
    }
    boolean ret = readBytes(response.reply); // receive the reply
    Alarmer.Defuse(alarmum); // kill the alarmer (in case it didn't go off)
    response.reset();
    if(ret) {
      response.process(vb);
    }
    // +++ add resend capability here (maybe for a 3-time retry limit?)
    handler.println("received: "+ response.packetImage()+" - " + response);
    return ret;
  }

  // called by the alarmum when read+write times out (just do on read? +++)
  public void onTimeout() {
    kill();
  }

}
//$Id: CSSubmittalSocketAgent.java,v 1.37 2004/02/11 00:57:03 mattm Exp $


