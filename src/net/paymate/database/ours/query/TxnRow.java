/**
* Title:        $Source: /cvs/src/net/paymate/database/ours/query/TxnRow.java,v $
* Description:  Data structure capable of holding the data from the txn table<p>
* Copyright:    2000-2002
* Company:      PayMate.net
* @author       PayMate.net
* @version      $Revision: 1.142 $
*/
package net.paymate.database.ours.query;
import java.sql.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.util.*;
import net.paymate.awtx.*;
import net.paymate.jpos.data.*;
import net.paymate.connection.*;
import net.paymate.authorizer.*; // AuthResponse, VisaCPSdata
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.lang.Fstring;
import net.paymate.data.sinet.business.*;
import net.paymate.jpos.awt.Hancock; // for signature extraction, etc.

public class TxnRow extends Query {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TxnRow.class, ErrorLogStream.WARNING);
  static final String tranmoney="#0.00"; //has decimal point but no dollar sign//should come from Enterprise table

  private String title = "";
  public String setTitle(String newTitle) {
    title = newTitle;
    return title();
  }
  public String title() {
    return title;
  }

  public TxnRow() {
    this(null);
    //all fields are init'ed to ""
  }

  protected TxnRow(Statement stmt) {
    super(stmt);
  }

  protected void refresh(){ //reset all cached interprations
    auth=null;
    card=null;
    sale=null;
  }

  protected void fromResultSet(ResultSet rs) {
    super.fromResultSet(rs);
    refresh();
  }

  /////////////////
  /**
   * Makes a txn that can NOT scroll (just a snapshot of a single record).
   */
  public static final TxnRow NewOne(ResultSet rs) {
    TxnRow tj = new TxnRow();
    tj.fromResultSet(rs);
    return tj;
  }
  public static final TxnRow NewOne(EasyProperties ezp) {
    TxnRow tj = new TxnRow();
    tj.fromProperties(ezp);
    return tj;
  }

  /**
   * Makes a txn that CAN scroll.
   */
  public static final TxnRow NewSet(Statement stmt) {
    return new TxnRow(stmt);
  }
/////////////////////////////////////////
///

  public void setSettleop(TransferType from) {
    SettleOp so = new SettleOp();
    boolean authzB = false;
    boolean settleB = false;
    if(TransferType.IsLegal(from)) {
      switch (from.Value()) {
        case TransferType.Authonly: {
          so.setto(SettleOp.Sale);
          authzB = true;
          // settle stays false
        } break;
        case TransferType.Force: {
          so.setto(SettleOp.Sale);
          // authz stays false
          settleB = true;
        } break;
        case TransferType.Sale:  {
          so.setto(SettleOp.Sale);
          authzB = true;
          settleB = true;
        } break;
        case TransferType.Modify: {
          so.setto(SettleOp.Modify);
          // authz stays false
          // settle stays false (but need to set it to true in the original! +++)
        } break;
        case TransferType.Query: {
          so.setto(SettleOp.Query);
          authzB = true;
          // settle stays false
        } break;
        case TransferType.Return: {
          so.setto(SettleOp.Return);
          authzB = true;
          settleB = true;
        } break;
        case TransferType.Reversal: {
          so.setto(SettleOp.Void);
          // authz stays false (until set otherwise)
          // settle stays false
        } break;
        default:  {
          so.setto(SettleOp.Unknown);
          // authz stays false
          // settle stays false
        } break;
      }
    }
    settleop=String.valueOf(so.Char());
    setAuthz(authzB);
    setSettle(settleB);
  }

  PINData pindata; //cached
  public PINData pindata(){
    if(pindata==null){
      pindata= PINData.Null();
    }
    return pindata;
  }
  public TxnRow setPINdata(PINData pindata){
    this.pindata=pindata;
    return this;
  }

  public Authid authid() {
    return new Authid(authid);
  }

  public Authid settleid() {
    return new Authid(settleid);
  }

  public Txnid txnid() {
    return new Txnid(txnid);
  }

  public Terminalid terminalid() {
    return new Terminalid(terminalid);
  }

  public Drawerid drawerid() {
    return new Drawerid(drawerid);
  }

  public Associateid associateid() {
    return new Associateid(associateid);
  }

  public STAN stan() {
    return STAN.NewFrom(stan);
  }

  public String refNum() {
    return String.valueOf(STAN.NewFrom(stan));
  }

