package net.paymate.connection;
/**
* Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/connection/ActionReply.java,v $
* Description:  Reply to an ActionRequest<p>
* Copyright:    2000 PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Id: ActionReply.java,v 1.126 2005/03/03 05:19:55 andyh Exp $
*/

import net.paymate.data.PayType;
import net.paymate.data.TransferType;
import net.paymate.lang.StringX;
import net.paymate.util.*;

import java.util.Vector;

public class ActionReply implements isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ActionReply.class);
  public Vector hostTimeouts;  //for future use in dynamically tuning the clients to the server load.

  protected LegacyEnum legacyClass=new LegacyEnum();//set when client is of a previous revision and we have to do something wierd when repluying.
  public ActionReply setLegacy(LegacyEnum incoming){
    legacyClass.setto(incoming.Value());
    return this;
  }
  /**
  * official time for the related operation
  */
  protected UTC refTime;
  public UTC refTime(){//overloaded for replies with txnReferences in them
    return refTime;
  }

  protected EasyUrlString origMessage = new EasyUrlString();
  public byte [] origMessage(){
    return origMessage.rawValue().getBytes();
  }

  public EasyUrlString setOrigMessage(byte []raw){
    return origMessage.setrawto(raw);
  }

  public EasyUrlString setOrigMessage(String raw){
    return origMessage.setrawto(raw);
  }

  public EasyUrlString setOrigMessage(EasyUrlString rhs){
    return origMessage=rhs;
  }

  public boolean isGatewayMessage() {
    return EasyUrlString.NonTrivial(origMessage);
  }

  /**
  * when things work correctly this is the same as the associated Request's time.
  */
  public String locallyUniqueId(){
    return String.valueOf(refTime);
  }

  public boolean belongsWith(ActionRequest ar){
    return locallyUniqueId().equals(ar.locallyUniqueId());
  }

  // transport
  private static final String refTimeKey = "refTime";
  private static final String statusKey = "status";
  private static final String errorsKey = "Errors";
  private static final String responseKey = "Response";
  private static final String defaultResponseText = "NOT";//not a valid code
  private static final String origMessageKey = "origMessage";
  private static final String classKey="class";

  public ActionReplyStatus status = new ActionReplyStatus(ActionReplyStatus.Unimplemented);

  public TextList Errors = new TextList();  //public to simplify appending from all sorts of places.
  public static SystemRevision revisionCode = new SystemRevision(); // version control

  //////////////////
  /**
   * this should be renamed "Completed"
   */
  public final boolean Succeeded(){
    return status != null && (status.is(ActionReplyStatus.Success) || status.is(ActionReplyStatus.SuccessfullyFaked));
  }

  public static final boolean Successful(ActionReply ar){
    return ar !=null &&  ar.Succeeded();
  }

  public static final String Stan(ActionReply reply){//used by history dump
    try {
      return  ((PaymentReply)reply).refNum();
    }
    catch (Exception ex) {//deals with class cast exceptions and NPE's
      return "NONE";
    }
  }
  /**
  * determines which faults indicate stand in should be done
  * @return true if fault is likely to be due to internet problems that will solve themselves.
  * //v means that alh verified that the item has been reviewed
  */
  public final boolean ComFailed(){
    switch(status.Value()){
      case ActionReplyStatus.Success:

        return false;//really shouldn't ever get here.
      //       return Response.equals(ResponseCode.AuthorizerDown());
      case ActionReplyStatus.SuccessfullyFaked:
        return false;//gets here on standing in of ???

      //things that a retry with same request might succeed
      case ActionReplyStatus.CertifyFailed: //local clock error?
      case ActionReplyStatus.ConnectFailed:
      case ActionReplyStatus.GarbledReply:
      case ActionReplyStatus.HostTimedOut://v
      case ActionReplyStatus.NotInitiated:
      case ActionReplyStatus.ReplyTimeout://v

      case ActionReplyStatus.UnavailableDueToTxnSystemMaintenance://v
      case ActionReplyStatus.SocketCantInit://config error?
      case ActionReplyStatus.SocketTimedOut://v
      case ActionReplyStatus.ObjectStreamingException://v
      case ActionReplyStatus.DatabaseQueryError://presume database connection is hosed
      return true;
//things which are probably not re-try-able
      case ActionReplyStatus.GarbledRequest://v
      case ActionReplyStatus.InvalidLogin://v
      case ActionReplyStatus.InvalidTerminal://v
      case ActionReplyStatus.InsufficientPriveleges://v
      case ActionReplyStatus.FailureSeeErrors://v
      case ActionReplyStatus.ServerError://v could be due to configuration, which doesn't fix itself
      case ActionReplyStatus.Unimplemented:
        return false; //we would choke the server if we retried these.

      default:
        //presume that we have forgotten to categorize new ones.
      return true; //@500 message@
    }
  }

  ///////////////////////////////////////////
  // must overload these, then call them in the overload
  public void save(EasyCursor ezp){
    dbg.Enter("save");
    try {
      ezp.saveEnum(statusKey, status);
      ezp.setTextList(errorsKey, Errors);
      revisionCode.save(ezp);
      ezp.setUTC(refTimeKey,refTime);
      origMessage.save(ezp);
    } catch (Exception t) {
      dbg.Caught(t);
    } finally {
      dbg.Exit();//this was misplaced, potentially causing infinite stack growth
    }
  }

  public void load(EasyCursor ezp){//expected to be called via super.load() in each extension
    dbg.VERBOSE("status as text:"+ezp.getProperty(statusKey));
    ezp.loadEnum(statusKey, status);
    revisionCode.load(ezp);
    Errors = ezp.getTextList(errorsKey);
    refTime= ezp.getUTC(refTimeKey);
    origMessage.load(ezp);
  }
  //////////////////////////////////////////////
  // no need to overload these.  They should be fine(al)
  public final EasyCursor toProperties() {
    EasyCursor ezc=null;
    try {
      ezc = new EasyCursor();
      ezc.setString(classKey, this.getClass().getName());//moved before save so that legacy can change classname
      save(ezc); // this *should* call save() on the extended class, right?
      //if legacy then change classname
      if(legacyClass.isLegal()){//change type of reply
        ezc.setString(classKey, StringX.replace(legacyClass.toString(), "Request", "Reply"));
      }
    } catch (Exception t) {
      dbg.Caught("in toProperties",t);
    } finally {
      return ezc;
    }
  }

  private static ActionReply forActionCode(int actioncode){
    dbg.VERBOSE("ActionReply.forActionCode() switching on actioncode " + actioncode);
    switch (actioncode) {
//no admin reply's?
//public final static int admin       =0;
//public final static int adminWeb    =1;
//public final static int ipstatupdate=6;
      case ActionType.batch:         return BatchReply.New();
      case ActionType.clerkLogin:    return new LoginReply();
      case ActionType.connection:    return new ConnectionReply();
      case ActionType.gateway:       return new GatewayReply();
      case ActionType.multi:         return new MultiReply();
      case ActionType.payment:       {
        dbg.VERBOSE("Creating PaymentReply!");
        return new PaymentReply();
      }
//      case ActionType.receiptGet:    return new ReceiptGetReply();
      case ActionType.receiptStore:  return new ReceiptStoreReply();//deprecation is OK
      case ActionType.stoodin:       return new StoodinReply();
      case ActionType.store:         return new StoreReply();
      case ActionType.update:        return new UpdateReply();
      default: return ActionReply.Bogus("Unknown Action:"+actioncode);
    }
  }

  public static final ActionReply rawReplyFor(ActionType type){
    if(type != null) {
      dbg.VERBOSE("rawReplyFor type = " + type);
      return forActionCode(type.Value());
    }
    return new ActionReply(ActionReplyStatus.FailureSeeErrors);
  }

  public static final ActionReply rawReplyFor(ActionRequest request){
    if(request!=null){
      return rawReplyFor(request.Type());
    }
    return new ActionReply(ActionReplyStatus.FailureSeeErrors);
  }

  /**
  * to simplify some legacy code. after "For()" was authored.
  */
  public ActionReply setState(int anActionReplyStatus){
    status.setto(anActionReplyStatus);
    return this;
  }

  /**
  * to simplify some legacy code. after "For()" was authored.
  */
  public ActionReply setState(ActionReplyStatus arStat){
    status.setto(arStat.Value());
    return this;
  }

  /**
  * @param success pass/fail to simplify some code, when details of error are irrelevent to POS.
  */
  public ActionReply setState(boolean success){
    status.setto(success ? ActionReplyStatus.Success : ActionReplyStatus.FailureSeeErrors);
    return this;
  }

  public ActionReply Failure(String reason){
    Errors.Add(reason);
    setState(false);
    return this;
  }

   /**
   * @return a new reply object of type matching request. set state as required
   * by client for matching with its request.
   */
  public static final ActionReply For(ActionRequest request) {
    ActionType type = request.Type();
    UTC requestInitiationTime = request.requestInitiationTime;
    PayType pt = null;
    TransferType tt = null;
    if (request.isFinancial()) {
      PaymentRequest pq = (PaymentRequest) request;
      pt = pq.PaymentType();
      tt = pq.OperationType();
    }
    return For(type, requestInitiationTime, pt, tt);
  }

  public static final ActionReply For(ActionType type,
                                      UTC requestInitiationTime,
                                      PayType pt,
                                      TransferType tt) {
    ActionReply newone = rawReplyFor(type);
    newone.refTime = requestInitiationTime;
    newone.status.setto(ActionReplyStatus.NotInitiated);
    if (type.is(ActionType.payment)) {
      PaymentReply pr = (PaymentReply) newone;
      pr.payType.setto(pt);
      pr.transferType.setto(tt);
    }
    return newone;
  }

  private static final int ActionTypeForClassNamed(String classname){//replaces use of reflection to aid compilation of client
    //strip net.paymate.connection to make the switch reasonable
    LegacyEnum arf=LegacyEnum.ForClass(StringX.replace(classname, "Reply", "Request"));
    if(arf.isLegal()) {
      return ActionType.payment;
    }
    classname=StringX.afterLastDot(classname);

    if(classname.equals("BatchReply"))         return ActionType.batch;
    if(classname.equals("ConnectionReply"))    return ActionType.connection;
    if(classname.equals("GatewayReply"))       return ActionType.gateway;
    if(classname.equals("LoginReply"))         return ActionType.clerkLogin;
    if(classname.equals("MultiReply"))         return ActionType.multi;
    if(classname.equals("PaymentReply"))       return ActionType.payment;
//    if(classname.equals("ReceiptGetReply"))    return ActionType.receiptGet;
    if(classname.equals("ReceiptStoreReply"))  return ActionType.receiptStore;
    if(classname.equals("StoodinReply"))       return ActionType.stoodin;
    if(classname.equals("StoreReply"))         return ActionType.store;
    if(classname.equals("UpdateReply"))        return ActionType.update;
    //legacy types, in case we update while we have standins:
//    if(classname.equals("PaymentReply"))       return ActionType.payment;
    //no! let them fail, too much work to do the testing required!
    return ActionType.admin;
  }

  public static final ActionReply fromProperties(EasyCursor ezc)  {
    ActionReply ar=null;
    try {
      String className = ezc.getString(classKey);
      int ac=ActionTypeForClassNamed(className);
      ar=forActionCode(ac);
      ar.setState(ActionReplyStatus.GarbledReply).load(ezc);
    }
    catch (Exception e) {
      dbg.Caught("in fromProperties", e);
    }
    finally {
      if(ar==null){
        ar = new ActionReply(ActionReplyStatus.GarbledReply); //preload failure code
      }
      return ar;
    }
  }

  public boolean revisionCodeMatches() {
    return revisionCode.isCurrent();
  }

  /**
  * this constructor is to be used for gross failures
  */
  public ActionReply(/*ActionReplyStatus*/ int newStatus) {
    this();
    status.setto(newStatus);
  }

  public String toEasyCursorString() {
    EasyCursor ezp = toProperties();
    return String.valueOf(ezp);
  }

  public String toString() {
    return toProperties().asParagraph();
  }

  public ActionReply fubar(String detail){
    Errors.Add("Please call for service. Report:");
    Errors.Add(detail);
    status.setto(ActionReplyStatus.FailureSeeErrors);
    return this;
  }

  public static final PaymentReply Fubar(String detail){
    return (PaymentReply)(new PaymentReply()).fubar(detail);
  }

  public static final ActionReply Bogus(String detail){
    return new ActionReply().fubar(detail);
  }

  //////////////////////////////////////////////////////
  //these should be overridden in each extension

  public ActionType Type(){
    return new ActionType();
  }

  //nullargs constructor is required by fromProperties()
  public ActionReply() {
    refTime=UTC.Now();//utc
  }

}
//$Id: ActionReply.java,v 1.126 2005/03/03 05:19:55 andyh Exp $
