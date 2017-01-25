/**
* Title:        Tranjour<p>
* Description:  Data structure capable of holding the data from the tranjour table<p>
* Copyright:    2000<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: TxnRow.java,v 1.1 2001/11/17 06:16:59 mattm Exp $
*/
package net.paymate.database.ours.query;
import  net.paymate.ISO8583.factory.Field;
import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.database.ours.*;
import  net.paymate.util.*;
import  net.paymate.awtx.*;
import  net.paymate.ISO8583.data.*;
import  net.paymate.jpos.data.*;
import  net.paymate.connection.*;//resolve locations

public class TxnRow extends Query {

  private static final ErrorLogStream dbg = new ErrorLogStream(TxnRow.class.getName(), ErrorLogStream.WARNING);
  static final String tranmoney="#0.00"; //has decimal point but no dollar sign

  private String title = "";
  public String setTitle(String newTitle) {
    title = newTitle;
    return title();
  }
  public String title() {
    return title;
  }
  public int storeid() {
    return Safe.parseInt(storeid);
  }

  public TxnRow() {
    this(null);
    //all fields are init'ed to ""
  }

  protected TxnRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a tranjour that can NOT scroll (just a snapshot of a single record).
   */
  public static final TxnRow NewOne(ResultSet rs) {
    TxnRow tj = new TxnRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a tranjour that CAN scroll.
   */
  public static final TxnRow NewSet(Statement stmt) {
    return new TxnRow(stmt);
  }

  /**
   * users: batchlistings, getting original info for void receipt
   * modifyemployee is time that tran was swiped
   */
  public java.util.Date refTime(){
    return PayMateDB.tranUTC(wasStoodin()? clientreftime:  transtarttime);//utc#
  }

  public RealMoney rawamount(){//ignores trantype, hence always positive
    return new RealMoney(transactionamount);
  }

  // monitor (or something) these so that you only have to do the processing once. ( dirty bit logic)
  ProcessingCode proccode;
  public boolean isReturn(){
    proccode=ProcessingCode.Parse(processingcode);
    return  proccode.isaReturn();
  }
  public boolean isDebit() {
    proccode=ProcessingCode.Parse(processingcode);
    return  proccode.isaDebit();
  }
  public boolean isCredit() {
    proccode=ProcessingCode.Parse(processingcode);
    boolean ret = proccode.isaCredit();
    return  ret;
  }

  public boolean isReversal() {
    return "0400".equals(messagetype);
  }

  // +++ put this elsewhere?
  public ActionType actionType() {
    ActionType type = new ActionType();
    if(isReversal()) {
      type.setto(ActionType.reversal);
    } else if(isDebit()) {
      type.setto(ActionType.debit);
    } else if(isCredit()) {
      type.setto(ActionType.credit);
    } else {
      // what else can it be?
      type.setto(ActionType.check);
    }
    return type;
  }

  public boolean neverAuthed(){
    return !Safe.hasSubstance(hostresponsecode);
  }

  public boolean wasStoodin(){
    return  Safe.hasSubstance(/*stoodinstan*/clientreftime); // --- possible source of bug
  }

  public boolean wasVoided() {
    return "Y".equals(voidtransaction);
  }

  public int siStan(){
    return wasStoodin()? Safe.parseInt(stoodinstan) :0; //0 is not a legal stan
  }

  public LedgerValue netamount(){//using trantype, positive for sale, negative for refund
    LedgerValue newone=new LedgerValue(tranmoney);
    newone.parse(transactionamount);
    newone.changeSignIf(isReturn());
    return newone;
  }

  public MSRData card(){
    MSRData newone=new MSRData();
    newone.accountNumber= new CardNumber(cardholderaccount);
    newone.expirationDate= new ExpirationDate(expirationdate);
    return newone;
  }

  /**
   * @deprecate should fetch card then do things with it.
   */
  public ExpirationDate expiry(){
    return new ExpirationDate(expirationdate);
  }

  public String cardType(){
    return Safe.fill(paymenttypecode, '_', 2, true /*left*/);
  }

  public String cardGreeked(){
    return cardType() + card().accountNumber.Greeked("...");
  }

  public TransactionID tid(){
    return TransactionID.New(transtarttime,stan,Integer.valueOf(storeid).intValue());
  }

  public ResponseCode response(){
    return new ResponseCode(responsecode);
  }

  public void clearAll() {
    setAllTo("");
  }

/**
 * @return true if NOT voided,declined, etc.
 */
  public boolean isGood(){
    return response().isApproved();
  }
//////////////////////
  // +++ enhance this to handle different data types (etc)
  public String actioncode = "";
  public String authendtime = ""; // unused ?
  public String authidresponse = "";
  public String authid = ""; // unused ?
  public String authseq = "";
  public String authstarttime = ""; // unused ?
  public String authtermid = "";
  public String storeid = "";
  public String cardacceptortermid = "";
  public String cardholderaccount = "";
  public String expirationdate = "";
  public String hostresponsecode = "";
  public String hosttracedata = ""; // unused ?
  public String messagetype = "";
  public String stoodinstan = "";
  public String clientreftime = "";
  public String originalstan = ""; // unused ?
  public String paymenttypecode = "";
  public String processingcode = "";
  public String responsecode = "";
  public String stan = "";
  public String track1data = ""; // unused ?
  public String track2data = ""; // unused ?
  public String tranendtime = ""; // unused ?
  public String transactionamount = "";
  public String transactiontype = ""; // unused ?
  public String transtarttime = "";
  public String voidtransaction = "";

  // +++ rewrite using reflection
  public final String toString() {
    String ret = "";
    TextList tl = new TextList(60);
    tl.add("actioncode="+actioncode);
    tl.add("authendtime="+authendtime);
    tl.add("authidresponse="+authidresponse);
    tl.add("authid="+authid);
    tl.add("authseq="+authseq);
    tl.add("authstarttime="+authstarttime);
    tl.add("authtermid="+authtermid);
    tl.add("storeid="+storeid);
    tl.add("cardacceptortermid="+cardacceptortermid);
    tl.add("cardholderaccount="+cardholderaccount);
    tl.add("expirationdate="+expirationdate);
    tl.add("hostresponsecode="+hostresponsecode);
    tl.add("hosttracedata="+hosttracedata);
    tl.add("messagetype="+messagetype);
    tl.add("stoodinstan="+stoodinstan);
    tl.add("clientreftime="+clientreftime);
    tl.add("originalstan="+originalstan);
    tl.add("paymenttypecode="+paymenttypecode);
    tl.add("processingcode="+processingcode);
    tl.add("responsecode="+responsecode);
    tl.add("stan="+stan);
    tl.add("track1data="+track1data);
    tl.add("track2data="+track2data);
    tl.add("tranendtime="+tranendtime);
    tl.add("transactionamount="+transactionamount);
    tl.add("transactiontype="+transactiontype);
    tl.add("transtarttime="+transtarttime);
    tl.add("voidtransaction="+voidtransaction);
    ret = tl.asParagraph();
    return ret;
  }


/*
  public String ISO(int isobit){
    switch (isobit){
      default: return null;
      case Field.PrimaryAccountNumber                : return cardholderaccount ;
      case Field.ProcessingCode                      : return processingcode ;
      case Field.TransactionAmount                   : return transactionamount ;
      case Field.SystemTraceAuditNumber              : return stan ;
      case Field.ExpirationDate                      : return expirationdate ;
      case Field.Track2Data                          : return track2data ;
      case Field.AuthorizationIdentificationResponse : return authidresponse ;
      case Field.ResponseCode                        : return responsecode ;
      case Field.CardAcceptorTerminalIdentification  : return cardacceptortermid ;
      case Field.CardAcceptorIdentificationCode      : return storeid ;
      case Field.Track1Data                          : return track1data ;
      case Field.TransStartTime                      : return transtarttime ;
      case Field.TransEndTime                        : return tranendtime ;
      case Field.AuthorizationStartTime              : return authstarttime ;
      case Field.AuthorizationEndTime                : return authendtime ;
      case Field.PaymentTypeCode                     : return paymenttypecode ;
      case Field.TransactionType                     : return transactiontype ;
      case Field.OriginalSTAN                        : return originalstan ;
      case Field.ActionCode                          : return actioncode ;
      case Field.AuthorizerName                      : return authorizername ;
      case Field.HostResponseCode                    : return hostresponsecode ;
    }
  }
*/

  public static final TxnRow forTesting(){
    TxnRow tester=new TxnRow();
    tester.storeid = "%storeKey%";
    tester.cardacceptortermid = "%LAME001%";
    tester.cardholderaccount = "4200000000000000";
    tester.messagetype = "0200";
    tester.processingcode = "003000";
    tester.stan = "12345";
    tester.transactionamount = "54321";
    tester.authidresponse = "%0auth0%";
    tester.voidtransaction = "N";
    tester.track1data = "";
    tester.track2data = "";
    tester.responsecode = "";
    tester.expirationdate = "0504";
    tester.transtarttime = "2001mmddhhmmss";//- alh:removed millis
    tester.tranendtime =   "2001mmddhhmmss";
    tester.authendtime = "";
    tester.authstarttime = "";
    tester.paymenttypecode = "";
    tester.transactiontype = "";
    tester.originalstan = "";
    tester.actioncode = "";
    tester.authid = "";
    tester.hostresponsecode = "";
    tester.hosttracedata = "";
    tester.clientreftime = "";
    tester.stoodinstan = "";
    return tester;
  }
}
//$Id: TxnRow.java,v 1.1 2001/11/17 06:16:59 mattm Exp $