/**
 * @return whether newly set stan is valid.
 */
  public boolean setStan(STAN stan){
    if(STAN.isValid(stan)){
      this.stan=String.valueOf(stan);
      return true;
    } else {
      this.stan="";
      return false;
    }
  }

  /**
   * users: batchlistings, getting original info for void receipt
   * time is that when tran was spawned, according to client
   */
  public UTC refTime(){
    return UTC.New(clientreftime);
  }

  public TxnReference tref(){
    return TxnReference.New(txnid(), terminalid() , stan(), refTime());
  }

  public RealMoney rawAuthAmount(){//ignores trantype, hence always positive
    return new RealMoney(amount);
  }

  public RealMoney rawSettleAmount(){//ignores trantype, hence always positive
    return new RealMoney(settleamount);
  }

  public long netSettleAmountCents(){//negative if is a return
    return - Bool.signum(isReturn()) * rawSettleAmount().Value();
  }

  public boolean amountsMatch() {
    return rawAuthAmount().Value() == rawSettleAmount().Value();
  }

  // don't know for sure, but maybe
  public RealMoney probablyTipAmount() {
    long samt = rawSettleAmount().Value();
    long aamt = rawAuthAmount().Value();
    return new RealMoney((aamt > samt) ? 0L : (samt - aamt));
  }

  public TxnRow setAuthAmount(RealMoney rm){
    amount=realMoneyToString(rm);
    sale=null;//for refresh of sale info
    return this;
  }
  public TxnRow setAuthAmount(String image){
    return setAuthAmount(new RealMoney(image));
  }

  public static final String realMoneyToString(RealMoney rm) {
    // +++ use some kind of range here, but not sure which class to use
    // first, be sure that the money will fit in the database!
    // the max size of the money in the database is: -2,147,483,647 to 2,147,483,647.
    long amt = rm.Value();//which is cents.
    if(amt < -2147483647 || amt > 2147483647) { // this is an overflow
      amt = 2147483647;
    } else if(amt < 0) { // money should never be negative in the database!
      amt = -amt;
    }
    return String.valueOf(amt);
  }

  public TxnRow setSettleAmount(RealMoney rm){
    settleamount=realMoneyToString(rm);
    return this;
  }

  public PayType paytype(){
    return SaleInfo().type.payby;
  }

  public TxnRow setPaytype(PayType pt){
    paytype=String.valueOf(pt.Char());
    sale=null;//for refresh of sale info
    return this;
  }
  public TxnRow setPaytype(char onechar){
    return setPaytype(new PayType(onechar));
  }

  public boolean isDebit() {
    return paytype().is(PayType.Debit);
  }
  public boolean isGiftCard() {
    return paytype().is(PayType.GiftCard);
  }
  public boolean isCredit() {
    return paytype().is(PayType.Credit);
  }

  public TransferType transfertype(){
    return SaleInfo().type.op;
  }

  public TxnRow setTransferType(TransferType tt){
    transfertype=String.valueOf(tt.Char());
    sale=null;//for refresh of sale info
    return this;
  }

  public SettleOp settleop() {
    return new SettleOp(StringX.NonTrivial(settleop) ? settleop.charAt(0) : new SettleOp(SettleOp.Unknown).Char());
  }

  public boolean isReturn(){
    return transfertype().is(TransferType.Return);
  }

  public boolean isSale(){
    return transfertype().is(TransferType.Sale);
  }

  public boolean isForce(){
    return transfertype().is(TransferType.Force);
  }

  public boolean isAuthOnly(){
    return transfertype().is(TransferType.Authonly);
  }

  public boolean isModify(){
    return transfertype().is(TransferType.Modify);
  }

  public boolean isReversal() {
    return transfertype().is(TransferType.Reversal);
  }

  public boolean changesAnother(){
    return isModify()||isReversal();
  }

  public boolean isQuery() {
    return transfertype().is(TransferType.Query);
  }

  public ActionType actionType() {
    int actioncode=ActionType.unknown;
    switch (transfertype().Value()) {
      case TransferType.Reversal:
      case TransferType.Sale:
      case TransferType.Return:
      case TransferType.Query:
      case TransferType.Modify:
      case TransferType.Authonly:
      case TransferType.Force:
      {
        actioncode=ActionType.payment;
      } break;
    }
    return new ActionType(actioncode);
  }

  /**
   * This means it was stoodin and approved.
   */
  public boolean wasStoodin(){
    return stoodin();
  }

  public boolean wasAuthApproved() {
    return StringX.equalStrings(actioncode, ActionCode.Approved);
  }

  private static final String KNOWN = ActionCode.Approved + ActionCode.Declined + ActionCode.Failed;

  public boolean responded(){
    return StringX.NonTrivial(actioncode) && Bool.flagPresent(actioncode.charAt(0),KNOWN);
  }

  public boolean inProgress(){
    return !responded(); //cand debate foldign wasStoodinApproved() into here.
  }

  public boolean isVoided() {
    return Bool.For(voided);
  }

  public boolean canModify() {
    return canVoid() && (isSale() || isAuthOnly()) && isCredit() &&
        (isGood() || inProgress()); // +++ and notdrawered?
  }

  // not voided, not settled
  public boolean canVoid() {//@todo: reconcile with 'canReverse()'
    // +++ check to make sure this is all we need to know !
    return !isSettled() && /*!StringX.NonTrivial(drawerid) &&*/
        !isReversal() && !isModify() && !isVoided() /* included in isReversed(): && !isVoided()*/
        && isGood(); // doesn't have to be auth'd to be voided
  }

  public boolean modifiesTxn() {//@todo: do usage analysis, try to discard.
    try {
      switch (sale.type.op.Value()) {
        case TransferType.Reversal:
        case TransferType.Modify:
          return true;
        default:
          return false;
      }
    }
    catch (Exception ex) {
      return false; //hideously defective request
    }
  }

  public String whyCannotVoid() {
    if(StringX.NonTrivial(batchid)) {
      return "Batch already closed.  Transaction is final.  Create a new txn instead.";
    } else if(isReversal()) {
      return "Cannot void a reversal transaction.";
    } else if(isModify()) {
      return "Cannot void a modify transaction.";
    } else if(isVoided()) {
      return "Already voided.";
    } else if(!isGood()) {
      return "Was not approved.";
    } else if (StringX.NonTrivial(drawerid)){
      return "Drawer closed."; // +++ is this still a reason?
    } else {
      return "Unknown reason.";
    }
  }
  public String whyCannotModify() {
    if(StringX.NonTrivial(batchid)) {
      return "Batch already closed.  Transaction is final.  Create a new txn instead.";
    } else if(isReversal()) {
      return "Cannot modify a reversal transaction.";
    } else if(isModify()) {
      return "Cannot modify a modify transaction.";
    } else if(isVoided()) {
      return "Voided.";
    } else if(!isGood()) {
      return "Was not approved.";
    } else if (StringX.NonTrivial(drawerid)){
      return "Drawer closed."; // +++ is this still a reason?
    } else if(!StringX.NonTrivial(authendtime)) {
      return "Authorization is still pending.";  // +++ I think this is going to be a problem !!!
    } else {
      return "Unknown reason.";
    }
  }
  public String whyCannotRefund() {
    // +++  @refundbutton - decide the rules !!!
//    if(StringX.NonTrivial(batchid)) {
//      return "Batch closed.";
//    } else if(isReversal()) {
//      return "Cannot modify a reversal transaction.";
//    } else if(isModify()) {
//      return "Cannot modify a modify transaction.";
//    } else if(isVoided()) {
//      return "Voided.";
//    } else if(!isGood()) {
//      return "Was not approved.";
//    } else if (StringX.NonTrivial(drawerid)){
//      return "Drawer closed.";
//    } else if(!StringX.NonTrivial(authendtime)) {
//      return "Authorization is still pending.";  // +++ I think this is going to be a problem !!!
//    } else {
      return "Unknown reason.";
//    }
  }

  public boolean canRefund() {
    // +++  @refundbutton - decide the rules !!!
    return (isCredit() || isGiftCard()) && (isSale() || isReturn());
  }

