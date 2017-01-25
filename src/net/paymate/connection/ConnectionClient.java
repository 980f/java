package net.paymate.connection;
/**
* Title:        $Source: /cvs/src/net/paymate/connection/ConnectionClient.java,v $
* Description:  Manages the communications between client and server from the client side (Actions)<p>
* Copyright:    2000 PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: ConnectionClient.java,v 1.118 2001/11/17 00:38:34 andyh Exp $
*/

import net.paymate.Main;
import net.paymate.net.*;
import net.paymate.ISO8583.data.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;

public class ConnectionClient implements CnxnUser {
  protected Tracer dbg;
  String objectID;

  TerminalInfo termInfo;

  public ActionList actionHistory = new ActionList();

  public TextList ActionStatsReport(TextList responses){//4ipterm
    // displays statistics about this running terminal (how many txns, avg time, etc)
    responses.add("");
    responses.add("STATISTICS for " + name() + ":");
    return actionHistory.ActionStatsReport(responses);
  }

  public TextList ActionHistoryReport(TextList responses){
    return actionHistory.ActionHistoryReport(responses);
  }


  /**
  * background components
  */
  public Standin standin;
  public boolean online(){
    return standin.online();
  }

  public void setStoreInfo(StoreInfo si){
    standin.setStandinRules(si);
  }
  ////////////////
  //
  TxnAgent connman;//connection manager /agent /whatever

  /** @return true for successful initiation */
  public boolean StartAction(ActionRequest theRequest) {
    if (online()) {
      dbg.VERBOSE("Posting request:"+theRequest.TypeInfo());
      Action posted=connman.Post(theRequest);
      if(posted!=null){
        actionHistory.register(posted);
        return true;
      } else {
        return false;
      }
    }
    else {
      dbg.VERBOSE("standing in");
      Action prepared=TxnAgent.Prepare(theRequest);
      prepared.setReply(standin.whileStandin(prepared.request));
      return connman.Post(prepared)!=null;
    }
  }

  public void processReply(Action action,boolean inBackground){//CnxnUser interface
//  ErrorLogStream.Debug.ERROR("ccprocrep:"+dbg.myLevel.Image());
//  dbg=ErrorLogStream.Debug; //dammit, regular dbg's level was trashed.
    dbg.Enter("processReply");
    try {
//ErrorLogStream.Debug.ERROR("actions stats");
      actionStats(action);
//ErrorLogStream.Debug.ERROR("test comfailed");
      if(action.reply.ComFailed()){//may start standing in
//ErrorLogStream.Debug.ERROR("com did fail");
        dbg.ERROR("ComFailed:"+action.reply.status.Image());
        //stoodinreplies that fail must be put back into in-memory list.
        if (inBackground) {//then action was an attempt to reduce the backlog of stoodin stuff
          dbg.WARNING("background starts up standin");
          standin.setStandin(true);//failed while reducing backlog
        } else {//enter standin AND fake this one
          action.reply= standin.thisFailed(action);
          if(action.reply==null){//totally hosed
            dbg.ERROR("can't be stood in");
            action.reply=ActionReply.For(action.request).setState(ActionReplyStatus.SocketTimedOut);
          }
          dbg.WARNING("standin reply is "+action.reply.status.Image());
        }
      } else {//exit standin!
        //only exactly Success, definitely not on SuccessfullyFaked!
//  ErrorLogStream.Debug.ERROR("com was ok");
        if(action.reply.status.is(ActionReplyStatus.Success)){
          standin.setStandin(false);
        }
      }
      dbg.mark("callback");
//  ErrorLogStream.Debug.ERROR("before callback");
      action.doCallback();
//  ErrorLogStream.Debug.ERROR("after callback");
      dbg.mark(null);
    } catch (Exception e) { /* catch any callback problems */
      dbg.Caught(e);
    }
    finally {
      dbg.Exit();
    }
  }

  public void Stop() {
    dbg.ERROR("Stopping standin");
    standin.Stop();
    dbg.ERROR("Stopping connman");
    connman.shutdown();
  }


  /**
  * action timing statistics
  * part of run()
  */
  public void actionStats(Action action){
    try {
      // next line is possible overflow error
      actionHistory.avgTimeTotal += (action.response.Stop());//might already be stopped, not a prolbme if so.
      if(action.reply.status.is(ActionReplyStatus.Success)) {//+_+ include fakers?
        actionHistory.successes++;
      } else {
        actionHistory.others++;
      }
    }
    catch(Exception t){
      //unimportant function
    }
  }

  public String name(){
    return Safe.OnTrivial(""+termInfo.id() ,"txnClient");
  }

  public ConnectionClient(TerminalInfo termInfo){
    this.termInfo = termInfo;
    dbg=new Tracer("Connection."+termInfo.id());
    objectID=dbg.myLevel.Name();
    ErrorLogStream.Debug.ERROR("ConnectionClient.init:["+objectID+"]"+dbg.myLevel.Image());
    connman=TxnAgent.New(this,this.name());

    standin=new Standin(this);
  }

}
//$Id: ConnectionClient.java,v 1.118 2001/11/17 00:38:34 andyh Exp $
