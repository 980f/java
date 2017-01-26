/**
* Title:        Action<p>
* Description:  Storage for an action Request/Reply pair<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: Action.java,v 1.36 2003/12/10 02:16:49 mattm Exp $
*/
package net.paymate.connection;
import net.paymate.util.timer.StopWatch;
import net.paymate.util.*;
import net.paymate.data.*; // id's
import net.paymate.lang.StringX;
import net.paymate.lang.Fstring;

/*
Action mutexing:
add a boolean on Action that marks it as "done".
In all methods that can modify the action check and return immediately if already 'done'.
This requires removing all public members.
*/

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

  private ConnectionCallback callback(){
    return request.callback;
  }

  public void setCallback(ConnectionCallback cb){
    request.callback=cb;
  }

  public void doCallback(){
    doCallback(ErrorLogStream.Null());
  }

  public void doCallback(ErrorLogStream dbg){
    dbg.WARNING("Action.doCallback: type="+TypeInfo()); // +++ @@@ %%% bug leak
    if(request!=null){
      if(request.callback!=null){
        request.callback.ActionReplyReceipt(this);
      } else {
        dbg.ERROR("null callback on "+request.toEasyCursorString());
      }
    } else {
      dbg.ERROR("no callback, null request");
    }
  }

  public String TypeInfo(){
    return request!=null?request.TypeInfo():"NoRequest!";
  }

  public ActionType Type(){
    return request!=null? request.Type(): new ActionType(ActionType.unknown);
  }

  public static final boolean isFinancial(Action act){
    return NonTrivial(act)&&act.request.isFinancial();
  }

  protected final boolean isConcordant(){
    return request.Type().equals(reply.Type());
  }

  public static final boolean isConcordant(Action act){
    return isComplete(act) && act.isConcordant();
  }

  public static final boolean NonTrivial(Action act){
    return act!=null&&act.request!=null;
  }

  public static final boolean isComplete(Action act){
    return NonTrivial(act)&&act.reply!=null;
  }

  /**
   * @return a financial action's reference.
   */
  public static final TxnReference trefOf(Action act){
    if(act!=null && act.reply!=null && act.reply instanceof PaymentReply){
      return ((PaymentReply)act.reply).tref();
    } else {
      return TxnReference.New();
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

  public String historyRec() {//+_+ ancient, purge?
    String stan = ActionReply.Stan(reply);
    String type = ActionRequest.OperationTypeFlag(request);
    return
    Fstring.righted(""+sequence,5,' ')+ ' ' +
    Fstring.righted(stan,5,' ') + ' ' +
    Fstring.fill((request == null) ? "" : type ,2,' ') +
    Fstring.fill((request == null) ? "" : request.Type().Image() ,14,' ') +
    Fstring.fill((request == null) ? "" : StringX.restOfString(DateX.timeStamp(response.startedAt()), 4),16,' ') +
    Fstring.fill((reply == null) ? "" : reply.Type().Image(),14,' ') +
    Fstring.righted((reply == null) ? "" : DateX.millisToSecsPlus(response.millis()),7 ,' ') + ' ' +
    Fstring.fill((reply == null) ? "" : reply.status.Image(),19,' ') ;
  }

}
//$Id: Action.java,v 1.36 2003/12/10 02:16:49 mattm Exp $