//  public boolean canRedo() {
//    return isVoided() && isGood() && (isCredit() || isGiftCard()); // only redo valid GC and CR txns
//  }

  public boolean isSettled(){
    return StringX.NonTrivial(batchid);
  }

  public LedgerValue acctBalance() {//was ambiguously named 'netBalance'()
    LedgerValue newone=new LedgerValue(tranmoney);
    newone.parse(acctbalance);
    return newone;
  }

  public boolean authed() {
    return StringX.NonTrivial(authendtime) && (authendtime.charAt(0) == '2');
  }

  public LedgerValue netAuthAmount(){//using trantype, positive for sale, negative for refund
    LedgerValue newone=new LedgerValue(tranmoney);
    newone.parse(amount);
    newone.changeSignIf(isReturn());
    return newone;
  }


  public String cardType(){
    return Fstring.fill(institution, 2, '_');
  }

  public String cardGreeked(){
    return cardGreeked("...", "/");
  }

  public String cardGreeked(String dots, String sep){
    String pt = String.valueOf(paytype().Char());
    return card().accountNumber.Greeked(pt + sep + cardType() + dots);
  }

  public String last4() {
    Fstring lastfor = new Fstring(4);
    return lastfor.righted(cardlast4, 4, '0');
  }

  public void clearAll() {
    setAllTo("");
  }

