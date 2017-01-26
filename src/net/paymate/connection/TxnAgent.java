package net.paymate.connection;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/connection/TxnAgent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.54 $
 */

import net.paymate.util.*;
import net.paymate.net.*;
import net.paymate.util.timer.*;
import net.paymate.data.*;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;

/**
 * stages of interaction with server
 */
interface stage {
   int open=0;
   int write=1;
   int read=2;
   int parse=3;
   int number=4;
}

public class TxnAgent implements QActor,TimeBomb {
  Tracer dbg; //for internal thread
  ErrorLogStream dbq; //for user threads
  private static final ErrorLogStream groupdbg=ErrorLogStream.getForClass(TxnAgent.class);
  private QAgent runner;//action requests in
  private Terminalid terminalid; //config item

  private static int globalInstance=0; //for debug naming.
  private SinetSocket socket;

  private int timeout[]=new int[stage.number];

  /* package */ Action Prepare(ActionRequest aRequest){
    aRequest.requestInitiationTime= DateX.UniversalTime();
    return Action.New(aRequest);
  }

  /**
   * @return action IF it goes into queue, else null
   */
  public Action Post(Action anaction){
    dbq.VERBOSE("post(action) "+anaction.TypeInfo());
    return runner.Post(anaction)? anaction : null;// see runone()
  }

  /**
   * @return an action for this request IF it goes into queue, else null
   */
  public Action Post(ActionRequest request){
    dbq.VERBOSE("post(request) " + request.TypeInfo());
    return Post(Prepare(request));
  }

  public void shutdown() {
    dbq.ERROR("stopping runner");
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
    dbq.VERBOSE("background set to:"+amBackground);
    runner.config(net.paymate.terminalClient.Appliance.txnHoldoff()*(amBackground?2:1));
    return this;
  }

  ////////////////
  private boolean makeSocket(SinetHost host,int readtimeout) {
    dbg.VERBOSE("makesocket, socket read timeout will be:"+readtimeout);
    if(socket==null){
      socket=SinetSocket.Create();
    }
    return socket.open(host,readtimeout);
  }

  private void killSocket(){
    socket.close();
  }
  // end socket manager
  ///////////////

  ///////////////
  // working parts
  public void runone(Object fromq){//public for QAgent interface
    ++globalInstance;
    try {
      Action action=(Action)fromq;
      try{
        runAction(action);
      } catch(Exception oops){
        dbg.Caught(oops);
        // @IPFIX@ an exception in the next two lines will blow up to the QAgent! add null checks on action here, among other things
        action.setReply(ActionReply.For(action.request).setState(ActionReplyStatus.UnknownException));
        action.reply.Errors.Add(dbg.prefix()+oops);
      }
    } catch(ClassCastException cce){
      dbg.ERROR("bad object in queue:"+ReflectX.shortClassName(fromq));
    }
  }


