package net.paymate.authorizer.linkpoint;

import net.paymate.authorizer.*; // AuthSocketAgent, etc.
import net.paymate.net.*; // IPSpec
import net.paymate.database.*; // PayMateDBDispenser
import net.paymate.util.*; // ErrorLogStream
import net.paymate.util.timer.*; // TimeBomb, etc.
import java.io.*; // socket stream stuff
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*; // packet

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LPSubmittalSocketAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

// This just needs to fake it.  You don't settle with Linkpoint, as it is host capture!  (Is this true?)

public class LPSubmittalSocketAgent extends SubmittalSocketAgent implements TimeBomb {

  private static final Tracer dbg = new Tracer(LPSubmittalSocketAgent.class, ErrorLogStream.WARNING);

  public LPSubmittalSocketAgent(Authorizer handler) {
    super(null, handler);
  }

  public boolean sendSubmittal(AuthSubmitTransaction txn) {
    handler.connectionAttempts.incr();
    if(txn == null) {
      handler.PANIC("Unable to sendSubmittal()!  Txn is null!");
      return false;
    }
    if((txn.request == null) || !(txn.request instanceof LinkpointSettlementRequest)) {
      handler.PANIC("Unable to sendSubmittal()!  Request is either null or not an LinkpointSettlementRequest!");
      return false;
    }
    if((txn.response == null) || !(txn.response instanceof LinkpointSettlementResponse)) {
      handler.PANIC("Unable to sendSubmittal()!  Response is either null or not an LinkpointSettlementResponse!");
      return false;
    }
    try {
      // +++ combine with ALWAYS AUTH submittalSocketAgents from other authorizers into a single class
      handler.println("sendSubmittal starting run method");
      // AuthSubmitRequest has an authid and terminalid in it.  Use the authorizer's db, if needed.
      // run the submission to try to get the actioncode & authrespmsg back
      // if couldn't do the submittal, PANIC !!!
      LinkpointSettlementRequest  request  = (LinkpointSettlementRequest) txn.request;
      LinkpointSettlementResponse response = (LinkpointSettlementResponse)txn.response;
      request.records = txn.records;
      boolean ret = false;
      try {
        while(txn.records.next()) {
          TxnRow row = txn.records; // shorter name for less typing
          dbg.WARNING("record["+row.txnid+"]: isReversal="+row.isReversal()+
                      ", wasVoided="+row.isVoided()+", wasAuthApproved="+
                      row.wasAuthApproved()+", isQuery="+row.isQuery()+
                      ", responded="+row.responded()+", settle="+row.settle());
          if(row.isReversal() || row.isVoided() || !row.wasAuthApproved() ||
             !row.responded() || row.isQuery() || !row.settle()) {
            continue; // skip it
          }
          txn.request.records = row;
          request.detail(); // adds the numbers into the totals
        }
        // stamp the database with the totals from the request object
        PayMateDBDispenser.getPayMateDB().setBatchTotals(request.batchid(), request.txncount(), request.txntotal());
        response.markApproved("OK");
        handler.connections.incr();
        dbg.mark("done");
        return ret = true;
      } catch(Exception exception2) {
        handler.println("LPSubmittalSocketAgent had possible error ["+dbg.location+"]: " + exception2);
        dbg.Caught(exception2);
        response.markFailed("Exception during transmission");
        return ret = false;
      } finally {
        handler.println("LPSubmittalSocketAgent is closing.");
        if(!ret) {
          handler.PANIC("Submittal [" + txn.request.batchid() + "] failed!", "Txn=["+txn+"]");
        }
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      handler.PANIC("Exception attempting to sendSubmittal()! "+t, "Txn=["+txn+"]");
      return false;
    }
  }
}