/**
 * @return true if NOT voided,declined, etc.
 * NOTE: This takes the standin into consideration!
 * For raw auth info, not including standin, just call wasAuthApproved().
 */
  public boolean isGood(){
    return response().isApproved();
  }

  public TxnRow incorporate(AuthResponse auth){
    this.auth=auth;
    if(auth != null) {
      actioncode=auth.action();
      approvalcode=auth.authcode();
      authresponsemsg=auth.message();
      authtracedata=auth.authtracedata();
      authattempt.setAuthResponse(auth.rawresponse.rawValue().getBytes());
      authrrn = auth.authrrn();
      dbg.ERROR("Setting authTraceData to [" + authtracedata + "], authrrn to ["+authrrn+"].");
      acctbalance=(auth.acctBalance != null) ? String.valueOf(auth.acctBalance.Value()) : "";
      setCPSdata(auth.getCPSdata());
      avsrespcode=auth.getAVS();
    }
    return this;
  }

  AuthResponse auth;
  public AuthResponse response(){
    if(auth==null){
      String actioncode = wasStoodin() ? ""+ActionCode.Approved : this.actioncode;
      auth=AuthResponse.mkAuth(actioncode,this.approvalcode,this.authresponsemsg,this.authtracedata,this.authrrn,this.avsrespcode);
    }
    return auth;
  }

  public boolean stoodin() {
    return Bool.For(stoodin);
  }

  public void setStoodin(boolean si) {
    this.stoodin = Bool.toString(si);
  }

  /**
   * store fields from externally generated (client standin) reply INTO record
   * gateway'eds use this field, too
   */
