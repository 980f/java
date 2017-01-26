package net.paymate.terminalClient.PosSocket;

/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/Formatter.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.18 $
*/
import net.paymate.connection.*;
import net.paymate.util.*;
import net.paymate.terminalClient.*;
import net.paymate.lang.ReflectX;
//for shared code sections, not needed for base functionality
import net.paymate.jpos.data.*;
import net.paymate.data.*;

abstract public class Formatter {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Formatter.class);

  protected ExternalTerminal parent;
  protected boolean gateway;    //true if running in gateway mode

  /**
   * a formatter in gateway mode USUALLY passes messages through.
   * it gets to filter them and when in standin actually does real work
   */
  public boolean isGateway(){
    return gateway;
  }

  abstract public String formatId();
  abstract public boolean onePerConnect();

  ////////////////
  //request
//  static final ActionRequest tobail=new AdminRequest();//anything but null

  private void accessError(String functionname){
    dbg.ERROR("Base class access error: "+ReflectX.shortClassName(this)+'.'+functionname);
  }

  //should be overridden in extension:
  abstract public ActionRequest requestFrom(byte[] line);
//  {
//    accessError("requestFrom");
//    return null;
//  }

  //should be overridden in gateway extensions:
  public ActionRequest openGatewayRequest(byte[] line){
    return requestFrom(line);
  }

  public static final byte[] NullResponse=new byte[0];

  ////////////////
  // repsonse, should be overridden in extension
  abstract public byte[] replyFrom(Action response,boolean timedout);
//  {
//    accessError("replyFrom");
//    return new byte[0];
//  }
  /**
   * response when replyFrom allows an exception to throw
   */
  public byte[] onException(Exception any){
    return NullResponse;
  }
///////////////////
// constrction
  public Formatter setTimeFormat(String tz, String newformat){
    return this;
  }

  public Formatter setParent(ExternalTerminal parent) {
    this.parent=parent;
    return this;
  }

  public Formatter(){
    //nothing to do.
  }

  public static Formatter New(String classclue){
    classclue=ReflectX.stripNetPaymate(classclue);
    if(classclue.endsWith("BatchIndexAsciiFormatter")){
      return new net.paymate.terminalClient.PosSocket.BatchIndexAsciiFormatter();
    }
    if(classclue.endsWith("AsciiFormatter")){
      return new net.paymate.terminalClient.PosSocket.AsciiFormatter();
    }
    if(classclue.endsWith("JumpwareFormatter")){
      return new net.paymate.terminalClient.PosSocket.JumpwareFormatter();
    }
    if(classclue.endsWith("PaymentechUTFormatter")){
      return new net.paymate.terminalClient.PosSocket.paymentech.PaymentechUTFormatter();
    }
    if(classclue.endsWith("GatewayUTFormatter")){
      return new net.paymate.terminalClient.PosSocket.paymentech.GatewayUTFormatter();
    }
    if(classclue.endsWith("HyperFormatter")){
      return new net.paymate.terminalClient.PosSocket.HyperFormatter();
    }
    return null;
  }

}
//$Id: Formatter.java,v 1.18 2004/02/06 18:57:57 mattm Exp $
