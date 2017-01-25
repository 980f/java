package net.paymate.connection;

/**
 * Title:
 * Description:  storage part of standin at client, for execution part @see Standin
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: StoodinRequest.java,v 1.14 2001/10/02 17:06:37 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.terminalClient.Receipt;
import net.paymate.jpos.data.*;

import java.io.File;

public class StoodinRequest extends AdminRequest implements canBacklog, isEasy  {
  private static final ErrorLogStream dbg = new ErrorLogStream(StoodinRequest.class.getName());


  public ActionType Type(){
    return new ActionType(ActionType.stoodin);
  }

  public boolean fromHuman(){
    return false;
  }

//class Action not used as these guys get here from way different locations than
//the normal request's action.
  FinancialRequest  request;//the stoodin request
  FinancialReply    reply; //the rpely that standin generated
  ReversalReply   voidme=null; //in case it is voided while in standin.
  String        recordID; //4debug.
//implements canBacklog
  File localFile;
  public File setLocalFile(File f){ return localFile=f;}
  public File getLocalFile()      { return localFile;}
//for use by backlog

  /**
   * we will have stored action data and receipt in separate files.
   * we use this constructor when fetching from disk to be sent to server.
   */
  StoodinRequest() {
//    this.request=null;
//    this.reply  =null;
//    this.receipt=null;
  }

  /**
   * request starts the standin
   */
  public StoodinRequest add(FinancialRequest request){
    this.request=request;
    return this;
  }
  /**
   * reply comes after a delay to simulate going over the net and back
   */
  public StoodinRequest add(FinancialReply reply){
    this.reply=reply;
    return this;
  }

  final static char fs=File.separatorChar;//too tedious to type! +++ put into Safe
/**
 * @return filename linked to embedded tid
 */
  public String filename(){
    try {
      recordID=reply.tid.image(fs)+fs+request.Amount().Value();
      return recordID;
    } catch (ClassCastException oops){
      dbg.Caught(oops);
      return "";//which should get erjected at a higher layer
    }
  }

/**
 * used to make up stan and auth.
 */
  public int hashCode(){
    try {
      FinancialRequest frek=(FinancialRequest) request;
      String arf=  frek.Amount().toString();//plain cents, no punctuation.
      if (frek instanceof CardRequest ) {
        MSRData card=((CardRequest) frek).card;
        arf+="."+card.accountNumber.Image()+"."+card.expirationDate.YYmm();
      } else if (frek instanceof CheckRequest) {
        MICRData check= ((CheckRequest) frek).check;
        arf+="."+check.toSpam();
      }
      return arf.hashCode();
    } catch (ClassCastException oops){
      return -1;
    }
  }

  public int  stanner(int hash){
    hash/=1000;//drop millis
    return hash ^ (hash/100000);
  }

  public String authful(int hash){
    return Fstring.centered(Integer.toString(hash),6,'0');
  }

//////////////////////////////////////
// transport
  private static final String requestKey="request";
  private static final String replyKey="reply";
  private static final String receiptKey="receipt";
  public static final String recordIDKey="recordID";


  /**
   * the connectionClient uses this to package object for transport
   */
  public void save(EasyCursor ezc){
    super.save(ezc);
    ezc.setString(recordIDKey,recordID);
    ezc.push(requestKey);
    ezc.addMore(request.toProperties());
    ezc.setKey(replyKey);
    ezc.addMore(reply.toProperties());
    ezc.pop();
  }

  /**
   * server will call this to process the standin
   */
  public void load(EasyCursor ezc){
    super.load(ezc);
    recordID = ezc.getString(recordIDKey);
    ezc.push(requestKey);
    request = (FinancialRequest)ActionRequest.fromProperties(ezc);
    ezc.setKey(replyKey);
    reply = (FinancialReply)ActionReply.fromProperties(ezc);
    ezc.pop();
    //if reboot caused the clock to roll back...do SOMETHING:
    if(requestInitiationTime<request.requestInitiationTime){
      requestInitiationTime=request.requestInitiationTime+1;
    }
  }

  public static final StoodinRequest New(EasyCursor ezc){
    StoodinRequest newone=new StoodinRequest();
    newone.load(ezc);
    return newone;
  }

}
//$Id: StoodinRequest.java,v 1.14 2001/10/02 17:06:37 mattm Exp $