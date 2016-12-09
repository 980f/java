package net.paymate.terminalClient;

/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/Appliance.java,v $
* Description:  a thing that has terminals
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author       PayMate.net
* @version $Id: Appliance.java,v 1.55 2001/11/17 00:38:35 andyh Exp $
*/

import net.paymate.ISO8583.data.*;
import net.paymate.connection.*;
import net.paymate.net.*;
import net.paymate.*;
import net.paymate.util.*;
import java.util.*;
import java.io.*;

public class Appliance implements CnxnUser, ConnectionCallback, Runnable, AtExit {
  static final ErrorLogStream dbg=new ErrorLogStream(Appliance.class.getName());
  ConnectionReply myInfo;//we don't bother unpacking this structure.
  ApplianceOptions opts=new ApplianceOptions();

  static Appliance sole=null;
  boolean haveConfigged=false;
  boolean runningcached=false;

  public String id(){
    return myInfo!=null?myInfo.applianceID://after a connection
    commonhost!=null? commonhost.appleId://before a connection
    "";//before object is valid
  }

  protected Vector /*<PosTerminal>*/ unit=new Vector();

  Constants commonhost;
  //  ConnectionClient hostlink;
  TxnAgent hostlink;

  public static int txnHoldoff(){//tix
    return sole.opts.txnHoldoff;
  }

  boolean killed=false;//pinger

  Thread pinger;

  //////////////////////
  // legacy builder
  PosTerminal addTerminal(TerminalInfo termInfo){
    dbg.VERBOSE("adding terminal "+termInfo.toSpam());
    PosTerminal newone=new PosTerminal(termInfo,this);
    unit.add(newone);
    dbg.VERBOSE("starting terminal "+newone.termInfo.getNickName());
    newone.Start(Main.props());
    return newone;
  }

  ////////////////////////
  // backup for standin-on-boot
  File cacheFile(){
    return Main.LocalFile(commonhost.appleId,"properties");
  }

  /**
   * save a connectionreply for use by restore()
   */
  private void cache(ConnectionReply tosave){
    try {
      dbg.VERBOSE("saving to disk  file:"+cacheFile().getAbsolutePath());
      EasyCursor ezp = tosave.toProperties();
      FileOutputStream cache=new FileOutputStream(cacheFile());
      ezp.store(cache,"created by server at:"+tosave.refTime);
      Safe.Close(cache);
    } catch(IOException oops){
      dbg.ERROR("Failed to cache appliance config:"+oops);
    }
  }
  /**
  * get last good Login from disk
  */
  private void restore(){
    try {
      dbg.WARNING("restoring from disk file:"+cacheFile().getAbsolutePath());
      EasyCursor fromdisk=new EasyCursor();
      fromdisk.Load(new FileInputStream(cacheFile()));
      ////////
      //the recognition of identical hardware use has to be done at easyCursor level
      processReply(((ConnectionReply) ActionReply.fromProperties(fromdisk)));
      runningcached=true;
    } catch(IOException ignored){
      dbg.ERROR("Failed to reload appliance from disk");
    }
  }

  ////////////////////////
  // normal login stuff

  /**
  * find a terminal in our list of existing hardware
  */
  PosTerminal terminal(TerminalInfo ti){
    for(int i=unit.size();i-->0;){
      PosTerminal possible=(PosTerminal) unit.elementAt(i);
      if(possible.termInfo.id() == ti.id()){
        return possible;
      }
    }
    return null;
  }

  private PosTerminal terminal(int i){
    return (PosTerminal) unit.elementAt(i);
  }

  static int shutcode=ExitCode.Halt;
  public static void terminalIsDown(PosTerminal pto,int exitcode){
    dbg.WARNING("terminalIsDown:"+pto.id()+" exitcode:"+exitcode);
    sole.unit.remove(pto);
//let applaince always rule as to what exit is    shutcode=exitcode; //last one to stop wins the fight over what to report.
    if(sole.unit.size()<=0){
      sole.killPinging(); //--- stdExit was supposed to call this.
      Main.stdExit(shutcode);
    }
  }

  void makeTerminals(){
    dbg.Enter("makeTerminals");
    try {
      //per appliance=6 java+appliance pinger+alarmer
      //per terminal=PosTerm+its connection+its standin+one per serial port
      //each ipterminal connect adds 3, they should cause a dump.
      //      AppStatus.threadThrashLimit=8+6*myInfo.numTerminals();
      for(int i=myInfo.numTerminals();i-->0;){
        //create a posterm
        TerminalInfo termfo=myInfo.terminal(i);
        PosTerminal posterm=addTerminal(termfo);
        if(posterm!=null){
          dbg.VERBOSE("configuring store "+posterm.id());
          posterm.Post(myInfo.cfg);
        } else {
          dbg.ERROR("Failed to construct "+termfo.id());
        }
      }

    }
    finally {
      dbg.Exit();
    }
  }

  void updateTerminals(){
    dbg.Enter("updateTerminals");
    try {
      for(int i=myInfo.numTerminals();i-->0;){
        //create a posterm
        TerminalInfo termfo=myInfo.terminal(i);
        PosTerminal posterm=this.terminal(termfo);
        if(posterm!=null){
          dbg.VERBOSE("re configuring storeinfo of "+posterm.id());
          posterm.Post(myInfo.cfg);
        } else {
          dbg.ERROR("Failed to update "+termfo.id());
        }
      }
    }
    finally {
      dbg.Exit();
    }
  }

