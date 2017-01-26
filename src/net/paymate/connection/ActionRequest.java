package net.paymate.connection;
/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ActionRequest.java,v $
 * Description:  The request which will be serialized and sent to the server<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: ActionRequest.java,v 1.102 2004/02/07 22:35:44 mattm Exp $
 *
 * NOTE!!!!      When making new request types remember to overload Type()
 *               If you extend this class, you MUST duplicate the copy code
 *               like is in FakeActionRequest.
 *               You also MUST either create NO constructors (so it uses the default)
 *               or create a default one "public WhateverRequest()".
 */

import net.paymate.util.*;
import java.util.Date;
import java.io.*;
import net.paymate.data.*; // id's
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;

public class ActionRequest implements isEasy {
  protected static final ErrorLogStream dbg = ErrorLogStream.getForClass(ActionRequest.class);

  ConnectionCallback callback = null;//client only

  public ConnectionCallback callback(){
    return callback;
  }

  public ActionRequest setCallback(ConnectionCallback cb){
    callback=cb;
    return this;
  }

  public int retry    =0; //>0 means this_is_a_retry! for debugging how many attempts something takes ONLY

  SystemRevision revision=new SystemRevision();//local system's revision
  public ClerkIdInfo clerk=new ClerkIdInfo();

  /**
   * requestIntiationTime is used to match requests and replies.
   */
  public UTC requestInitiationTime;

  public String locallyUniqueId(){
    return String.valueOf(requestInitiationTime);
  }

  /*private*/
  public /*for authmanager*/ String applname = "";
  /*private*/
  public /*for authmanager*/ Terminalid terminalid = new Terminalid();
  SinetHost host;//records who it was sent to.

  public LegacyEnum legacy = new LegacyEnum() ; // overwrite for FinancialRequest legacy
  public void pickHost(){
    host= TheSinetSocketFactory.GetHost(this instanceof UpdateRequest);//true argument==get primary even if it is offline.
    applname=host.appleId();
  }
  ///////////////

  protected EasyUrlString origMessage = new EasyUrlString();

  public byte [] origMessage(){
    return origMessage.rawValue().getBytes();
  }


  public EasyUrlString setOrigMessage(byte []raw){
    return origMessage.setrawto(raw);
  }

  public boolean isGatewayMessage() {
    return EasyUrlString.NonTrivial(origMessage);
  }

  public ActionRequest setApplianceInfo(String applname){
    this.applname=  applname;
    return this;
  }

  public ActionRequest setTerminalInfo(Terminalid terminalid){
    this.terminalid= terminalid;
    return this;
  }

  public int terminalId(){
    return terminalid.value();
  }

  private static final String retryKey                 = "retry";
  private static final String requestInitiationTimeKey = "requestInitiationTime";
  protected static final String applianceKey = "applianceId";//legacy
  private static final String terminalKey = "terminalId";//legacy
  private static final String origMessageKey = "origMessage";
  private static final String classKey = "class";


  public void save(EasyCursor ezp){
    revision.save(ezp);
    ezp.setInt    (retryKey,     retry);
    clerk.save(ezp);
    ezp.setUTC(requestInitiationTimeKey, requestInitiationTime);
    if(Terminalid.isValid(terminalid)){ //some request don't have an id
      terminalid.save(ezp);
    }
    ezp.setString(applianceKey, applname);
    origMessage.save(ezp);
  }

  public void load(EasyCursor ezp){
    revision.load(ezp);//keep this first as it may affect subsequent interpretation of the ezp.
    retry                 =ezp.getInt    (retryKey);
    clerk.load(ezp);
    requestInitiationTime=ezp.getUTC(requestInitiationTimeKey);
    terminalid.load(ezp);
    applname = ezp.getString(applianceKey);
    origMessage.load(ezp);
  }

  // no need to overload these.  They should be fine(al)
  public final EasyCursor toProperties() {
    EasyCursor ezp = new EasyCursor();
    save(ezp); // this *should* call save() on the extended class, right?
    ezp.setString(classKey, this.getClass().getName());
    ezp.purgeTrivials();
    return ezp;
  }

  public final String toEasyCursorString() {
    return String.valueOf(toProperties());
  }

  public final String toString() {
    return toProperties().asParagraph();
  }

  public static final ActionRequest fromProperties(String buried,EasyCursor ezp) {
    ezp.push(buried);
    try {
      return ActionRequest.fromProperties(ezp);
    } finally {
      ezp.pop();
    }
  }