  /**
   * blocking write-read of socket to host.
   * this forces one-at-a-time requests so that we don't have to match replies
   * with requests based on content.
   */
  private void runAction(Action action){
    dbg.Enter("GI."+globalInstance);
    try {
      try {
        if(action.hasReply()) {//for instance a timeout may have injected one.
          dbg.VERBOSE("running stoodin txn for a "+action.TypeInfo());
        } else {
          dbg.VERBOSE("attempting to transact a "+action.TypeInfo());
          action.response.Start();//performance timer
          action.setReply(interact(action.request)); //blocks on socket in here.
          //update host/path picker
          if(action.reply.ComFailed()){
            boolean justfailed=TheSinetSocketFactory.ThisFailed(action.request.host);
          } else {
            boolean justworked=TheSinetSocketFactory.ThisWorked(action.request.host);
          }
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
    finally {
      dbg.Exit();
    }
  }



  /**
   * try to do an actual client-server interaction
   * this is the part of txnThread's run() that was getting too deeply nested to read,
   * and standin wants just this part.
   */
  ActionReply interact(ActionRequest request) {
    ActionReply ar = ActionReply.For(request);
    String httpResponse=null;
    dbg.Enter("interact."+globalInstance);
    try {
      //pick host per action type:
      request.pickHost();//assigns applid/name as a side effect.
      request.setTerminalInfo(terminalid);
      int totaltimeout=0;
       totaltimeout+=
      timeout[stage.open]=request.host.creationTimeoutMs;
       totaltimeout+=
      timeout[stage.write]=6000;//@todo:  get from configs? is local system dependent, not host as 100% of message buffers on first write attempt
       totaltimeout+=
      timeout[stage.read]=request.timeout();
      timeout[stage.parse]=Integer.MAX_VALUE;//pratically never time out

      dbg.VERBOSE("Extending caller's timeout to:"+totaltimeout);
      request.callback().extendTimeout(totaltimeout);

      boolean stepok=true;
      abandonRequest.Clear(); //this should be done sooner...put onto request itself. +_+

      StopWatch steptime=new StopWatch();

      for(int step=stage.open; stepok && !abandonRequest.testandclear(); ++step){
        readtimeout= Alarmer.New(timeout[step],this);
        steptime.Start();
        dbg.VERBOSE("Step:"+step+" tout:"+readtimeout.toSpam());//+++ use .mark()
        switch(step){
          case stage.open:{// ensure we have a socket
            dbg.mark("makeSocket");
            if(!makeSocket(request.host,timeout[stage.read]-(int)Ticks.forSeconds(1))){//round down so that socket thread timesout first, if operating correctly.
              dbg.VERBOSE("makeSocket fails");
              ar.setState(ActionReplyStatus.ConnectFailed);
              stepok=false;
            }
          } break;
          case stage.write:{  // package up the message and stream it out
            dbg.mark("sending");
            // System.out.println("txnagent"+Ascii.bracket(request.origMessage()));
            if(!socket.sendRequest(request)) {
              ar.setState(ActionReplyStatus.ObjectStreamingException);
              stepok=false;
            }
          } break;
          case stage.read: {// wait for response
            dbg.mark("receiving");
            try {
              httpResponse =  socket.read();//blocking.
              // if you get nothing here, it would have resulted in keeping a stale socket open, possibly forever, causing it to go offline forever (instant response)
              if(!StringX.NonTrivial(httpResponse)) {
                dbg.WARNING("You got nada; you woulda been offline in the old days");
              }
              stepok=StringX.NonTrivial(httpResponse);
            } catch(Exception any){
              dbg.WARNING("While waiting on response Caught:"+any);
              ar.setState(ActionReplyStatus.ReplyTimeout);
              stepok=false;
            }
          } break;
          case stage.parse: {//process reply
            dbg.mark("parsing");
            ar=socket.parseReply(httpResponse);
            if(ar.status.is(ActionReplyStatus.Success)){//!don't screw with clock if we didn't connect!
              DateX.UniversalTimeIs(ar.refTime);//record skew
 //@todo refresh timeouts              request.host.updateTimeouts(ar);//+_+
            }
            stepok=false; //because we are done.
          } break;
        }//end each step
        Alarmer.Defuse(readtimeout);
        dbg.VERBOSE("Step done:"+step+ " took:"+steptime.Stop());
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
   * this should only fire when 'interact()' is active. it executes on its own thread
   * as far as this class is concerned ...
   * but now it is called from other events as well so..
   */

  Alarmum  readtimeout;
  Bool abandonRequest=new Bool(true);//breaking out of interact()'s for loop

  public void onTimeout(){//timebomb interface
    Alarmer.Defuse(readtimeout);//in case some other agent invokes this
    abandonRequest.set();//
    dbq.ERROR("Timeout!");
    killSocket();//will make the blocking read in interact() throw ioexception.
  }

  /////////////////
  // construction
  private CnxnUser cc;
  private TxnAgent(CnxnUser cc,String name4it,Terminalid terminalid){
    runner=QAgent.New(name4it+".TA",this);
    this.cc=cc;
    this.terminalid=terminalid;
    //not our job to finesse priority QAgnet will do so at need.   runner.thread.setPriority(Thread.NORM_PRIORITY);
    dbg=new Tracer(this.getClass(),name4it);
    dbq=ErrorLogStream.getExtension(this.getClass(), name4it+".Q");//#gets too long if we use classname
    groupdbg.WARNING("TxnAgentDebuggerNames: internal="+dbg.myName()+" external="+dbq.myName());
    runner.Start();//start taking requests
  }

  public static final TxnAgent New(CnxnUser cc,String name4it,Terminalid terminalid){
    return new TxnAgent(cc,name4it,terminalid);
  }

  /**
   * used by standin to create background agent.
   */
  public static TxnAgent Clone(TxnAgent rhs,String name4it){
    return new TxnAgent(rhs.cc,name4it,rhs.terminalid);
  }

}
//$Id: TxnAgent.java,v 1.54 2005/03/02 05:23:06 andyh Exp $
