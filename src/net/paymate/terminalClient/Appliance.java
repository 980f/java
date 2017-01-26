package net.paymate.terminalClient;

/**
* Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/terminalClient/Appliance.java,v $
* Description:  a collection of terminals and their common data
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author       PayMate.net
* @version $Id: Appliance.java,v 1.125 2005/03/23 03:42:45 andyh Exp $
* @todo: look at piggyBack component of updateReply and do something with it.
*/

import net.paymate.ExitCode;
import net.paymate.Main;
import net.paymate.connection.*;
import net.paymate.data.TerminalInfo;
import net.paymate.io.IOX;
import net.paymate.lang.*;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.util.timer.Alarmer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.util.WeakHashMap;

public class Appliance implements LineServerUser, CnxnUser, ConnectionCallback, Runnable, AtExit {
  static ErrorLogStream dbg;
  ConnectionReply myInfo;//we don't bother unpacking this structure.
  ApplianceOptions opts;//=new ApplianceOptions();
  public String appleid;

  static Appliance the=null;
  boolean haveConfigged=false;
  boolean runningcached=false;

  public static String id(){
    return the.appleid;//after a connection
  }

  public static StoreConfig StoreInfo(){
    return the.myInfo.cfg;
  }

  ///////////////////////////

  private Vector /*<BaseTerminal>*/ unit=new Vector();

  TxnAgent hostlink;

  public static int txnHoldoff(){//tix
    return (the!=null&&the.opts!=null)? the.opts.txnHoldoff():40;//+_+ migrate to SinetHost options
  }

  private boolean killed=false;//pinger

  Thread pinger;

  //////////////////////
  // legacy builder
  private static EasyCursor hacks;
  private BaseTerminal addTerminal(TerminalInfo termInfo){
    dbg.VERBOSE("adding terminal "+termInfo.toSpam());
    BaseTerminal newone= BaseTerminal.instantiate(termInfo);
    if(ObjectX.NonTrivial(newone)){
      unit.add(newone);
      dbg.VERBOSE("starting terminal "+newone.termInfo.getNickName());
      if(hacks==null){
        hacks=net.paymate.Main.Properties(PosTerminal.class);//not BaseTerminal, for legacy reasons.
      }
      hacks.push(termInfo.getNickName());
      try {
        newone.Start(hacks);
      }
      finally {
        hacks.pop();
      }
    } else {
      dbg.ERROR(termInfo.equipmentlist.toSpam());
      dbg.ERROR("Failed to instantiate terminal!");
    }
    return newone;
  }

  ////////////////////////
  // backup for standin-on-boot
  private File cacheFile(){
    return Main.LocalFile(TheSinetSocketFactory.PreferredHost().appleId(),"properties");
  }

  /**
  * save a connectionreply for use by restore()
  */
  private void cache(ConnectionReply tosave){
    try {
      dbg.VERBOSE("saving to disk  file:"+cacheFile().getAbsolutePath());
      EasyCursor ezp = tosave.toProperties();
      FileOutputStream cache=new FileOutputStream(cacheFile());
      ezp.store(cache,"created by server at:"+tosave.refTime());
      IOX.Close(cache);
    } catch(IOException oops){
      dbg.ERROR("Failed to cache appliance config:"+oops);
    }
  }
  /**
  * get last good Login from disk
  */
  private void restore(){
    try {
        File file = cacheFile();
        dbg.WARNING("restoring from disk file:"+file.getAbsolutePath());
      EasyCursor fromdisk=new EasyCursor();
      fromdisk.Load(new FileInputStream(cacheFile()));
      ////////
      //the recognition of identical hardware use has to be done at easyCursor level
      processReply(((ConnectionReply) ActionReply.fromProperties(fromdisk)));
      runningcached=true;
    } catch(IOException ioex){
      dbg.ERROR("Failed to reload appliance from disk:"+ioex);
      Main.stdExit(ExitCode.LaunchError);//as if JVM failed to load Main()
    } catch(ClassCastException cce){
      dbg.ERROR("Failed to reload appliance from disk:"+cce);
      Main.stdExit(ExitCode.LaunchError);//as if JVM failed to load Main()
    }
  }

  ////////////////////////
  // normal login stuff

  /**
  * find a terminal in our list of existing hardware
  */
  private BaseTerminal terminal(TerminalInfo ti){
    for(int i=unit.size();i-->0;){
      BaseTerminal possible=(BaseTerminal) unit.elementAt(i);
      if(possible.termInfo.id() == ti.id()){
        return possible;
      }
    }
    return null;
  }

