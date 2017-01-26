package net.paymate.ivicm.et1K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/ET1K.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ET1K.java,v 1.100 2004/02/26 18:40:50 andyh Exp $
*/

import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.ivicm.*;
import net.paymate.ivicm.comm.*;
import net.paymate.serial.*;
import net.paymate.jpos.Terminal.LinePrinter;//jpos printer is too complex...
import net.paymate.text.Formatter;
import java.util.Vector;
import net.paymate.lang.ReflectX;

public final class ET1K extends SerialDevice implements QActor {
  static final ErrorLogStream dbg= ErrorLogStream.getForClass(ET1K.class);
  static final ErrorLogStream rcvr=ErrorLogStream.getExtension(ET1K.class, "rcvr");
  static final ErrorLogStream qusr=ErrorLogStream.getExtension(ET1K.class, "qusr");

  static final String driverVersion="(C)2000-2002 Paymate ET1K $Revision: 1.100 $";

  //////////////////
  //link state
  /**
  * currentCommand is the last one sent EXCEPT for retry commands (05.04.06.07)
  */
  private Command currentCommand;
  /**
  * this is true when a whole packet seems to have been received.
  */
  private boolean phasedin=false;
  /**
  * true when we THINK that the enTouch MIGHT be sending a response
  */
  private Reply rcvbuf;
  private Waiter packetWait;
  //end link state
  ///////////////

  private Monitor qlock;//still neededuntil objFifo implements uniqueness per this module's needs

  //////////////////
  /**
  * inter character timeout
  */
  public final static int ICT=5999;//inter character timeout
  /**
  * initial response timeout
  */
  public final static int plentyOfTime=6000;//packet initial response timeout

  private int resendCount=0;//"request to send last reply again" counter
  public static double minlatency =- .050; //pessimistic setting
  ////////////////////////////////
  // command queuing

  private QAgent CommandProcessor;
  private Vector atStartup=new Vector();//commands to reissue on a reconnect

  /**
  * thread=any that trigger PosTerminal, especially system initialization
  */

  public void setStartup(Command cmd){
    qusr.Enter("setting startup:"+cmd.errorNote);//#gc
    try {
      //at front cause list is reverse iterated when used. we want order to be preserved.
      atStartup.insertElementAt(cmd.boostTo(Command.priorityInit),0);
      QueueCommand(cmd);//and execute now as well.
    }
    finally {
      qusr.Exit();//#gc
    }
  }


   /**
  * stick @param complete into the queue. The command contains its importance.
  * thread=any that trigger PosTerminal
  */
  public void QueueCommand(Command complete){
    try {
      //lock here because this is the entry point for many threads.
      qlock.LOCK("Queueing:"+complete.errorNote);//module entry point for most threads
      qusr.Enter(complete.errorNote);//#gc

      complete.log(qusr,"attempting to enque:");
      if(CommandProcessor.putUnique(complete)){
        qusr.WARNING("Command replaced one already in queue:"+complete.errorNote);
        return;
      }
    } finally {
      qusr.Exit();//#gc
      qlock.UNLOCK("Queueing");
    }
  }

  /**
  * remove object cmd from list if present.
  * thread=any that feed PosTerminal
  */
  void squelch(Command cmd){
    int removed=CommandProcessor.removeAny(cmd);
    qusr.VERBOSE("squelched "+removed+" "+cmd.errorNote);
  }

  private int spewVolume(){
    return dbg.levelIs(LogSwitch.VERBOSE)?80:15;
  }

  private void cmsg(String msg){
    currentCommand.log(dbg,msg);
  }

