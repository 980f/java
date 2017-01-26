package net.paymate.connection;

/**
     * Title:        $Source: /cvs/src/net/paymate/connection/StoodinRequest.java,v $
 * Description:  storage part of standin at client, for execution part @see Standin.java
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version $Revision: 1.42 $
 * @todo: use ezc.getObject stuff in save() and load()
 */

import net.paymate.util.*;
import net.paymate.terminalClient.Receipt;
import net.paymate.jpos.data.*;
import net.paymate.data.*; // STAN

import java.io.File;
import net.paymate.awtx.RealMoney;

public class StoodinRequest extends AdminRequest implements canBacklog, isEasy {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoodinRequest.class);

//////////////
// sub class attributes
  public ActionType Type() {
    return new ActionType(ActionType.stoodin);
  }

  public boolean fromHuman() {
    return false;
  }

//class Action not used as these guys get here from way different locations than
//the normal request's action.
  PaymentRequest request; //the stoodin request
  PaymentReply reply; //the rpely that standin generated
  PaymentReply voidme = null; //in case it is voided while in standin.
//  PaymentRequest modifier = null;

  public PaymentRequest request(){
    return request; //the stoodin request
  }
  public PaymentReply reply(){
    return reply; //the rpely that standin generated
  }
  public PaymentReply voidme(){
    return voidme; //in case it is voided while in standin.
  }
  public boolean isVoided(){
    return voidme!=null;
  }

  public boolean isModifier(){
    return request!=null&&request.isModify();
  }
  /**
   *
   * @return money amount, ignoring voided.
   */
  public RealMoney amount(){
    return //modifier!=null? modifier.Amount() :
        request!=null? request.Amount(): RealMoney.Zero();
  }

  String recordID; //4debug.
  public String recordID(){
    return recordID;
  }
//////////////////
//implements canBacklog
  File localFile;
  public File setLocalFile(File f) {
    return localFile = f;
  }

  public File getLocalFile() {
    return localFile;
  }

//for use by backlog

/**
 * we will have stored action data and receipt in separate files.
 * we use this constructor when fetching from disk to be sent to server.
 * @unwise  - only used by loadFromProperties
 */
  public StoodinRequest() {

  }

  /**
   * request starts the standin
   */
  public StoodinRequest add(PaymentRequest request) {
    this.request = request;
    return this;
  }

  /**
   * reply comes after a delay to simulate going over the net and back
   */
  public StoodinRequest add(PaymentReply reply) {
    this.reply = reply;
    return this;
  }

  final static char fs = File.separatorChar; //so verbose that without this names would be hard to read
  /**
   * @return filename linked to embedded tid
   * @xtodo: when terminal trustsstan we must use stan rather than refTime to file objects in order to detect duplicates attempted into standin.
   * no, let them duplicate in standin and let the server resolve them. too difficult to deal with at this layer.
   */
  public String filename() {
    try {
      recordID = String.valueOf(reply.refTime) + '.' + request.Amount().Value();
      return recordID;
    }
    catch (ClassCastException oops) {
      dbg.Caught(oops);
      return ""; //which should get rejected at a higher layer
    }
    finally {
      dbg.WARNING("filename for this standin is:" + recordID);
    }
  }



//////////////////////////////////////
// transport
  private static final String requestKey = "request";
  private static final String replyKey = "reply";
  private static final String voiderKey = "voider";
  private static final String modifierKey = "modifier";

  private static final String receiptKey = "receipt";
  public static final String recordIDKey = "recordID";

  /**
   * the connectionClient uses this to package object for transport
   */
  public void save(EasyCursor ezc) {
    if (request != null) {
      super.save(ezc);
      ezc.setString(recordIDKey, recordID);
      ezc.push(requestKey);
      try {
        ezc.addMore(request.toProperties());
        if (reply != null) {
          ezc.setKey(replyKey);
          ezc.addMore(reply.toProperties());
          if (voidme != null) {
            ezc.setKey(voiderKey);
            ezc.addMore(voidme.toProperties());
          }
//          if(modifier!=null){
//            ezc.setKey(modifierKey);
//            ezc.addMore(modifier.toProperties());
//          }
        }
      }
      finally {
        ezc.pop();
      }
    }
  }

  /**
   * server will call this to process the standin
   */
  public void load(EasyCursor ezc) {
    super.load(ezc);
    recordID = ezc.getString(recordIDKey);
    for (int i = 4; i-- > 0; ) { //many items any one of which might croak
      try {
        switch (i) {
//          case 3:{
//            ezc.push(modifierKey);
//            modifier = (PaymentRequest) ActionRequest.fromProperties(ezc);
//          } break;
          case 2: {
            ezc.push(requestKey);
            request = (PaymentRequest) ActionRequest.fromProperties(ezc);
          }
          break;
          case 1: {
            ezc.push(replyKey);
            reply = (PaymentReply) ActionReply.fromProperties(ezc);
          }
          break;
          case 0: {
            ezc.push(voiderKey);
            voidme = (PaymentReply) ActionReply.fromProperties(ezc); // blows if no voider, which then gets set to null; no problem
          }
          break;
        }
      }
      catch (ClassCastException ex) {
        switch (i) {
//          case 3: {
//            dbg.Caught("bad stored modifier Loading FromProperties", ex);
//            modifier = null;
//          } break;
          case 2: {
            dbg.Caught("bad stored request Loading FromProperties", ex);
            request = null;
          }
          break;
          case 1: {
            dbg.Caught("bad stored reply Loading FromProperties", ex);
            reply = null;
          }
          break;
          case 0: {
            dbg.VERBOSE("no void, Loading FromProperties,"+ ex);
            voidme = null;
          }
          break;
        }
      }
      finally {
        ezc.pop();
      }
    } // endfor
    //if reboot caused the clock to roll back...do SOMETHING:
    requestInitiationTime.ensureAfter(request.requestInitiationTime);
  }

  public static final StoodinRequest New(EasyCursor ezc) {
    StoodinRequest newone = new StoodinRequest();
    newone.load(ezc);
    return newone;
  }

  public static final StoodinRequest New(PaymentRequest frek,PaymentReply fry) {
    return new StoodinRequest().add(frek).add(fry);
  }

}
//$Id: StoodinRequest.java,v 1.42 2004/01/28 06:26:14 mattm Exp $