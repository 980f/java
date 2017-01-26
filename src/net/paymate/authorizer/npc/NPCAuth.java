package net.paymate.authorizer.npc;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCAuth.java,v $
 * Description:  Paymentech Authorization Service
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.28 $
 */

import net.paymate.authorizer.*;
import net.paymate.database.ours.query.*;
import net.paymate.database.PayMateDB;
import net.paymate.connection.PaymentReply;
import net.paymate.util.*; // ErrorLogStream
import net.paymate.data.*; //Storeid
import net.paymate.awtx.RealMoney;
import java.io.*; // File for path
import net.paymate.lang.ThreadX;
import net.paymate.data.sinet.business.*;

public class NPCAuth extends Authorizer implements NPCConstants {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NPCAuth.class);

  // MUST have a public empty constructor for the loader !!!
  public NPCAuth() {
    super();
    authsubmitmonitor = new Monitor(serviceName()+"Submit");
  }
  protected void logGatewayTransaction(PayMateDB db, AuthAttempt attempt) {
    // stub; shouldn't do this!
  }
  protected PaymentReply doForce(PayMateDB db, TxnRow record) {
    return doneLocally(db, record, AuthResponse.mkDeclined("Cannot do force."));
  }
  protected AuthTransaction genTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    return new NPCTransaction(record, original, storeid, slim, merch);
  }
  protected GatewayTransaction genGWTransaction(byte[] bytes, String potentialGWTID) {
    return null; // they don't do this!
  }

  protected boolean accepts(TransferType tt) {
    switch(tt.Value()) {
      case TransferType.Return:
      case TransferType.Reversal:
      case TransferType.Sale: {
        return true;
      } // break;
      case TransferType.Authonly:
      case TransferType.Force:
      case TransferType.Modify:
      case TransferType.Query:
      case TransferType.Unknown:
      default:{
        return false;
      } // break;
    }
  }

  public int calculateSubmittalRate() {
    return 200; // +++ no rates set yet, and no place to get them (what things do we NOT send off?)
  }
  public int calculateTxnRate(TransferType tt, PayType pt, Institution in) {
    // everything but the voids are sent off through maverick, so put them here ....
    switch (tt.Value()) {
      case TransferType.Reversal:
        return 0;
      default:
        return 200;
    }
//    return 0; // these are not sent off through this authorizer
  }
  public AuthSocketAgent genSubmittalAgent() {
    return new NPCSubmittalSocketAgent(VisaBuffer.NewReceiver(bufferSize), this); // an extension of AuthSocketAgent
  }
  protected void loadProperties() {
    getJustSimulate();
    getTestMode(); // prime it, so that we can change the parameter if we want, even before the first settlement has occurred
    getCompuserveRoutingIndicator(); // prime it
    getSubmittalSleepInterval();
  }

  private Monitor authsubmitmonitor; // set in constructor of class

  /* package */ boolean sendSubmittal(NPCSubmittalSocketAgent socketAgent, AuthSubmitTransaction txn) {
    boolean ret = false;
    // mutex
    authsubmitmonitor.getMonitor();
    try {
      // call the sendSubmittal on the socketagent
      ret = socketAgent.sendMutexedSubmittal(txn);
      ThreadX.sleepFor(Ticks.forSeconds(getSubmittalSleepInterval())); // this lets the serial port reset itself
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      authsubmitmonitor.freeMonitor();
      return ret ;
    }
  }

/////////////////////////
/// Grabbing parameters
  private static final String TESTMODE = "TESTMODE";
  private boolean getTestMode() {
    return dbd.getBooleanServiceParam(serviceName(), TESTMODE, false);
  }

  private static final String CSROUTIND = "CSROUTIND";
  private String getCompuserveRoutingIndicator() {
    return dbd.getServiceParam(serviceName(), CSROUTIND, "");
  }

  // in seconds
  private static final String SUBMITSLEEP = "SUBMITSLEEP";
  private static final int DEFAULTSUBMITSLEEP = 10;
  private int getSubmittalSleepInterval() {
    return dbd.getIntServiceParam(serviceName(), SUBMITSLEEP, DEFAULTSUBMITSLEEP);
  }

  private static final String JUSTSIMULATE = "JUSTSIMULATE";
  private boolean getJustSimulate() {
    return dbd.getBooleanServiceParam(serviceName(), JUSTSIMULATE, false);
  }
/// Grabbing parameters
/////////////////////////

  protected AuthSubmitTransaction genSubmitTxn(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    AuthSubmitTransaction submittal = new AuthSubmitTransaction();
    submittal.request = new NPCAuthSubmitRequest(authid, terminalid, merch, this, getTestMode(), getCompuserveRoutingIndicator(), getJustSimulate());
    submittal.response = new NPCAuthSubmitResponse();
    return submittal;
  }
  protected boolean processLocally(AuthTransaction authTran) {
    return true; // we don't do auths at all with them, actually +++ what to do here?
  }
  protected AuthTerminalAgent genTermAgent(Terminalid termid) {
    return new VBAuthTermAgent(this, termid, bufferSize, sequencer(),
                               termbatchnum(termid), fgThreadPriority,
                               bgThreadPriority);
  }

}
// $Id: NPCAuth.java,v 1.28 2004/03/30 03:33:08 mattm Exp $