  private BaseTerminal terminal(int i){
    return (BaseTerminal) unit.elementAt(i);
  }

  private static int shutcode=ExitCode.Halt;
  public static void terminalIsDown(BaseTerminal pto){
    dbg.WARNING("terminalIsDown:"+pto.id());
    the.unit.remove(pto);
    if(the.unit.size()<=0){
      the.killPinging(); //--- stdExit was supposed to call this.
      Main.stdExit(shutcode);
    }
  }

  private void makeTerminals(){
    dbg.Enter("makeTerminals");
    try {
      //per appliance=6 java+appliance pinger+alarmer
      //per terminal=PosTerm+its connection+its standin+one per serial port
      //each ipterminal connect adds 3, they should cause a dump.
      //      AppStatus.threadThrashLimit=8+6*myInfo.numTerminals();
      for(int i=myInfo.numTerminals();i-->0;){
        //create a posterm
        TerminalInfo termfo=myInfo.terminal(i);

        BaseTerminal posterm=addTerminal(termfo);
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

  private void updateTerminals(){
    dbg.Enter("updateTerminals");
    try {
      for(int i=myInfo.numTerminals();i-->0;){
        //create a posterm
        TerminalInfo termfo=myInfo.terminal(i);
        BaseTerminal posterm=this.terminal(termfo);
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

  private synchronized void processReply(ConnectionReply reply){//synched' with BroadCastTerminalCommand
    dbg.Enter("processReply");

    boolean haveInited=myInfo!=null;
    try {
      if(haveInited && !myInfo.compliesWith(reply)){//NPE's if not same number of terminals
        cache(reply);
        orderlyExit(ExitCode.WarmBoot); //%%% will trash xactions in progress!
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

  private void bePinging(){
    killed=false;
    if(pinger.isAlive()){
      pinger.interrupt();
    } else {
    //up ourselves so that we start pinger thread before calling thread returns.
//still wasn't good enough to keep program alive      pinger.setPriority(Thread.currentThread().getPriority()+1);
      pinger.start();
    }
  }

  public void AtExit(){
    killPinging();
  }

  public boolean IsDown() {
    return !pinger.isAlive();
  }

  private void killPinging(){
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
        BaseTerminal t=terminal(i);
        if(t instanceof PosTerminal){
          StandinStatus oneterm = ((PosTerminal)t).Standin().status();
          ss.addTxnCount(oneterm.txnBacklog());
          ss.addRcpCount(oneterm.rcpBacklog());
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return ss;
    }
  }

  public void processReply(Action action,boolean bgnd){//implements CnxnUser
    action.doCallback(dbg);//should get us to ActionReplyReceipt
  }

  /**
  * appliance logged in, (re) configure all the terminals.
  * %%% check for multiple/reentrancy problems.
  */
  public void ActionReplyReceipt(Action action) {//implements ConnectionCallback
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
          setAllStandin(false); //had to wait until terminals existed!
        } else {
          dbg.ERROR("Connecting Failed, already running from cache:"+runningcached);
          //+++txnHost.reset(); //renegotiate SSL
          if(!haveConfigged&&!runningcached){
            restore();
          } else {
            dbg.ERROR("Running on:"+(haveConfigged?"recent":"cached")+" info");
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
          if(!opts.validatePeriod(opts.txnHoldoff()*2)) {
            dbg.ERROR("Server gave back a bad period:"+opts.period());
          }

          setAllStandin(false); //early exit of standin on all of the terminals
          for(int i=upper.piggyBack.getCount();i-->0;){
            //ActionReply also=upper.piggyBack.itemAt(i);
            //todo: do something with these!!!
            //find associated terminal
            //post it.
          }
        } else {
          //_+++txnHost.reset(); //renegotiate SSL
        }
      }
      else {
        dbg.ERROR("unexpected reply:"+action.reply.Type().Image());
      }
    } finally {
      dbg.Exit();
    }
  }
  public void extendTimeout(int millis){}//ConnectionCallback interface

  public static void Initialize(){
    the =new Appliance();//container for the terminals
    the.Start();//will read cached config
  }

  private Appliance() {
    dbg=ErrorLogStream.getForClass(Appliance.class);
    opts=new ApplianceOptions();
    killed=false;
    TheSinetSocketFactory.Initialize(Main.props());//just stores the info, doesn't actually talk over the net

    hostlink=TxnAgent.New(this,"appliance",null/*no termimalid*/);
    pinger=new Thread(this,"AppliancePinger");//#NORM_PRIORITY
    pinger.setDaemon(true);//false replaces keepalive from main --- didn't work.
    Main.OnExit(this);//should come back and kill the pinger
  }

  LineServer ipTerminal=null;
  public boolean onePerConnect(){
    return false;
  }
  private void Start(){
    //--wait until first network failure  restore();//so that we can talk to hardware, if the same hardware as last boot.
    EasyCursor ezc=Main.props("debug");
    ipTerminal= SocketLineServer.New(ezc.getInt("port",49852),this,true,SocketLineServer.NOLINEIDLETIMEOUT);
    if(ezc.getBoolean("byip",false)) {
      dbg.WARNING("starting terminal server");
      ipTerminal.Start();
    } else {
      dbg.VERBOSE("terminal server not started");
    }

    bePinging();//we now use the pinger thread for all transactions
  }

  private void makeRequest(boolean startup){
    UpdateRequest ar;
    if(startup){
      ar= new ConnectionRequest();
    } else {
      StandinStatus ss = trueCount();
      ar= UpdateRequest.Generate(ss.txnBacklog(), ss.rcpBacklog());
    }
    ar.opt=opts; //#fixes disconnect between reported and acted upon interval.
    dbg.VERBOSE("makeRequest:"+ar.TypeInfo());
    hostlink.Post(ar.setCallback(this));
  }

  LogSwitch gcme;//4debug access. else would be inside inside run()
  public void run(){//pinger Thread
    int runcounter=0;
    gcme=LogSwitch.getFor(Appliance.class,"gc").setLevel(LogSwitch.OFF);//verbose==do it
    while(!killed){
      try {
        dbg.VERBOSE("run:"+ ++runcounter + " Configged?"+haveConfigged);
        makeRequest(!haveConfigged);
        Main.gc(gcme);
        dbg.VERBOSE("SleepingFor:"+opts.period());
        ThreadX.sleepFor(opts.period());
      } catch (Throwable e) {//#inarun()
        dbg.Caught("Run:"+runcounter,e);
      }
    }
  }

  ////////////////
  private static BaseTerminal terminalNumber(int i){
     return (BaseTerminal) the.unit.elementAt(i);
  }

  /**
   * send something to every terminal except one, usually the one that is calling this function.
   */
  public static void BroadCast(Object tc,BaseTerminal excluded){
    for(int i=the.unit.size();i-->0;){
      BaseTerminal posterm=terminalNumber(i);
      if(posterm!=excluded){//#object compare , should be able to deal wtih excluded being null
        posterm.Post(tc);
      }
    }
  }

  public static void BroadCast(TerminalCommand tc){
    dbg.WARNING("BroadCasting:"+tc.Image());
    if(tc.isLegal()){
      for(int i=the.unit.size();i-->0;){
        ((BaseTerminal) the.unit.elementAt(i)).Post(tc);
      }
    }
  }

  public synchronized static void BroadCastTerminalCommand(int tcvalue){//synch'ed with processReply
    TerminalCommand tc=new TerminalCommand(tcvalue);
    BroadCast(tc);
  }

  public static String doMagic(MajicEvent spell){
    dbg.VERBOSE("doMagic"+spell.Image());
    return doCommand(new TerminalCommand(spell.Value()));
  }

  public static String doCommand(TerminalCommand spell){
    switch(spell.Value()){
    //commands directed at appliance class
      case TerminalCommand.SERVICEMODE: {
        BroadCast(spell);
        return "AppMonStarted:"+the.ipTerminal.Start();
      }
      case TerminalCommand.Normal: {
        BroadCast(spell);
        the.ipTerminal.Stop();
      } return "AppMonStopRequested";
    //whole appliance commands:
      case TerminalCommand.Restart:     return the.orderlyExit(ExitCode.WarmBoot);
      case TerminalCommand.Shutdown:    return the.orderlyExit(ExitCode.Halt);
      case TerminalCommand.StatusUp:    return the.orderlyExit(ExitCode.StatusOn);
      case TerminalCommand.StatusDown:  return the.orderlyExit(ExitCode.StatusOff);
      case TerminalCommand.GetConfig:   {
        if(rufrunner("getconfig")){//if new way then
          return the.orderlyExit(ExitCode.seven);//just restart java
        } else {
          return the.orderlyExit(ExitCode.UpgradeApp);//old way
        }
      } //break;
      case TerminalCommand.GetProgram:  return the.orderlyExit(ExitCode.UpgradeAll);
    //else one terminal is gigging all the others:
      default: BroadCast(spell); return "TerminalCommand";
    }
  }

  /////////////////////
  private String [] zeta = {
    "eatdeceasedbeef",
    "101bytethebitbucket010",
  };

  static final String miniprompt = " $ ";
//  static final String CRLF = "\r\n";
  static final String prompt = Ascii.CRLF + miniprompt;
  static final int promptSize = miniprompt.length();
  static final String theBye = "bye";
  static final String theLast = "\0x1B";//or PC up arrow esc sequence

  static final String theQuit = "quit";
  static final String theExit = "exit";
//  static final String theHistory = "<ESC>";

  String logonspew= "Type '" + theBye + "' to quit the session but leave program running."+prompt ;
//  + "Type '" + theHistory + "' to get a list of the commands used thus far."+CRLF;

  private boolean loggedin=false;
  private TextList args= new TextList();//last command

  private String orderlyExit(int exitCode){
    shutcode= exitCode;
    BroadCastTerminalCommand(TerminalCommand.Shutdown);
    return new ExitCode(exitCode).Image();
  }

  static final void dumpEnumOptions(TrueEnum enum, String title, TextList tl) {
    tl.add(title + " options:");
//    tl.appendMore(enum.dump("  "));
    tl.add(enum.dump("  ", "")); // @andy@ +++ this will now have bugs !!!
  }

  static final boolean validEnumOption(TrueEnum enum, String parameter, String cmd, TextList responses) {
    boolean stat = enum.isLegal();
    if(!stat) {
      // check to see if it could be numeric instead
      int pint = StringX.parseInt(parameter);
      if(pint == 0){//parseInt returns 0 for garbage in
        if( ! parameter.equals("0") ){
          pint = TrueEnum.Invalid();
        }
      }
      enum.setto(pint);
      stat = enum.isLegal();
    }
    if(!stat) {
      if(!parameter.equals("?") && !parameter.equals("")) {
        responses.add("Option '" + parameter + "' not valid for command '" + cmd + "'.");
      }
      dumpEnumOptions(enum, cmd, responses);
    }
    return stat;
  }

  static synchronized void logLevels(TextList responses) {
    responses.appendMore(LogSwitch.listLevels());
  }

  private String logwad(TextList args){
    String cmd=args.itemAt(0);  //patching into legacy code
    String param0=args.itemAt(1);
    String param1=args.itemAt(2);
    TextList responses=new TextList();

    boolean all = param0.equalsIgnoreCase("ALL") || param0.equals("*") || param0.equals("") ;
    boolean justsave= param0.equalsIgnoreCase("SAVE");
    if(justsave){
      Main.saveLogging(param1);
      return "Saved settings to <"+param1+"> in temp directory, copy to logcontrol.properties to use them";
    }
    //Vector debuggers = LogSwitch.Sorted();
    if(StringX.NonTrivial(param1)) { // we are setting
      LogLevelEnum level = new LogLevelEnum(param1);
      if(validEnumOption(level, param1, cmd, responses)) {
        if(all) {
          LogSwitch.SetAll(level);
        } else {
          LogSwitch.setOne(param0, level);
        }
      } else {
        responses.add("Not a valid option " + cmd);
      }
    }
    if(all) {
      logLevels(responses);
    } else {
      if(LogSwitch.exists(param0)) {
        responses.add("Log switch not found: " + param0);
      } else {
        LogSwitch ls = LogSwitch.getFor(param0);
        responses.add(ls.Name() + ": " + ls.Level());
      }
    }
    return responses.asParagraph();
  }

  WeakHashMap shells=new WeakHashMap(); //background shell


  String processArgs(TextList args){
    //arg[0] is AppMonOpcode
    AppMonOpcode op= (AppMonOpcode) TrueEnum.makeEnum(AppMonOpcode.Prop,args.itemAt(0));
    boolean startit= args.itemAt(1).equalsIgnoreCase("start");

    switch (op.Value()) {
      default: return TextList.enumAsMenu(op).foldAt(20).asParagraph();
      case AppMonOpcode. shell:{// shell {start|stop} [port] portdefault 32123
        int shellport= StringX.parseInt(args.itemAt(2));
        Integer key=new Integer(shellport);

        if(startit){
          if(shellport<1024){
            shellport=32123;
          }
          TelnetService shell=  new TelnetService(shellport);
          if(shell!=null && shell.Start()){
            shells.put(key,shell);
            return "shell started at port:"+shellport;
          } else {
            return "couldn't start shell at port:"+shellport;
          }
        } else {
          //try to find shell for given port
          TelnetService shell=  (TelnetService) shells.remove(key);
          if(shell!=null){
            shell.Stop();
            return "shell stopped at port:"+key;
          } else {
            return "no shell to stop at "+key;
          }
        }
      } //break;


      case AppMonOpcode. terminalOp:{
        TerminalCommand termop= (TerminalCommand) TrueEnum.makeEnum(TerminalCommand.Prop,args.itemAt(1));
        if(termop.isLegal()){
          doCommand(termop);
          return "did:"+termop.Image();
        } else {
          return "Illegal Terminal Option\n try:"+TextList.enumAsMenu(termop);
        }
      }
//      identify:{
//        BroadCastTerminalCommand(TerminalCommand.Identify);
//        return "check peripherals for terminal name";
//      }

//      case AppMonOpcode. reconnect:    return orderlyExit(ExitCode.WarmBoot);
      case AppMonOpcode. statusclient: return orderlyExit(startit ? ExitCode.StatusOn : ExitCode.StatusOff);
//      case AppMonOpcode. reload      : return orderlyExit(ExitCode.UpgradeApp);
//      case AppMonOpcode. reinstall   : return orderlyExit(ExitCode.UpgradeAll);
//      case AppMonOpcode. shutdown    : return orderlyExit(ExitCode.Halt);

      case AppMonOpcode. standin     :{
      //+++ add a status keyword and show standin counts.
        TerminalCommand tc= new TerminalCommand(startit ? TerminalCommand.GoOffline: TerminalCommand.GoOnline);
        BroadCast(tc);
        return "Issued:"+tc.Image();
      }

      case AppMonOpcode. logging     :{
        return logwad(args);
      }

      case AppMonOpcode. alarms      :{
        String flags=StringX.OnTrivial(args.itemAt(1),"d");
        if(Bool.flagPresent('g',flags)){
          Alarmer.Check(); //refresh all
        }
        if(Bool.flagPresent('d',flags)){
          return Alarmer.dump().asParagraph();
        }
      }
      return "Op:"+op.Image()+" understood but not implemented.";
    }
  }

  public byte[] onReception(byte[] line){//return response to "line"
    String humph=Ascii.cooked(line).trim();
    if(!loggedin) {
      for(int i = zeta.length; i-->0;) {
        if(humph.equalsIgnoreCase(zeta[i])) {
          loggedin = true;
          return logonspew.getBytes();
        }
      }
      return null; //closes the socket, makes it tedious to guess passwords.
    }
    if(!humph.startsWith(theLast)){
      args.clear().wordsOfSentence(humph);
    }
    return (processArgs(args)+miniprompt).getBytes();
  }

  public byte [] onConnect(){//return something to send when a connection has been made
    return ("AppMon:" + "$Revision: 1.125 $" + Ascii.CRLF).getBytes(); //"Enter password" or a misleading version thereof like an FBI warning
  }

  /**
   *
   * @param which variation of ruf to run.
   * @return whether <b>attempt</b> was made.
   */
  private static boolean rufrunner(String which){
    EasyCursor rufprops=app.props("ruf");
    if(rufprops.getBoolean(which)){
      RufClient ruffer=new RufClient(rufprops);
      TextList names=ruffer.run();
      ErrorLogStream.Global().WARNING("ruffed files",names.toStringArray());
      return true;
    } else {
      return false;
    }
  }
  //////////////////////////////////
  // program launch!
  public static Main app;

  public static final void main(String argv[]){
    try {
      //in case we don't have a logcontrol file:
      LogSwitch.SetAll(LogSwitch.ERROR);
      PrintFork.SetAll(LogSwitch.OFF);//output NOTHING. without IP terminal assistance
      app=new Main(Appliance.class);
      //now get overrides from file:
      app.stdStart(argv); //starts logging etc. merges argv with system.properties and thisclass.properties
//doing ruf here minimizes the ram already used.
      rufrunner("onstart");
      Appliance.Initialize();

      ErrorLogStream.Global().ERROR("Appliance is started");
      // now, just keep the app alive (hopefully)
      Main.keepAlive();//still needed as app start doesn't start quickly enough to keep us alive.
      ErrorLogStream.Global().ERROR("keepAlive quit");
      System.exit(ExitCode.MainDied);//#abnormal termination
    } catch (Throwable caught) {
      ErrorLogStream.Global().Caught(caught);
      //will need different codes for different exceptions.
      System.exit(ExitCode.MainCaught);//#abnormal termination
    } finally {
      ErrorLogStream.Global().Exit();
    }
  }

}
//$Id: Appliance.java,v 1.125 2005/03/23 03:42:45 andyh Exp $
