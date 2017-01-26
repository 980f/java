package net.paymate.connection;
/**
* Title:        $Source: /cvs/src/net/paymate/connection/ConnectionClient.java,v $
* Description:  Manages the communications between client and server from the client side (Actions)<p>
* Copyright:    2000 PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: ConnectionClient.java,v 1.135 2004/02/11 00:23:15 andyh Exp $
*/

import net.paymate.Main;
import net.paymate.net.*;
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.lang.StringX;

public class ConnectionClient implements CnxnUser {
  protected Tracer dbg;
  String objectID;
  TerminalInfo termInfo;
  TxnAgent connman;//connection manager /agent /whatever
  public Standin standin;

  public TextList stateDump(){
    TextList dump=new TextList(10);
    dump.add("connection status for ",termInfo.getNickName());
    return TheSinetSocketFactory.Dump(dump);
  }

  public boolean online(){
    return standin.online();//@ptgwsi2@
  }

  public void setStoreInfo(StoreInfo si){
    standin.setStandinRules(si);
  }

  public boolean listWhenOffline(){
    return ! termInfo.isGateway(); //+_+ cheap quick fix for jumpware batch reconciliation, need to add a specific flag for this purpose.
  }

  /** @return true for successful initiation */
  public boolean StartAction(ActionRequest theRequest) {
    Action prepared=connman.Prepare(theRequest);
    if (online()) {//@ptgwsi2@
      dbg.VERBOSE("Posting request:"+theRequest.TypeInfo());
      if(connman.Post(prepared)!=null){
        return true;
      } else {
        return false;
      }
    }
    else { // foreground initaites a new txn while we are in standin
      dbg.VERBOSE("standing in");
      prepared.setReply(standin.whileStandin(prepared.request));
      return connman.Post(prepared)!=null;//will feed the reply back to host on another thread.
    }
  }

  public void processReply(Action action,boolean inBackground){//CnxnUser interface
    dbg.Enter("processReply");
    try {
      if(action.reply.ComFailed()){//may start standing in
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
      } else {//exit standin! but only exactly Success, definitely not on SuccessfullyFaked!
        if(action.reply.status.is(ActionReplyStatus.Success)){
          standin.setStandin(false);
        }
      }
      dbg.mark("callback");
      action.doCallback(dbg);
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

  public String name(){
    return StringX.OnTrivial(""+termInfo.id() ,"txnClient");
  }

  public ConnectionClient(TerminalInfo termInfo){
    this.termInfo = termInfo;
    dbg=new Tracer(this.getClass(),termInfo.id().toString());
    objectID=dbg.myName();
    connman=TxnAgent.New(this,name()+".FORE",termInfo.id());
    standin=new Standin(this);
  }

}
//$Id: ConnectionClient.java,v 1.135 2004/02/11 00:23:15 andyh Exp $