  /**
   * removed use of reflection to aid obfuscator.
   * @param classname
   * @return
   */
  private static final ActionRequest New(String classname){
    classname=ReflectX.stripNetPaymate(classname);
    if (classname.endsWith("PaymentRequest")) {
      return new PaymentRequest();
    }
    if (classname.endsWith("AdminRequest")) {
      return new AdminRequest();
    }
    if (classname.endsWith("AdminWebRequest")) {
      return new AdminWebRequest();
    }
    if (classname.endsWith("BatchRequest")) {
      return new BatchRequest();
    }
//  illegal to instatiate this one reflectively.
//    if (classname.endsWith("BypassRequest")) {
//      return ...;
//    }
    if (classname.endsWith("ConnectionRequest")) {
      return new ConnectionRequest();
    }
    if (classname.endsWith("GatewayRequest")) {
      return new GatewayRequest();
    }
    if (classname.endsWith("IPStatusRequest")) {
      return new IPStatusRequest();
    }
    if (classname.endsWith("LoginRequest")) {
      return new LoginRequest();
    }
//    if (classname.endsWith("ReceiptGetRequest")) {
//      return new ReceiptGetRequest();
//    }
    if (classname.endsWith("ReceiptStoreRequest")) {
      return new ReceiptStoreRequest();
    }
    if (classname.endsWith("SettlementRequest")) {
      return new SettlementRequest();
    }
    if (classname.endsWith("StoodinRequest")) {
      return new StoodinRequest();
    }
    if (classname.endsWith("StoreRequest")) {
      return new StoreRequest();
    }
    if (classname.endsWith("UpdateRequest")) {
      return new UpdateRequest();
    }
    return new ActionRequest();
  }

  public static final ActionRequest fromProperties(EasyCursor ezp) {
    try {
      dbg.Enter("fromProperties");
      String classname = ezp.getString(classKey);
      if(StringX.NonTrivial(classname)) {//+_+ this check seems to fail
        dbg.VERBOSE("getting class for name '" + classname + "'");
        ActionRequest ar = null;
        LegacyEnum arf=LegacyEnum.ForClass(classname);
dbg.VERBOSE("arf for "+classname+" is " + arf.toString());
        if(arf.isLegal()) {//all members of LegacyEnum are now represented by one class:
          ar = new PaymentRequest();
          ar.legacy=arf;
        } else {
          ar = ActionRequest.New(classname);
        }
        dbg.VERBOSE("loading it ...");
        ar.load(ezp); // this calls load() on the extended class, which then needs to super.load();
        return ar; //success
      } else {
        dbg.VERBOSE("fromProperties(): classname is null!");
        return new ActionRequest(); // what else can we do here? +++ be sure it is set to failure
      }
    }
    //  throws ClassNotFoundException, IllegalAccessException, InstantiationException
    catch (Exception e) {
      dbg.Caught(e);
      return new ActionRequest(); // what else can we do here?
    } finally {
      dbg.Exit();
    }
  }

  public static final ActionRequest fromStream(InputStream is) {
    return fromProperties((EasyCursor.New(is)));
  }

///////////////////
  /**
   * @returns seconds for response timeout. Time from request to server 'til response complete.
   * if no host then instant timeout.
   */
  public int timeout(){
    return host!=null? host.timeoutFor(Type()): 0;
  }

  public String TypeInfo(){
    return Type().Image();
  }

  public static final String OperationTypeFlag(ActionRequest request){
    try {
      return ((PaymentRequest )request).sale.type.shortOp();
    } catch(Exception ignored){
      return "";
    }
  }

  /**
   * true if clerk member should be nontrivial and in database
   */
  public boolean fromHuman(){
    return true;
  }

//////////////////////////
// these should always be overridden, this class is concrete to deal with faulty
// request generation logic.
  public ActionType Type(){
    return new ActionType(ActionType.unknown);
  }

  public boolean isFinancial() {
    return Type().is(ActionType.payment);
  }

  /**
   * @return whether this is an automated request.
   */
  public boolean isAutomatedRequest(){
    // :( Admin used for pings +_+
    switch(this.Type().Value()) {
      // err on the side of security
      case ActionType.unknown:
      default:
      // these are not auto
      case ActionType.adminWeb:
      case ActionType.receiptGet:
      case ActionType.batch:
      case ActionType.clerkLogin:
      case ActionType.payment:
      case ActionType.store: {
        return false;
      }
      // these are auto
      case ActionType.multi:
      case ActionType.gateway:
      case ActionType.receiptStore:
      case ActionType.stoodin:
      case ActionType.admin:
      case ActionType.update:
      case ActionType.connection:
      case ActionType.ipstatupdate: {
        return true;
      }
    }

  }

}

//$Id: ActionRequest.java,v 1.102 2004/02/07 22:35:44 mattm Exp $