// @@@ %%% +++ stoodin->PMActionCode
  public void incorporateStandin(PaymentReply reply){
//  public void incorporate(PaymentReply reply) {
    this.stan= ActionReply.Stan(reply); // overwrite the db-generated stan with the client-generated one (whatever was printed on the receipt)
    setStoodin(!reply.isGatewayMessage());//this is only a standin if the txn wasn't a fully completed gateway (we are masquerading gateways as standins)
  }

  // this needs to always be called on creation, then called when set to TRUE
  public void setVoided(boolean voidedb) {
    this.voided = Bool.toString(voidedb);
  }

  public void setAuthz(boolean authzb) {
    this.authz = Bool.toString(authzb);
  }

  public void setSettle(boolean settleb) {
    this.settle = Bool.toString(settleb);
  }

  public PaymentRequest extractRequest() {
    dbg.Enter("extractReply");
    PaymentRequest pr = null;
    try {
      Txnid orig=new Txnid(origtxnid);
      pr = PaymentRequest.ResurrectFrom(SaleInfo(), card(), pindata(), TxnReference.New(orig));//+_+ suspect 4th argument
    } catch (Exception ex) {
      dbg.Caught(ex);
      dbg.ERROR("exception Getting Reply From Record");
      pr = PaymentRequest.Null(); // should never be getting any other types!
    } finally {
      dbg.ERROR("pr = " + pr);
      dbg.Exit();
      return pr;
    }
  }

  public RealMoney nominalAmount(){
    RealMoney settleAmt = rawSettleAmount();
    return ((settleAmt.absValue() > 0) ? settleAmt : rawAuthAmount());
  }

  public PaymentReply extractReply() {
    refresh();
    dbg.Enter("extractReply");
    ActionReply caster = null;
    ActionType forcasting = null;
    try {
      forcasting = actionType();
      caster = ActionReply.For(forcasting, refTime(), paytype(), transfertype());
      PaymentReply ar = (PaymentReply)caster;
      if(hasAuthResponse()) {
        ar.setOrigMessage(authattempt.authresponse);
      }
      ar.setAuth(response());
      ar.setReference(tref());//stan and time etc.

      ar.card=card(true); // was inside the next if() block, but I think we need to always do it, otherwise personname is not on receipts

      if ( changesAnother()){
        ar.originalAmount= rawAuthAmount();//settleamount is NOT what we wish to report here
        //reply doesn't yet contain reference number (stan) of original! in fact the trace number is
        //... prinnted from the request, not this reply.
      }
      if (ar.payType.is(PayType.GiftCard)) {
        RealMoney balance= new RealMoney(acctbalance);
        ar.sv.balance.setto(balance);
        if(rawAuthAmount().exceeds(balance)){
          ar.shortage=rawAuthAmount().minus(balance);
        }
      }

      dbg.ERROR("ar = " + ar);
      return ar;
    } catch (ClassCastException cce){
      dbg.ERROR("bad type, pt|tt:"+paytype+transfertype+", typeForCasting="+forcasting+", caster="+caster);
      return PaymentReply.Fubar("bad actiontype in record:"+paytype+transfertype);
    } catch (Exception ex) {
      dbg.Caught(ex);
      return PaymentReply.Fubar("exception Getting Reply From Record");
    } finally {
      dbg.Exit();
    }
  }

  public boolean isManual() {
    return Bool.For(manual);
  }

  private EntrySource mkEntrySource(){
    EntrySource es= new EntrySource();
    es.setto(isManual()?EntrySource.Manual:EntrySource.Machine);
    return es;
  }

  private SaleInfo sale; // AUTH sale info, not SETTLE sale info
  public SaleInfo SaleInfo(){ // AUTH sale info, not SETTLE sale info
      if(sale==null){ // +++ MUTEX !!!
        sale=new SaleInfo();
        sale.stan=stan();
        sale.preapproval=approvalcode;
        sale.type.setto(new PayType(StringX.charAt(paytype, 0)),new TransferType(StringX.charAt(transfertype, 0)),mkEntrySource());
        sale.setMoney(rawAuthAmount());
        sale.setMerchantReferenceInfo(merchref);
        dbg.VERBOSE("saleInfo got:"+sale.toSpam());
      }
      return sale;
  }

  public SettleInfo SettleInfo() {
    return SettleInfo.New(settleop(), rawSettleAmount(), settle());
  }

  public TxnRow setSaleInfo(SaleInfo sale){ // AUTH sale info, mostly
    //stan may get oveerridden by other stuff, leave it off totally here
    setTransferType(sale.type.op);
    setPaytype(sale.type.payby);
    setAuthAmount(sale.Amount());
    setSettleAmount(sale.Amount());//#this assignment is relied upon by Modifies to pass value to db.
    approvalcode = sale.preapproval;
    manual=Bool.toString(sale.type.source.is(EntrySource.Manual));
    merchref=sale.merchantReferenceInfo();
    //cashback not yet implemented
    return this;
  }

  public TxnRow setClientReferences(TxnReference tref){
    terminalid = String.valueOf(tref.termid);//?more better than id of request?
    setStan(tref.httn);
    clientreftime = PayMateDBQueryString.forTrantime(tref.crefTime); // CLIENTREFTIME
    return this;
  }

  public BatchLineItem blightem(){
    BatchLineItem blight=new BatchLineItem();
    blight.date = refTime();
    blight.setSaleInfo(SaleInfo());//money, payment type , transfer type
    blight.setAuth(response());
    blight.setSettleInfo(SettleInfo());
    blight.setServerStoodin(wasStoodin());//until we have separate db columns all si's are posted as server si's
    blight.setVoided(isVoided());
    MSRData card = card(false); // don't get the name; takes too long!
    if(blight.isCard()){
      blight.card= card.accountNumber;
      blight.TypeColData = cardType();
    } else if(blight.isCheck()){
      blight.check= MICRData.fromTrack(card.track(MSRData.T1).Data());//+_+ checkhack
      blight.TypeColData = cardType();
    }
    return blight;
  }

  public void setCPSdata(CPSdata cpsdata) {
    if(cpsdata != null) {
      if(cpsdata instanceof VisaCPSdata) {
        VisaCPSdata vcpsd= (VisaCPSdata)cpsdata;
        this.cpsaci      = vcpsd.cpsaci;
        this.cpsrespcode = vcpsd.cpsrespcode;
        this.cpstxnid    = vcpsd.cpstxnid;
        this.cpsvalcode  = vcpsd.cpsvalcode;
      } else if(cpsdata instanceof MastercardCPSdata) {
        MastercardCPSdata mccpsd= (MastercardCPSdata)cpsdata;
        this.cpstxnid   = mccpsd.referenceNumber;
        this.cpsvalcode = mccpsd.date;
      }
    }
  }

  public CPSdata getCPSdata() {
    VisaCPSdata visacpsdata = new VisaCPSdata(cpsaci, cpsrespcode, cpstxnid, cpsvalcode);
    if(visacpsdata.isValid()) {
      return visacpsdata;
    }
    MastercardCPSdata mccpsdata = new MastercardCPSdata(cpstxnid, cpsvalcode);
    if(mccpsdata.isValid()) {
      return mccpsdata;
    }
    return null;
  }

  public boolean authz() {
    return Bool.For(authz);
  }

  public boolean settle() {
    return Bool.For(settle);
  }

  public int authseq() {
    return StringX.parseInt(authseq);
  }

  /////////////////// AuthAttempt stuff
  // +++++++ Need to make this an ARRAY of attempts.
  // +++ every time an attempt is made to auth, create a new entry in the authattempt table
  // +++ then, when displaying them on the webpage, display the array of them (or links to them).
  // +++ so, maybe we should also make the authattempts page on the website just show links,
  // +++ and each link takes you to an authattempt detail page (with lots of info on it).
  public AuthAttempt authattempt = new AuthAttempt();

  public boolean hasAuthRequest() {
    return authattempt.hasAuthRequest();
  }

  public void setAuthRequest(byte []raw) {
    authattempt.setAuthRequest(raw);
  }

  public boolean hasAuthResponse() {
    return authattempt.hasAuthResponse();
  }

  public void setAuthResponse(byte []raw) {
    authattempt.setAuthResponse(raw);
  }

  //////////////////////////////
  public TxnRow copy() {
    return NewOne(toProperties());
  }

