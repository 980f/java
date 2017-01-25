package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/TxnAgent.java,v $
* Description:
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.7 $
*/

import net.paymate.util.*;
import net.paymate.net.*;
import net.paymate.util.timer.*;

public class TxnAgent implements QActor,TimeBomb {
  Tracer dbg;
//  static final ErrorLogStream poster=new ErrorLogStream(TxnAgent.class.getName());
  private QAgent runner;//action requests in
  /**
  * NOT private as Standin needs to prepare requests taken from disk.
  */
  public static Action Prepare(ActionRequest aRequest){
    //set universal fields
    aRequest.applianceId=CertifiedSocket.cfg.appleId;
    aRequest.requestInitiationTime= Safe.utcNow(); // @swipetime@
    //end universal fields
    return Action.New(aRequest);
  }

  /**
  * @return true if action goes into queue
  */
  public Action Post(Action anaction){
    dbg.VERBOSE("Post(action)");
    return runner.Post(anaction)? anaction : null;
  }

  /**
  * @return true if action goes into queue
  * use this one when you don't have to track the action.
  */
  public Action Post(ActionRequest request){
    dbg.VERBOSE("Post(request)");
    return Post(Prepare(request));
  }


  public void shutdown() {
    dbg.ERROR("stopping runner");
    runner.Stop();
  }

  public void Stop(){
  //called when above calls runner.Stop()
  //we might consider doing something with a transaction in progress+++
  }

  ///////////////
  //
  private boolean amBackground=false;
  public TxnAgent setBackgroundMode(boolean b){
    amBackground=b;
    dbg.VERBOSE("background set to:"+amBackground);
    runner.config(net.paymate.terminalClient.Appliance.txnHoldoff()*(amBackground?2:1));
    return this;
  }

  ////////////////
  // my socket. The CertifiedSocket class knows about the IP for the one-and-only host.
  CertifiedSocket socket;
  public boolean makeSocket(int timeout) {
    dbg.VERBOSE("makesocket, to="+timeout);
    if(socket==null){
      socket = CertifiedSocket.New(timeout);
    } else {
      socket.reopen(timeout);  // set the user-requested timeout
    }
    return socket.ok();
  }

  void killSocket(){
    if(socket!=null){
      socket.close();
    }
    socket=null;
  }
  // end socket manager
  ///////////////

  ///////////////
  // working parts
  public void runone(Object fromq){
    try {
      Action action=(Action)fromq;
      try{
        runAction(action);
      } catch(Exception oops){
        dbg.Caught(oops);
        action.setReply(ActionReply.For(action.request).setState(ActionReplyStatus.UnknownException));
        action.reply.Errors.Add(dbg.prefix()+oops);
      }
    } catch(ClassCastException cce){
      dbg.ERROR("bad object in queue:"+fromq.getClass().getName());
    }
  }

  ///////////////////////////////////////////////////////////////////////////////
  //@POOL@ httpmessages.

  static URIQuery  uri;

  private void makeUri(){
    if(uri==null){
      ConnSource toSource = new ConnSource(ConnSource.terminalObjects);
      EasyCursor props = new EasyCursor();
      props.setString(ConnSource.class.getName(), toSource.Image());
      uri =  new URIQuery(props);
    }
  }

  protected static String httpMessage(ActionRequest request){
    String requestString = request.toEasyCursorString();
    HTTPMessage message  = new HTTPMessage(new HTTPMethod(HTTPMethod.POST),
    CertifiedSocket.cfg.ipSpec, CertifiedSocket.cfg.UrlPath,  uri, requestString );
    return message.toString();
  }