  /**
  * thread=QAgent
  * notifier= super.myPort. an object reference that definitely doesn't change while we are running.
  */
  private Command interact(Command cmd){
    dbg.Enter("Interact");//#gc
    try {
      if(cmd!=null){
        Command chained=null;//compiler couldn't figure out that all paths were covered...
        setCommand(cmd);
        cmsg("beginning:");
        rcvbuf= Reply.New();
        packetWait.prepare();
        sendCommand();
        long waiter=plentyOfTime;//for most operations
        switch (currentCommand.opCode()) {
          case OpCode.AUX_FUNCTION:{
            if(currentCommand.outgoing().bight(3)==AuxCode.AUX_COMPRESSFLASH){
              waiter=Ticks.forMinutes(2);
            }
          } break;
          case OpCode.GET_COMPRESSED_SIG:{
            waiter=Ticks.forSeconds(5+2);//and now plentyOfTime can be reduced...
          } break;
          case OpCode.getVersionInfo:{
            waiter=Ticks.forSeconds(2+2);//and now plentyOfTime can be reduced...
          } break;
        }

        do {
          cmsg("about to wait");
          packetWait.Start(waiter,dbg);

          boolean iscomp= rcvbuf.isComplete();
          dbg.VERBOSE("iscomp:"+iscomp);
          if(iscomp || suckInput(dbg)){//one last chance for hanging data to come in.
            dbg.VERBOSE("input is complete");//a good thing
            chained=onCompletion(rcvbuf,dbg);
          } else {
            if(++resendCount<1000){//long but not infinite
              dbg.ERROR("After timeout retrying "+resendCount+" of "+currentCommand.outgoing().toSpam(spewVolume()));
              dbg.ERROR("want "+ rcvbuf.EndExpectedAt()+ " rcv'd "+ rcvbuf.ptr()+" so far:"+rcvbuf.toSpam());
              packetWait.prepare();//added for jre 1.4, retry operations were immediately timed out.
              sendBuffer(resync.outgoing());
              rcvbuf.reset();
              chained=null; //but currentCommand.isProcessed will be false so we will loop back to the waitOn()
            } else {
              dbg.ERROR("Giving up after Timeout on "+currentCommand.outgoing().toSpam(spewVolume()));
              chained=onCompletion(rcvbuf,dbg);//complete, with failure.
            }
          }
        } while(!currentCommand.processed);//usually set by onCompletion()
        cmsg("processed reply: "+rcvbuf.toSpam(spewVolume()));
        return chained;
      } else {
        return null;
      }
    } finally {
      dbg.Exit();//#gc
    }
  }