//////////////////////
// ** Look for set and get functions for these fields and use them whenever they exist **
  //candidates
  //these are in the database:
  public String txnid = "";
  public String associateid = "";
  public String authid = "";
  public String settleid = "";
  public String batchid = "";
  public String acctbalance="";
  public String terminalid = "";
  public String drawerid = "";
  public String institution = "";
  public String stoodin = "";
  public String clientreftime = "";
  public String paytype = "";
  public String transfertype = "";
  public String manual = "";
  public String amount = "";
  public String actioncode = "";
  public String authstarttime = "";
  public String authendtime = "";
  public String authresponsemsg = "";
  public String approvalcode = "";
  public String authseq = "";
  public String cpsaci = "";
  public String cpsrespcode = "";
  public String cpstxnid = "";
  public String cpsvalcode = "";
  public String authrrn="";
  public String authtracedata = "";
  public String merchref = "";
  public String origtxnid = "";
  public String stan = "";
  public String transtarttime = "";
  public String tranendtime = "";
  public String voided = "";
  public String settleop = "";
  public String authz = "";
  public String settle = "";
  public String settleamount = "";
  public String avsrespcode = "";
  public String servicecode = "";
  public String cardhash = ""; // Hash (int) of the whole card number
  public String echa = ""; // EncryptedCardHolderAccount = The encryption of the expiry + whole card number
  public String echn = ""; // Name of cardholder, as pulled from track1, encrypted, for receipt printing purposes
  public String cardlast4 = ""; // The last 4 digits of the card number.
  public String signature = "";

  private final String encryptCardImage(MSRData card) {
    return encryptCardImage(card, terminalid());
  }
  public static final String encryptCardImage(MSRData card, Terminalid terminalid) {
    if(! Terminalid.isValid(terminalid)) {
      dbg.ERROR("encryptCardImage() called with invalid terminalid!"); // PANIC! +++
    }
    String value = card.expirationDate.YYmm() + card.accountNumber.toString();
    return DataCrypt.databaseEncode(value.getBytes(), terminalid);
  }

  private final MSRData decryptCardImage(String echa, MSRData ret) {
    return decryptCardImage(echa, ret, terminalid());
  }
  public static final MSRData decryptCardImage(String echa, MSRData ret, Terminalid terminalid) {
    if(! Terminalid.isValid(terminalid)) {
      dbg.ERROR("decryptCardImage() called with invalid txnid!"); // PANIC! +++
    }
    String value = new String(DataCrypt.databaseDecode(echa, terminalid));
    String expiry = StringX.subString(value, 0, 4);
    String cardno = StringX.subString(value, 4);
    if(ret == null) {
      ret = new MSRData();//just so we can debug, doesn't actually get back to caller
    }
    ret.accountNumber.setto(cardno);
    ret.expirationDate.parseYYmm(expiry);
    return ret;
  }

  private String getCardholdername() {
    return DecodeCardholderName(echn, terminalid());
  }
  public static String DecodeCardholderName(String name, Terminalid terminalid) {
    return new String(DataCrypt.databaseDecode(name, terminalid));
  }

  private void setCardholdername(String newname) {
    echn = EncodeCardholderName(newname, terminalid());
  }
  public static String EncodeCardholderName(String name, Terminalid terminalid) {
    return DataCrypt.databaseEncode((name != null) ? name.getBytes() : (byte [])null, terminalid);
  }


  private MSRData card=null;//cahced card info
  public synchronized MSRData card(boolean includeName){ // don't want 2 people doing this at the same time
    if(card==null){
      card=new MSRData();
      decryptCardImage(echa, card); // gets the cardnumber and expiry
      // we don't save tracks anymore
      card.ServiceCode=servicecode;
    }
    if(includeName && StringX.NonTrivial(echn) && ! card.person.isReasonable()) {
      card.person.Parse(getCardholdername());
    }
    return card;
  }
  public MSRData card(){
    return card(false);
  }

  public TxnRow setCard(MSRData card){
    this.card=card;
    echa=encryptCardImage(card); // to save the cardnumber and expiry
    // we don't save tracks anymore
    servicecode= card.ServiceCode;
    setCardholdername(card.person.CompleteName());
    cardhash=String.valueOf(card.cardHash());
    cardlast4=card.accountNumber.last4();
    return this;
  }

  public static final boolean equalCardExp(MSRData c1, MSRData c2) {
    return c1.accountNumber.equals(c2.accountNumber) &&
        c1.expirationDate.equals(c2.expirationDate);
  }

  public final Hancock getSignature() {
    Hancock h = new Hancock();
    EasyCursor ezc = new EasyCursor(signature);
    h.load(ezc);
    return h;
  }

  // returns a String errormessage that isn't null when you might have overwritten an already existing signature!
  public final String setSignature(Hancock sig) {
    // we have to be careful not to write over a good signature with a bad one!
    String newsig = HancockToString(sig);
    if(StringX.NonTrivial(newsig)) {
      if(StringX.NonTrivial(signature)) {
        String msg = "Not overwriting [\n" + signature + "\n] with [\n" + newsig + "\n]!";
        dbg.ERROR(msg);
        return msg;
      } else {
        signature = newsig;
        dbg.VERBOSE("setSignature(): set is good");
      }
    } else {
      dbg.VERBOSE("setSignature(): sig is trivial; not setting");
    }
    return null;
  }

  public final static String HancockToString(Hancock sig) {
    String ret = null;
    if(Hancock.NonTrivial(sig)) {
      EasyCursor ezc = new EasyCursor();
      sig.save(ezc);
      ret = ezc.toString();
    }
    return ret;
  }

  public static TxnRow fakeRecord(){
    TxnRow record = new TxnRow();
    record.transtarttime = UTC.Now().toString(14);
    record.voided = "N";
    record.setClientReferences(TxnReference.New(null/*not used in this function*/,new Terminalid(23),STAN.NewFrom(92010),UTC.Now()));
    record.txnid = "17123499";
    record.merchref = "MR1984Z";
    record.setSaleInfo(SaleInfo.fakeOne());
    record.setCard(MSRData.fakeOne());
    return record;
  }

}
//$Id: TxnRow.java,v 1.142 2004/02/26 16:34:50 mattm Exp $
