/**
* Title:        Action<p>
* Description:  Storage for an action Request/Reply pair<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: Action.java,v 1.21 2001/11/17 00:38:33 andyh Exp $
*/
package net.paymate.connection;
import net.paymate.ISO8583.data.TransactionID;
import net.paymate.util.timer.StopWatch;
import net.paymate.util.*;

public class Action {
  static int counter=0;
  public int sequence=0;

  public ActionRequest request = null;
  public ActionReply   reply   = null;

  public boolean hasReply=false;

  //put a stopwatch here, replace the timing pieces of the request and reply
  public StopWatch response=new StopWatch(false);

  public static Action New(ActionRequest request, ActionList list) {
    return new Action(request, list);
  }

  private Action(ActionRequest request, ActionList list) {
    this.request = request;
    sequence=++counter;
    if(list !=null){
      list.register(this);
    }
  }

  public static Action New(ActionRequest request) {
    return new Action(request, null);
  }

  public void setReply(ActionReply rp){
    reply=rp;
    setDone();
  }

  public boolean hasReply(){
    return reply!=null && hasReply;
  }

  public boolean setDone(){
    hasReply= reply!=null;
    return hasReply();
  }

  public static final boolean Requestable(Action action){
    return action!=null&& action.request!=null&& !action.request.Type().is(ActionType.unknown);
  }

  public ConnectionCallback callback(){
    return request.callback;
  }

  public void setCallback(ConnectionCallback cb){
    request.callback=cb;
  }

  public void doCallback(){
    ErrorLogStream.Debug.ERROR("do a callback");
    if(request!=null){
      if(request.callback!=null){
        request.callback.ActionReplyReceipt(this);
      }
      else {
        ErrorLogStream.Debug.ERROR("null callback on "+request.toEasyCursorString());
      }
    }
    else {
      ErrorLogStream.Debug.ERROR("no callback, null request");
    }
  }

  public String TypeInfo(){
    return request!=null?request.TypeInfo():"NotDefined";
  }

  public ActionType Type(){
    return request!=null? request.Type(): new ActionType(ActionType.unknown);
  }

  public static final boolean isFinancial(Action act){
    return NonTrivial(act)&&act.request.isFinancial();
  }

  public static final boolean NonTrivial(Action act){
    return act!=null&&act.request!=null;
  }

  public static final boolean isComplete(Action act){
    return NonTrivial(act)&&act.reply!=null;
  }

  public static final TransactionID tidOf(Action act){
    if(act!=null && act.reply!=null && act.reply instanceof FinancialReply){
      return ((FinancialReply)act.reply).tid;
    } else {
      return TransactionID.Zero();
    }
  }

  public String toString() {
    return "[" + counter + "|" + sequence + "] request: " + request + " ... reply: " + reply;
  }

  /**
  * verbose string for debug
  */
  public static final String historyHeader() {
    return
    Fstring.fill    ("index"        , 6,' ') +
    Fstring.righted ("STAN"         , 5, ' ') + ' ' +
    Fstring.fill    ("T"            , 2,' ') +
    Fstring.fill    ("Request Type" ,14,' ') +
    Fstring.fill    ("Time Sent"    ,16,' ') +
    Fstring.fill    ("Reply Type"   ,14,' ') +
    //      Fstring.fill    ("Time Received",16,' ') +
    Fstring.righted ("Seconds"      ,7 ,' ') + ' ' +
    Fstring.fill    ("Status"       ,19,' ') +
    Fstring.fill    ("Response"     ,18,' ')
    ;
  }

  public String historyRec() {
    String stan = ActionReply.Stan(reply);
    //((reply != null) && (reply instanceof FinancialReply)) ? ((FinancialReply)reply).tid.stan : "";
    String type = ActionRequest.OperationTypeFlag(request);
    //((reply != null) && (request instanceof FinancialRequest)) ? ((FinancialRequest)request).sale.type.shortOp() : "";
    return
    Fstring.righted(""+sequence,5,' ')+ ' ' +
    Fstring.righted(stan,5,Safe.NonTrivial(stan)?'0':' ') + ' ' +
    Fstring.fill((request == null) ? "" : type ,2,' ') +
    Fstring.fill((request == null) ? "" : request.Type().Image() ,14,' ') +
    Fstring.fill((request == null) ? "" : Safe.restOfString(Safe.timeStamp(response.startedAt()), 4),16,' ') +
    Fstring.fill((reply == null) ? "" : reply.Type().Image(),14,' ') +
    //    Fstring.fill((reply == null) ? "" : Safe.restOfString(Safe.timeStamp(response.receivedStamp), 4),16,' ') +
    Fstring.righted((reply == null) ? "" : Safe.millisToSecsPlus(response.millis()),7 ,' ') + ' ' +
    Fstring.fill((reply == null) ? "" : reply.status.Image(),19,' ') +
    Fstring.fill(((reply == null) || (reply.Response == null)) ? "" : reply.Response.completeDescription("-"),18,' ')
    ;
  }

}
//$Id: Action.java,v 1.21 2001/11/17 00:38:33 andyh Exp $
