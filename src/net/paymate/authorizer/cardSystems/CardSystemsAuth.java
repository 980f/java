package net.paymate.authorizer.cardSystems;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CardSystemsAuth.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.80 $
 */

import java.util.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.authorizer.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.*;
import net.paymate.data.*;
import net.paymate.awtx.RealMoney;
import net.paymate.jpos.data.*; // TrackData
import net.paymate.connection.PaymentReply;
import net.paymate.data.sinet.business.*;

// +++ Eventually we will want to certify with CS by sending them compliance data in submittals, which we do not do currently (field D21).
// +++ Whenever convenient. Does not affect auth msgs.

public final class CardSystemsAuth extends Authorizer implements CardSystemConstants {

  public CardSystemsAuth() {
    super();
  }

  protected void logGatewayTransaction(PayMateDB db, AuthAttempt attempt) {
    // stub; shouldn't do this!
  }
  protected PaymentReply doForce(PayMateDB db, TxnRow record) {
    return doneLocally(db, record, AuthResponse.mkDeclined("Cannot do force."));
  }

  // whether to just auth and NOT settle
  // +++ eventually need to know who we are going to settle with when the txn is created,
  // +++ so, don't make the authNoSettle a parameter of the authorizer, but instead a parameter of the STOREAUTH info,
  // +++ which should be an OBJECT!
  private boolean getAuthNoSettle() {
    return dbd.getBooleanServiceParam(serviceName(), "authNoSettle", false);
  }

  protected final void loadProperties() {
    getAuthNoSettle();  // just a primer
  }

  protected boolean processLocally(AuthTransaction authTran){//modifies flow of reversals
    TransferType tt=authTran.record.transfertype();
    switch (tt.Value()) {//exclude some of these from going to the authorizer:
      case TransferType.Unknown:
        return true;
      case TransferType.Reversal:
        return authTran.record.paytype().is(PayType.Credit);  // only locally auth the credit voids !!!
      default:
        return false;
    }
  }

  protected final AuthTransaction genTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    return new MaverickTransaction(record, original, storeid, slim, merch, getAuthNoSettle(), this);
  }
  protected GatewayTransaction genGWTransaction(byte[] bytes, String potentialGWTID) {
    return null; // they don't do this!
  }

  protected final AuthTerminalAgent genTermAgent(Terminalid termid) {
    return new VBAuthTermAgent(this, termid, bufferSize, sequencer(),
                               termbatchnum(termid), fgThreadPriority,
                               bgThreadPriority);
  }

  public AuthSocketAgent genSubmittalAgent() {
    return new CSSubmittalSocketAgent(this); // an extension of AuthSocketAgent
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

  public int calculateTxnRate(TransferType tt, PayType pt, Institution in) {
    switch (tt.Value()) {
      case TransferType.Reversal:
        return 0; // we are NOT sending off the reversals [see processLocally() above]
      default:
        return 200;
    }
  }

  public int calculateSubmittalRate() {
    return 200; // +++ no rates set yet, and no place to get them (what things do we NOT send off?)
  }

  protected AuthSubmitTransaction genSubmitTxn(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    AuthSubmitTransaction submittal = new AuthSubmitTransaction(); // +++ extend?
    submittal.request = new CSAuthSubmitRequest(authid, terminalid, merch);
    submittal.response = new CSAuthSubmitResponse();
    return submittal;
  }

  /* package */ static void AmountInfo(VisaBuffer req, RealMoney amount){
    AmountInfo(req, amount.Value());
  }
  /* package */ static void AmountInfo(VisaBuffer req, long amount){
    req.appendNumericFrame(amount, 3);
  }
  /* package */ static void source(VisaBuffer req, String trackData1, String trackData2, boolean forceManualCard, MSRData card) {
    if(!forceManualCard){
      if(TrackData.isProper(MSRData.T2, trackData2)){//track 2 preferred
        req.append('D'); //4.10 swipe, track 2
        req.append(trackData2);
        return;
      }
      else if(TrackData.isProper(MSRData.T1, trackData1)){//T1 if T2 not present
        req.append('H'); //4.10 swipe, track 1
        req.append(trackData1);
        return;
      }
    }
  //If forced, or neither track is decent:
    req.append('T'); //4.10 manual, track 2 capable
    req.appendFrame(card.accountNumber.Image());
    req.appendFrame(card.expirationDate.mmYY());
  }

}
//$Id: CardSystemsAuth.java,v 1.80 2004/03/30 03:33:07 mattm Exp $
