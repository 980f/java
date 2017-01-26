package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/CheckAuthorizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.32 $
 */

import net.paymate.data.*;
import net.paymate.awtx.*;//Realmoney
import net.paymate.database.*;
import net.paymate.database.ours.query.*;//txnrow
import net.paymate.util.*;
import net.paymate.net.*; // IPSpec
import net.paymate.lang.StringX;
import net.paymate.connection.PaymentReply;
import net.paymate.data.sinet.business.*;

// Used as a bit bucket for now.  All txns that are approved or declined by the AuthManager or Authorizer base class will point to this placeholder class
// +++ Rename InternalAuthorizer

public class CheckAuthorizer extends Authorizer {

  // +++ MUST have a public empty constructor for the loader !!!
  public CheckAuthorizer() {
    super();
  }

  protected void logGatewayTransaction(PayMateDB db, AuthAttempt attempt) {
    // stub; shouldn't do this!
  }

  protected PaymentReply doForce(PayMateDB db, TxnRow record) {
    // stamp original as voided if record was an approved void
    return doneLocally(db, record, alwaysApprove ?
                       AuthResponse.mkApproved("Approved") :
                       AuthResponse.mkDeclined("Cannot do force."));
  }

  public AuthSocketAgent genSubmittalAgent(){
    return new InternalAuthSubmittalAgent(this);
  }

  protected boolean processLocally(AuthTransaction authTran){//modifies flow of reversals
    return true;
  }

  private boolean alwaysApprove = true;
  private static final String ALWAYSKEY = "always";
  private static final String APPROVEFLAG = "APPROVE";
  private void loadAlwaysSetting() {
    alwaysApprove = StringX.equalStrings(APPROVEFLAG, dbd.getServiceParam(serviceName(), ALWAYSKEY, APPROVEFLAG));
  }

  protected void loadProperties() {
    loadAlwaysSetting();
  }

  protected AuthTransaction genTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    return new AuthTransaction(record, original, storeid, slim, merch);
  }
  protected GatewayTransaction genGWTransaction(byte[] bytes, String potentialGWTID) {
    return null; // we don't do this!
  }

  protected AuthTerminalAgent genTermAgent(Terminalid termid) {
    return new bogusAuthAgent(this,termid,sequencer(), termbatchnum(termid),
                              fgThreadPriority, bgThreadPriority);
  }
  protected AuthSubmitTransaction genSubmitTxn(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    AuthSubmitTransaction submittal = new AuthSubmitTransaction(); // +++ extend?
    submittal.request = new InternalAuthSubmitRequest(authid, terminalid, merch);
    submittal.response = new InternalAuthSubmitResponse();
    return submittal;
  }

  protected boolean accepts(TransferType tt) {
    switch(tt.Value()) {
      case TransferType.Return:
      case TransferType.Reversal:
      case TransferType.Authonly:
      case TransferType.Force:
      case TransferType.Modify:
      case TransferType.Query:
      case TransferType.Unknown:
      case TransferType.Sale: {
        return alwaysApprove;// true;
      } // break;
      default:{
        return false;
      } // break;
    }
  }

  public int calculateTxnRate(TransferType tt, PayType pt, Institution in) {
    return 0; // +++ no rates set yet, and no place to get them (what things do we NOT send off?)
  }

  public int calculateSubmittalRate() {
    return 0; // +++ no rates set yet, and no place to get them (what things do we NOT send off?)
  }

  boolean doit(AuthTransaction authTran){
    return markDone(dbd.getPayMateDB(), authTran.record, alwaysApprove ? AuthResponse.mkApproved("OK") : AuthResponse.mkDeclined("NO WAY")); // always approves!
  }
}

class bogusAuthAgent extends AuthTerminalAgent {
  public bogusAuthAgent (CheckAuthorizer authorizer, Terminalid term,
                         Counter sequencer, Counter termbatchnumer,
                         int fgThreadPriority, int bgThreadPriority) {
    super(authorizer,term,sequencer,termbatchnumer,fgThreadPriority,bgThreadPriority);
  }

  public AuthSocketAgent makeSocketAgent() {
    return null;
  }
}

class InternalAuthSubmittalAgent extends AuthSocketAgent {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(InternalAuthSubmittalAgent.class);
  public InternalAuthSubmittalAgent(Authorizer handler) {
    super(null, handler);
  }
  public boolean sendSubmittal(AuthSubmitTransaction txn) {
    if(txn == null) {
      handler.PANIC("Unable to sendSubmittal()!  Txn is null!");
      return false;
    }
    if((txn.request == null) || !(txn.request instanceof InternalAuthSubmitRequest)) {
      handler.PANIC("Unable to sendSubmittal()!  Request is either null or not an InternalAuthSubmitRequest!");
      return false;
    }
    if((txn.response == null) || !(txn.response instanceof InternalAuthSubmitResponse)) {
      handler.PANIC("Unable to sendSubmittal()!  Response is either null or not an InternalAuthSubmitResponse!");
      return false;
    }
    boolean ret = false;
    try {
      handler.println("sendSubmittal starting run method");
      InternalAuthSubmitRequest  request  = (InternalAuthSubmitRequest) txn.request;
      InternalAuthSubmitResponse response = (InternalAuthSubmitResponse)txn.response;
      request.records = txn.records;
      try {
        boolean next = txn.records.next();
        if(next) {
          do {
            txn.request.records = txn.records.copy(); // make a copy so we can test the next one
            TxnRow row = txn.request.records; // then make its name shorter so we don't havbe to type so much here
            next = txn.records.next();
            if(!row.isReversal() && !row.isVoided() && row.wasAuthApproved() && row.responded() && !row.isQuery()) {
              request.add(row);
              handler.println("ADDED ONE ["+row.txnid+"]: ["+String.valueOf(row)+"]");
            }
          } while(next);
          // stamp the database with the totals from the request object
          handler.dbd.getPayMateDB().setBatchTotals(request.batchid(), request.txncount(), request.txntotal()); /// ++++ @@@ move to PaymentechAuth or something - put where we stamp approval ???
          // fill in the response
          response.markApproved("INTERNAL APPR");
          ret = true;
        } else {
          // can't do a batch if there aren't any records!
        }
      } catch(Exception exception2) {
        handler.println("InternalAuthSubmittalAgent had possible error: " + exception2);
        dbg.Caught(exception2);
      } finally {
        handler.println("InternalAuthSubmittalAgent is closing.");
        if(!ret) {
          handler.PANIC("Submittal [" + txn.request.batchid() + "] failed!", "Txn=["+txn+"]. socketOpenAttemptCount = " + txn.socketOpenAttempts.value());
        }
      }
    } catch (Throwable t) {
      dbg.Caught(t);
      handler.PANIC("Exception attempting to sendSubmittal()! "+t, "Txn=["+txn+"]. socketOpenAttemptCount = " + txn.socketOpenAttempts.value());
    } finally {
      return ret;
    }
  }
}

class InternalAuthSubmitRequest extends AuthSubmitRequest {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(InternalAuthSubmitRequest.class);
  public InternalAuthSubmitRequest(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    super(authid, terminalid, merch);
  }
  public int maxRequestSize() {
    return 0;
  }


  public boolean add(TxnRow tjr) {
    // add up the count and total;
    long amt = tjr.netSettleAmountCents();
    dbg.ERROR("adding " + amt);
    amounts.add(amt);
    LocalTimeFormat ltf = LocalTimeFormat.New(merch.tz(), "MMddyyHHmmss");
    return true;
  }
}

class InternalAuthSubmitResponse extends AuthSubmitResponse {
}

