package net.paymate.connection;
/**
* Title:        ActionReply<p>
* Description:  Reply to an ActionRequest<p>
* Copyright:    2000 PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Id: ActionReply.java,v 1.74 2001/10/05 18:47:43 mattm Exp $
*/

import  net.paymate.ISO8583.data.*;
import  net.paymate.util.*;
import  java.util.Date;

public class ActionReply implements isEasy {
  private static final ErrorLogStream dbg = new ErrorLogStream(ActionReply.class.getName());

  static {
    dbg.VERBOSE("ActionReply loaded!");//alh got tired of seeing this in error listings.
  }

  /**
  * official time for the related operation
  */
  public Date refTime;

  /**
  * when things work correctly this is the same as the associated Request's time.
  */
  public String locallyUniqueId(){
    return String.valueOf(refTime.getTime());
  }

  public boolean belongsWith(ActionRequest ar){
    return locallyUniqueId().equals(ar.locallyUniqueId());
  }

  // loading and storing
  protected static final String versionField = "version";
  protected static final String refTimeKey = "refTime";
  protected static final String statusKey = "status";
  protected static final String errorsKey = "Errors";
  public static final String responseKey = "Response";
  protected static final String defaultResponseText = "NOT";//not a valid code

  public ActionReplyStatus status = new ActionReplyStatus(ActionReplyStatus.Unimplemented);

  public TextList Errors = new TextList();  //public to simplify appending from all sorts of places.
  //the following is a stupid thing to do but gets us to the first test
  public ResponseCode Response = new ResponseCode(defaultResponseText);

  public ResponseCode setResponse(String twochar){
    if(Safe.NonTrivial(twochar)&&twochar.length()==2){
      Response= new ResponseCode(twochar);
    } else {// if illegal reset to very illegal value
      Response= new ResponseCode(defaultResponseText);
    }
    return Response;
  }

  public static String revisionCode = SystemRevision.BASECODE; // version control

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

  public static final String Stan(ActionReply reply){
    try {
      return  ((FinancialReply)reply).tid.stan();
    }
    catch (Exception ex) {
      return "";
    }
  }
  /**
  * determines which faults indicate stand in should be done
  * @return true if fault is likely to be due to internet problems that will solve themselves.
  * //v means that alh verified that the item has been reviewed
  */
  public final boolean ComFailed(){
    switch(status.Value()){
      case ActionReplyStatus.Success:  return Response.equals(ResponseCode.AuthorizerDown);

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
      return true;
//things which are probably not re-try-able
      case ActionReplyStatus.GarbledRequest://v
      case ActionReplyStatus.InvalidLogin://v
      case ActionReplyStatus.InvalidTerminal://v
      case ActionReplyStatus.InsufficientPriveleges://v
      case ActionReplyStatus.ServerError://v could be due to configuration, which doesn't fix itself

      default:
      return false;
    }
  }

  ///////////////////////////////////////////
  // must overload these, then call them in the overload
  public void save(EasyCursor ezp){
    dbg.Enter("save");
    try {
      ezp.saveEnum(statusKey, status);
      ezp.setString(responseKey, Response.toString());
      ezp.setTextList(errorsKey, Errors);
      ezp.setString(versionField, revisionCode);
      ezp.setDate(refTimeKey,refTime);
    } catch (Exception t) {
      dbg.Caught(t);
      dbg.Exit();
    }
  }

  public void load(EasyCursor ezp){
  dbg.VERBOSE("status as text:"+ezp.getProperty(statusKey));
    ezp.loadEnum(statusKey, status);
    setResponse(ezp.getString(responseKey));
    revisionCode = ezp.getString(versionField);
    Errors = ezp.getTextList(errorsKey);
    refTime= ezp.getDate(refTimeKey);
  }
  //////////////////////////////////////////////
  // no need to overload these.  They should be fine(al)
  public final EasyCursor toProperties() {
    dbg.Enter("toProperties");
    EasyCursor ezc=null;
    try {
      ezc = new EasyCursor();
      String classname = this.getClass().getName();
      save(ezc); // this *should* call save() on the extended class, right?
      ezc.setString("class", classname);
    } catch (Exception t) {
      //      dbg.Enter("toProperties");
      dbg.Caught(t);
      dbg.Exit();
    } finally {
      return ezc;
    }
  }

  public static final ActionReply rawReplyFor(ActionType type){
    if(type != null) {
      switch (type.Value()) {
        case ActionType.batch:  return BatchReply.New();
        case ActionType.credit: return new CreditReply();
        case ActionType.check:  return new CheckReply();
        case ActionType.clerkLogin: return new LoginReply();
        case ActionType.connection: return new ConnectionReply();
        case ActionType.debit:  return new DebitReply();
        case ActionType.receiptStore: return new ReceiptStoreReply();
        case ActionType.reversal: return new ReversalReply();
      }
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
  * to simplify some code.
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
  public static final ActionReply For(ActionRequest request){
    return For(request.Type(), request.requestInitiationTime);
  }
  public static final ActionReply For(ActionType type, long requestInitiationTime){
    ActionReply newone= rawReplyFor(type);
    newone.refTime=new Date(requestInitiationTime);//utc
    newone.status.setto(ActionReplyStatus.NotInitiated);
    return newone;
  }

  public static final ActionReply fromProperties(EasyCursor p)  {
    ActionReply ar = new ActionReply(ActionReplyStatus.GarbledReply);//preload failure code
    if(p != null) {
      String className="";
      try {
        dbg.Enter("fromProperties");
        className = p.getString("class",ActionReply.class.getName());
        ar = (ActionReply)Class.forName(className).newInstance();
        ar.setState(ActionReplyStatus.GarbledReply).load(p);
      }
      catch (InstantiationException ie){
        dbg.ERROR("No empty constructor  for class named'" + className + "'!");
      }
      catch (ClassNotFoundException cnfe) {
        dbg.ERROR("No class definition for class named'" + className + "'!");
      }
      catch (Exception e) {
        dbg.Caught(e);
      } finally {
        dbg.Exit();
        return ar;
      }
    } else {
      dbg.ERROR("No properties.  Returning bad reply.");
    }
    return ar;
  }

  public boolean revisionCodeMatches() {
    return SystemRevision.match(revisionCode);
  }



  /**
  * this constructor is to be used for gross failures
  */
  public ActionReply(/*ActionReplyStatus*/ int newStatus) {
    this();
    status.setto(newStatus);// = new ActionReplyStatus(newStatus);
  }

  public String toEasyCursorString() {
    EasyCursor ezp = toProperties();
    return ezp.toString();
  }

  public String toString() {
    EasyCursor ezp = toProperties();
    return ezp.asParagraph();
  }

  public ActionReply fubar(String detail){
    Errors.Add("Please call for service. Report:");
    Errors.Add(detail);
    return this;
  }

  public static final ActionReply Fubar(String detail){
    return (new ActionReply()).fubar(detail);
  }

  //////////////////////////////////////////////////////
  //these should be overridden in each extension

  public ActionType Type(){
    return new ActionType(ActionType.unknown);
  }

  public ActionReply() {
    refTime=Safe.Now();//utc
  }

}
//$Id: ActionReply.java,v 1.74 2001/10/05 18:47:43 mattm Exp $
