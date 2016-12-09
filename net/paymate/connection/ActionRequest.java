package net.paymate.connection;
/**
* Title:        $Source: /cvs/src/net/paymate/connection/ActionRequest.java,v $
* Description:  The request which will be serialized and sent to the server<p>
* Copyright:    2000 PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Id: ActionRequest.java,v 1.60 2001/10/24 04:14:18 mattm Exp $
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
import net.paymate.ISO8583.data.*; //for finaincial requests

public class ActionRequest implements isEasy {
  protected static final ErrorLogStream dbg = new ErrorLogStream(ActionRequest.class.getName(), ErrorLogStream.WARNING);

  ConnectionCallback callback = null;
  public int timeoutseconds=0;//if zero thread will use default

  public ConnectionCallback callback(){
    return callback;
  }

  public ActionRequest setCallback(ConnectionCallback cb){
    callback=cb;
    return this;
  }

  public int retry    =0; //>0 means this_is_a_retry! use previous RRN/STAN

  public ClerkIdInfo clerk=new ClerkIdInfo();

  /**
   * requestIntiationTime is used to match requests and replies.
   *
   */
  public long requestInitiationTime=0;
  public String locallyUniqueId(){
    return String.valueOf(requestInitiationTime);
  }

  public String terminalId = "";
  public String applianceId = "";

  public static final String applianceIdKey  = "applianceId";
  public static final String retryKey                 = "retry";
  public static final String requestInitiationTimeKey = "requestInitiationTime";
  public static final String terminalIdKey            = "terminalId";

  public void save(EasyCursor ezp){
    ezp.setInt    (retryKey,     retry);
    clerk.save(ezp);
    ezp.setLong   (requestInitiationTimeKey, requestInitiationTime);
    ezp.setString (terminalIdKey,            terminalId);
    ezp.setString (applianceIdKey,           applianceId);
  }

  public EasyCursor saveas(String key,EasyCursor ezp){
    ezp.push(key);
    try {
      save(ezp);
    } finally {
      return ezp.pop();
    }
  }

  public void load(EasyCursor ezp){
    retry                 =ezp.getInt    (retryKey);
    clerk.load(ezp);
    requestInitiationTime =ezp.getLong   (requestInitiationTimeKey);
    terminalId            =ezp.getString (terminalIdKey);
    applianceId           =ezp.getString (applianceIdKey);
  }

  // no need to overload these.  They should be fine(al)
  public final EasyCursor toProperties() {
    String classname = this.getClass().getName();
    EasyCursor ezp = new EasyCursor();
    save(ezp); // this *should* call save() on the extended class, right?
    ezp.setString("class", classname);
    ezp.purgeNulls(true /*trivials as well*/);
    return ezp;
  }

  public final String toEasyCursorString() {
    return toProperties().toString();
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

  public static final ActionRequest fromProperties(EasyCursor ezp) {
    ActionRequest ar = null;
    try {
      dbg.Enter("fromProperties");
      String classname = ezp.getString("class");
      if(Safe.NonTrivial(classname)) {//+_+ this check seems to fail
        dbg.VERBOSE("getting class for name '" + classname + "'");
        Class c = Class.forName(classname);
        dbg.VERBOSE("reconstructing a "+c.getName());
        ar = (ActionRequest)(c.newInstance());
        dbg.VERBOSE("loading it ...");
        ar.load(ezp); // this *should* call load() on the extended class, right?
      } else {
        ar = new ActionRequest(); // what else can we do here?
      }
    }
    //  throws ClassNotFoundException, IllegalAccessException, InstantiationException
    catch (Exception e) {
      dbg.Caught(e);
      ar = new ActionRequest(); // what else can we do here?
    } finally {
      dbg.Exit();
    }
    return ar;
  }

  public static final ActionRequest fromStream(InputStream is) {
    return fromProperties((EasyCursor.New(is)));
  }

  public static final ActionRequest fromFile(File f){
    ActionRequest ar=null;
    try {
      ar =ActionRequest.fromStream(new FileInputStream(f));
      if(ar!=null&&ar instanceof canBacklog){
        ((canBacklog) ar).setLocalFile(f);
      }
    } catch(FileNotFoundException nfe){
      dbg.Caught("request from "+f.getAbsolutePath()+ " got:",nfe);
    } finally {
      return ar;
    }
  }

///////////////////

  public String TypeInfo(){
    return Type().Image();
  }

  public static final TransferType OperationType(ActionRequest request){
    try {
      return ((FinancialRequest)request).OperationType();
    } catch(Exception ignored){
      return new TransferType();//will be unknown
    }
  }

  public static final String OperationTypeFlag(ActionRequest request){
    try {
      return ((FinancialRequest)request).sale.type.shortOp();
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
    return false;
  }

  public boolean isReversal() {
    return false;
  }

}
//$Id: ActionRequest.java,v 1.60 2001/10/24 04:14:18 mattm Exp $