  private void runAction(Action action){
    try {
      dbg.VERBOSE("nullthereply");
      if(!action.hasReply()) {//for instance a timeout may have injected one.
        dbg.VERBOSE("attemptingto transact");
        action.response.Start();
        //gutsofrun contains the timeout enable, we don't need to check 'timedout' until after it.
        action.setReply(gutsOfRun(action.request));
      } else {
        dbg.VERBOSE("stoodin or faked txn");
      }
    }
    catch (Exception oops) {//request processing blew
      dbg.Caught(oops);
      action.setReply(ActionReply.For(action.request).setState(ActionReplyStatus.UnknownException));
      action.reply.Errors.Add(dbg.prefix()+oops);
    }
    dbg.mark("stop timer");
    action.response.Stop();//we are timing the network response, not the total response
    dbg.mark("procrep");
    cc.processReply(action,amBackground);
    dbg.mark("was processed");
    dbg.mark(null);
  }
  /**
  * try to do an actual client-server interaction
  * this is the part of txnThread's run() that was getting too deeply nested to read,
  * and standin wants just this part.
  */
  ActionReply gutsOfRun(ActionRequest request) {
    ActionReply ar = ActionReply.For(request);
    String httpResponse=null;
    dbg.Enter("gutsOfRun");
    try {
      //      int timeout=(int)Ticks.forSeconds(request.timeoutseconds>0? request.timeoutseconds : cc.fgtimeout);
      int timeout=net.paymate.terminalClient.Appliance.txnHoldoff();
      //start timing out whole process
      readtimeout=Alarmer.New(timeout*2,this); //doulbed so that we don't have a race w. makesocket's timeout
      boolean stepok=true;
      for(int step=0; stepok && !diemotherfuckerdie; ++step){
        switch(step){
          case 0:{
            dbg.mark("makeSocket");
            if(!makeSocket(timeout)){
              ar.setState(ActionReplyStatus.ConnectFailed);
              stepok=false;
            }
          } break;
          case 1:{  // package up the message for streaming
            dbg.mark("asciiIzing");
            String requestMessage = httpMessage(request);
            dbg.mark("sending");
            if(!socket.write(requestMessage)) { // +++ where's this statement's timeout? alh: the timeout is a group timeout named readtimeout
              ar.setState(ActionReplyStatus.ObjectStreamingException);
              stepok=false;
            }
          } break;
          case 2: {// wait for response
            dbg.mark("receiving");

            try {
              httpResponse =  socket.read();//blocking.
            } catch(Exception any){
              dbg.WARNING("While waiting on response Caught:"+any);
              //                  if(Action.Requestable(pending)){
                //      pending.setReply(ActionReply.For(pending.request).setState(ActionReplyStatus.ReplyTimeout));
                //      cc.processReply(pending,amBackground);//#standin3: lost first receipt
              //    }
              return ar.setState(ActionReplyStatus.ReplyTimeout);
            } finally {
              Alarmer.Defuse(readtimeout);
            }
          } break;
          case 3: {//process reply

            if(Safe.NonTrivial(httpResponse)) {
              dbg.mark("extracting");
              // this message has some crap in it.  get rid of it
              int tag = httpResponse.indexOf(InternetMediaType.TEXT_HTML);
              if(tag >=0) {
                tag += InternetMediaType.TEXT_HTML.length();
                httpResponse = httpResponse.substring(tag);
              }
              // unpackage the reply
              EasyCursor reply = new EasyCursor(httpResponse);
              dbg.VERBOSE("Reply's parsed properties:  " + reply);
              ar = ActionReply.fromProperties(reply);
              Alarmer.adjustSystemClock(ar.refTime.getTime());
            } else {
              dbg.ERROR("proclaiming this a timeout");
              ar.setState(ActionReplyStatus.ReplyTimeout);
            }
            stepok=false; //because we are done.
          } break;
        }//end each step
      }//end stepper
      if(ar==null){//probalby can't happen
        ar = new ActionReply(ActionReplyStatus.ObjectStreamingException);
      }
      return ar;
    } catch (Exception others) {
      dbg.Caught(others);
      return new ActionReply(ActionReplyStatus.ObjectStreamingException);
    } finally {
      Alarmer.Defuse(readtimeout);//protection against early exit.
      dbg.Exit();
    }
  }

  /**
  * socket timeout doesn't deal with anything except the read()
  * we will back it up with one of our own timers
  * ------------------
  * this should only fire when 'gutsOfRun()' is active. it executes on its own thread
  * as far as this class is concerned ...
  * but now it is called from other events as well so..
  */

  Alarmum  readtimeout;
  boolean diemotherfuckerdie=false;//our thread likes to live long after it is hopeless

  public void onTimeout(){
    Alarmer.Defuse(readtimeout);//in case some other agent invokes this
    diemotherfuckerdie=true;
    dbg.ERROR("Timeout!");
    killSocket();//will make the blocking read in gutsOfRun() throw ioexception.
  }

  /////////////////
  // construction
  CnxnUser cc;
  private TxnAgent(CnxnUser cc,String name4it){
    runner=QAgent.New(name4it,this);
    this.cc=cc;
    //not our job to finesse priority QAgnet will do so at need.   runner.thread.setPriority(Thread.NORM_PRIORITY);
    dbg=new Tracer(name4it);
    ErrorLogStream.Debug.ERROR("TxnAgentDebuggerNameis:"+dbg.myLevel.Name());
    makeUri();
    runner.Clear();
  }

  public static final TxnAgent New(CnxnUser cc,String name4it){
    return new TxnAgent(cc,name4it);
  }

}
//$Id: TxnAgent.java,v 1.7 2001/11/17 00:38:34 andyh Exp $