  /**
  * thread=QAgent
  */
  public void runone(Object fromq){
    try {
      Command torun=(Command) fromq;
      while(torun!=null){
        while(!phasedin){//until one worksse
          dbg.WARNING("attempting resync");
          interact(resync);
        }
        dbg.VERBOSE("running command fromq");
        torun=interact(torun);
      }
    }
    catch (ClassCastException cce) {
      dbg.ERROR("Non command object in command queue:"+ReflectX.shortClassName(fromq));
    }
    catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  /**
  * thread=sameas onCompletion()
  */
  private Command PostFailure(String detail,ErrorLogStream dbg){
    rcvr.ERROR(detail);
    if(currentCommand!=null){
      if(currentCommand.service !=null){
        currentCommand.service.PostFailure(detail);//leads to jpos error event
      } else {
        rcvr.WARNING("Command without service! "+currentCommand.errorNote);
        return Post(currentCommand);
      }
    }
    return null;
  }

  /**
  * send reply to person who requested it.
  * thread=sameas onCompletion()
  */
  private Command Post(Command cmd){
    if(cmd!=null){
      if(cmd.onReception !=null){
        return cmd.onReception.Post(cmd);//poster might have multiple commands in queue
      } else {
        return cmd.next();//might be a block command
      }
    }
    return null;
  }

  /**
  * upon completion of a reply packet...even if it fails
  * thread=serialReader usually, QAgent when timeout in interact()
  * @return a chained command for fresh processeing or null, in whcih case isProcessed is true if teh command is complet, or false f we are expecting more input
  */
  private Command onCompletion(Reply response,ErrorLogStream somedbg){
    try {
    cmsg("Completed "+response.toSpam(spewVolume()));
    phasedin=response.isOk();
    somedbg.VERBOSE("phasedin:"+phasedin);
    currentCommand.failed=!phasedin; //legacy, needs cleanup
    currentCommand.incoming=response;//Services better copy what they want to keep!
    Command chained=null;

    if(currentCommand.failed){//not phased in yet
      if(++resendCount<10){//stay in listening state for a few more timeout periods.
        somedbg.ERROR("malformed response, flush and wait longer");
        myPort.reallyFlushInput(100); //desperate gamble.
        rcvbuf.reset();
        return null; //but is Processed==false; so we will wait longer
      } else {
        //hopeless, fail the command
        chained= PostFailure("total timeout",somedbg);
      }
    } else {
      //if powerup OR resync response resend current command
      if(response.opCode()==resync.opCode() || response.response()==ResponseCode.POWERUP){
        somedbg.ERROR("PowerupEvent");
        chained=currentCommand;//reissue
        //add in powerup config commands
        onReconnect(somedbg);
      } else {
          int fart=currentCommand.response();
          switch (fart) {//deal with universal response codes
            case ResponseCode.POWERUP:{//power failed
              somedbg.ERROR("PowerupEvent");
              chained=currentCommand;//reissue
              //add in powerup config commands
              onReconnect(somedbg);
            } break;

            //the following are problems the device had with our last sending(s).
            case ResponseCode.INVALIDSEQUENCENUMBER:{ //
              somedbg.ERROR("InvalidSequenceNumber:"+currentCommand.outgoing().toSpam(spewVolume()));
              if(currentCommand instanceof BlockCommand){
                BlockCommand bc=(BlockCommand) currentCommand;
                bc.dumpSent(somedbg,somedbg.ERROR);
                chained=bc.restart();//posts Failure and returns null if retries exceeded
              }
            } break;
            //join.
            case ResponseCode.PACKETERROR: //length doesn't jibe, unlikely without LRC error as well
            case ResponseCode.MESSAGEOVERFLOW://aka receive buffer full
            case ResponseCode.INVALIDLRC: //we check before we send.
            case ResponseCode.PACKETTIMEOUT:{// packet ain't likely to fix itself...
              chained=PostFailure("PacketError "+Formatter.ox2(fart),dbg);
            } break;

            default:{//command specific meaning and response
              cmsg("Posting ");
              chained=Post(currentCommand);
//              if(currentCommand instanceof PinPadCommand){
//                lockingService=
//              }
            } break;
          }
      }
    }
    markDone(currentCommand);
    if(chained!=null){
      somedbg.WARNING("Chaining command:"+chained.errorNote);
      return chained;
    }
    return null;//but is Processed==true;
    }
    finally {
      gc();
    }

  }

  /**
  * thread=inteact()serialReader
  */
  private void markDone(Command currentCommand){
    currentCommand.processed=true;
  }

  /**
  * thread=initialization(AppliancePinger),serialReader
  * lock queue because all of these must precede all of whatever is in queue.
  */
  private void onReconnect(ErrorLogStream somedbg){
    try {
      qlock.LOCK("reconnecting");
      for(int i=atStartup.size();i-->0;){
        Command cmd=(Command)atStartup.elementAt(i);
        somedbg.WARNING("queuing startup:"+cmd.errorNote);
        QueueCommand(cmd);
      }
    }
    finally {
      qlock.UNLOCK("reconnecting");
    }
  }

  private static final String comchar(int commchar){
    return commchar<0? Receiver.imageOf(commchar): Formatter.ox2(commchar);
  }
  ////////////////
  //receiver state machine

  /**
  * thread=serialReader,QAgent on timeout
  */
  public int onByte(int commchar) {
    rcvr.Enter("onByte:"+ comchar(commchar));//#gc
    myPort.boostCheck(); ///---twould be nice to find a better place for this...
    try {
      if(currentCommand==null){
        rcvr.ERROR("rcvenabled but command is null");
        return plentyOfTime;
      }

      if(commchar >= 0) {//is a real char
        rcvr.VERBOSE("rcvptr:"+rcvbuf.ptr());
        rcvbuf.append(commchar);
        switch(rcvbuf.ptr()) {
          case 1:{ //start of frame
            if(commchar != 0) {//rcv sof is not the same as sent sof.
              rcvbuf.reset();//drop chars until potential SOF encountered
            }
          } return ICT;

          case 2:{ // length byte
            //do nothing, almost any value is valid
          } return ICT;

          case 3: {
            if(currentCommand!=null){
              int expected = currentCommand.opCode();
              if(commchar != expected) {
                if(expected == resync.opCode()) {
                  rcvr.WARNING("resync will toss:"+Formatter.ox2(commchar));
                } else {
                  rcvr.ERROR("command "+Formatter.ox2(expected)+" reply "+Formatter.ox2(commchar));
                }
                rcvr.VERBOSE("+rcvbuf:"+rcvbuf.toSpam());
              }
            }
            //but we don't actually change state
          } return ICT;

          default:{
            if(rcvbuf.isComplete()) {//test length AND checksum et al.
              rcvr.WARNING("Rcv done:"+currentCommand.responseTime.Stop());
              rcvr.VERBOSE("Notifying interactor");
//              ThreadX.notify(myPort);
              packetWait.Stop();
              return TimeoutNever;
            } else {
              rcvr.VERBOSE("So far: "+rcvbuf.toSpam());
              return ICT;
            }
          }//SNR break;
        }
      } else {
        return TimeoutNever;
      }
    }
    finally {
      rcvr.Exit();//#gc
    }
  }

  private Exception sendBuffer(LrcBuffer outgoing) {
    cmsg(currentCommand.outgoing().toSpam(spewVolume())+" ");
    return myPort.lazyWrite(outgoing.packet(), 0,outgoing.ptr());
  }

  private void sendCommand(){//only called from synched methods.
    dbg.Enter("sendCommand");//#gc
    try {
      resendCount=0;
      sendBuffer(currentCommand.outgoing());
      currentCommand.responseTime.Start();
    }
    catch(NullPointerException npe){
      dbg.ERROR("npe on "+currentCommand.errorNote);
    }
    finally {
      dbg.Exit();//#gc
    }
  }

  /**
  *  created for estimating time spent sending data to RCB's "no feedback" output port.
  * @param payloadsize number of bytes within packet, exclude framing and lrc
  */
  public double packetTime(int payloadsize){
    //4=stx,length,opcode,lrc
    return myPort.CharTime(payloadsize+4)+minlatency;
  }

  /**
  *
  */
  private void setCommand(Command newone){
    currentCommand=newone;
  }

  Command resync=new Command(OpCode.RESEND_LAST_DATA_BLOCK,"resync");

  public void ClearError(){//called from Command.onReception.guys calling PostFailre()
    rcvr.WARNING("ClearErrors");
  }

  ////////////////////
  //
  public void disConnect() {
    //    stopReception();
    CommandProcessor.Stop();
    //+++ wait until packet completed then...
    super.disConnect();
  }

  public void Stop(){
    //thread manager wants us to die.
  }


  public void onConnect(){//called by super.Connect()
    qusr.Enter("onConnect");//#gc
    try {
      int flushed=myPort.reallyFlushInput(Reply.RcvSize);
      qusr.WARNING("Flushed "+flushed+" bytes");
      CommandProcessor.Start();// starts driver
      QueueCommand(Versions());//doesn't work right cuase it has no service to watch over it.
      onReconnect(qusr); //re connects do NOT recreate poll commands.
    }
    finally {
      qusr.Exit();//#gc
    }
  }

  /**
  * while we only expect this to be called once per ET1K object creation,
  * we don't trust the underlying serial port to not already have data to send.
  */
  public void Connect(SerialConnection sc){
    qusr.VERBOSE("connecting:"+myName);
    if(sc!=null&& sc.parms!=null){//config reload failed here
//      sc.parms.setFlow(Port.FLOWCONTROL_NONE);
      sc.parms.obufsize=Codes.maxPacketSize+1; //+1 for luck
      //      qusr.mark(sc.parms.getPortName());
    }
    super.Connect(sc);//this calls back to onConnect()
  }

  public ET1K(String givenName){
    super(givenName);
    qlock=new Monitor(givenName,qusr);
    packetWait= new Waiter();
    CommandProcessor= QAgent.New(givenName,this,PriorityComparator.Normal());
    CommandProcessor.config(dbg);
    qusr.VERBOSE("constructed:"+givenName);
  }

  ////////////////////////////////////
  // Service providers:

  public static Command Versions(){
    TextColumn wrapped=new TextColumn(38);//small font-margins, in OurFroms stuff.
    wrapped.add(driverVersion);
    wrapped.add(FormService.VersionInfo);
    wrapped.add(MSRService.VersionInfo);
    wrapped.add(PINPadService.VersionInfo);

    BlockCommand cmd= new BlockCommand("Driver Versions");
    cmd.addCommand(Command.JustOpcode(OpCode.CLEAR_SCREEN));
    for(int row=0;row<wrapped.size();row++){
      cmd.addCommand(DisplayTextAt(1+row,1,0,wrapped.itemAt(row)));
    }
    return cmd;
  }

  //+_+ make a formal LineDisplayService???
  public static final LrcBuffer DisplayTextAt(int row, int col, int fontcode, String s){
    int cutoff=Math.min(s.length(),Codes.maxPacketBody-4);//4==size,row,col,font
    LrcBuffer cmd = Command.Buffer(cutoff+5);
    cmd.append(OpCode.DISPLAY_TEXT_STRING);
    cmd.append(cutoff); //string size for entouch
    cmd.append(row);
    cmd.append(col);
    cmd.append(fontcode);
    cmd.append(s.substring(0,cutoff));
    cmd.end();
    return cmd;
  }

  public void Display(String forDisplay){//for tester
    QueueCommand(new Command(DisplayTextAt(1,1,0,forDisplay),"direct display"));
  }

  public FormService   FormService(String jname){
    return new FormService   (myName, this);
  }

  public MSRService     MSRService(String jname){
    return new MSRService    (jname, this);
  }

  public PINPadService  PINPadService(String jname){
    return new PINPadService (jname, this);
  }

  public /*jpos omission*/ LinePrinter  AuxPrinter(String jname){
    return new RCBPrinter               (jname, this);
  }

  /**
   * a service, so that error scan be responded to.
   */
  public Service rawAccess(){
    return new Service("Raw Access",this);
  }

  /////////////////////////////////////////
  //  public LinePrinter ScreenPrinter(){
    //    return new ScreenPrinter(myName,this);
  //  }

  ///////////////////////////////
  static LogSwitch mygc;
  static int downer=0;//4debug do one right away
  static final int divider=300;
  private static void gc(){
    if(mygc==null){
       mygc=LogSwitch.getFor(ET1K.class,"gc").setLevel(LogSwitch.ERROR);//error == off
    }
    if(--downer<0){
      downer=divider;
      net.paymate.Main.gc(mygc);
    }
  }

    ////////////////////
  /**
   *
   */

  public static Command CompressFlashCommand(){
    return new Command(OpCode.AUX_FUNCTION,AuxCode.AUX_COMPRESSFLASH,"CompressFlash");
  }

  static boolean testquietly=false;
  static public void main(String[] args) {
    ET1K testunit=new ET1K("ET1K.tester");

    testunit.testerConnect(args,19200,dbg);//this guy turns dbg to verbose
    if(testquietly){
      LogSwitch.SetAll(LogSwitch.ERROR);//@in a tester
      PrintFork.SetAll(LogSwitch.VERBOSE);//@in a tester
    } else {
      StringStack.setDebug(LogSwitch.VERBOSE);//test for leaks!
    }
//leave these commentd lines in. alh restores them while doing certain tests.
//    rcvr.setLevel(LogLevelEnum.ERROR);
//    qusr.setLevel(LogLevelEnum.ERROR);
//    dbg.setLevel(LogLevelEnum.ERROR);
//    Reply.rdbg.setLevel(LogLevelEnum.ERROR);
    testunit.Post(testunit.CompressFlashCommand());
    testunit.testloop(dbg);
  }

}
//$Id: ET1K.java,v 1.100 2004/02/26 18:40:50 andyh Exp $