  synchronized void processReply(ConnectionReply reply){//synched' with BroadCastTerminalCommand
    dbg.Enter("processReply");
    boolean haveInited=myInfo!=null;
    try {
      if(haveInited && !myInfo.compliesWith(reply)){//NPE's if not same number of terminals
        cache(reply);
        shutcode=ExitCode.WarmBoot;
        BroadCastTerminalCommand(TerminalCommand.Shutdown);//%%%@@@ will trash xactions in progress!
      } else {
        cache(myInfo=reply);
        if(haveInited){
          dbg.VERBOSE("updating config");
          updateTerminals();
        } else {
          dbg.VERBOSE("processing new config");
          makeTerminals();
        }
      }
    } finally {
      dbg.Exit();
    }
  }

  void bePinging(){
    killed=false;
    if(pinger.isAlive()){
      pinger.interrupt();
    } else {
      pinger.start();
    }
  }

  public void AtExit(){
    killPinging();
  }

  public boolean IsDown() {
    return !pinger.isAlive();
  }

  void killPinging(){
    killed=true;
    pinger.interrupt();
  }

  /**
  * standin "on" means "be offline."
  */
  private void setAllStandin(boolean on){
    BroadCastTerminalCommand(on?TerminalCommand.GoOffline:TerminalCommand.GoOnline);
  }

  private StandinStatus trueCount() {
    StandinStatus ss = new StandinStatus(0, 0);
    try {
      for(int i=unit.size();i-->0;){
        StandinStatus oneterm = terminal(i).Standin().status();
        ss.addTxnCount(oneterm.txnBacklog());
        ss.addRcpCount(oneterm.rcpBacklog());
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return ss;
    }
  }

  public void processReply(Action action,boolean bgnd){
    action.doCallback();//should get us to ActionReplyReceipt
  }

  /**
  * appliance logged in, (re) configure all the terminals.
  * @@@ check for multiple/reentrancy problems.
  */
  public void ActionReplyReceipt(Action action) {
    dbg.Enter("ActionReplyReceipt");
    try {
      if(action.Type().is(ActionType.connection)){
        dbg.VERBOSE("was connecting");
        if(action.reply.Succeeded()){
          processReply((ConnectionReply) action.reply);
          haveConfigged=true;
          if(TextList.NonTrivial(action.reply.Errors)){
            //          Notify();
          }
        } else {
          dbg.ERROR("Connecting Failed, running from cache");
          if(!haveConfigged&&!runningcached){
            restore();
          }
          setAllStandin(true); //early start of standin on all of the terminals
        }
      }
      else if(action.reply.Type().is(ActionType.update)){
        if(action.reply.Succeeded()){
          UpdateReply upper=(UpdateReply) action.reply;
          opts= upper.opt;
          if(upper.deathCode!=0){
            dbg.ERROR("Server Forcing Exit:"+upper.deathCode);
            Main.stdExit(upper.deathCode);
          }
          setAllStandin(false); //early exit of standin on all of the terminals
        }
      }
      else {
        dbg.ERROR("unexpected reply:"+action.reply.Type().Image());
      }
    } finally {
      dbg.Exit();
    }
  }

  /**
  * we only allow ourselves to use the strongest available
  */
  private static final String suites[] = {
    "SSL_RSA_WITH_RC4_128_MD5",
  };

  Appliance(Constants host) {
    dbg.VERBOSE("new appliance:"+host.appleId);
    sole=this;
    killed=false;
    commonhost=host;
    CertifiedSocket.Initialize(commonhost,suites);//just stores the info, doesn't actually talk over the net
    //    hostlink=new ConnectionClient((new TerminalInfo(commonhost.appleId).setNickName("appliance")));
    hostlink=TxnAgent.New(this,"appliance");
    //    if(!hostlink.ok()){
      //      dbg.ERROR("Connection did NOT init");
    //    }
    pinger=new Thread(this,"AppliancePinger");//#NORM_PRIORITY
    pinger.setDaemon(true);//false replaces keepalive from main --- didn't work.
    Main.OnExit(this);//should come back and kill the pinger
  }

  void Start(){
    //    EasyCursor timeouts=ConnectionClient.cfgTimeouts(Main.props());
    //    dbg.VERBOSE("timeouts set to: "+timeouts.asParagraph());
//    restore();//so that we can talk to hardware, if the same hardware as last boot.
    bePinging();//we now use the pinger thread for all transactions
  }

  void makeRequest(boolean startup){
    UpdateRequest ar;
    if(startup){
      ar= new ConnectionRequest();
    } else {
      StandinStatus ss = trueCount();
      ar= new UpdateRequest(ss.txnBacklog(), ss.rcpBacklog());
    }
    ar.opt=opts; //fixes disconnect between reported and acted upon interval.
    dbg.VERBOSE("makeRequest:"+ar.TypeInfo());
    hostlink.Post(ar.setCallback(this));
  }

  public void run(){
    while(!killed){
      try {
        dbg.VERBOSE("Configged:"+haveConfigged);
        makeRequest(!haveConfigged);
        dbg.VERBOSE("SleepingFor:"+opts.period);
        ThreadX.sleepFor(opts.period);
      } catch (Throwable e) {//inarun()
        dbg.Caught(e);
      }
    }
  }

  ////////////////
  public static void BroadCast(TerminalCommand tc){
    for(int i=sole.unit.size();i-->0;){
      ((PosTerminal) sole.unit.elementAt(i)).Post(tc);
    }
  }

  public synchronized static void BroadCastTerminalCommand(int tcvalue){//synch'ed with processReply
    TerminalCommand tc=new TerminalCommand(tcvalue);
    if(tc.isLegal()){
      BroadCast(tc);
    }
  }

}
//$Id: Appliance.java,v 1.55 2001/11/17 00:38:35 andyh Exp $
