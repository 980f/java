package net.paymate.ivicm.et1K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/ET1K.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ET1K.java,v 1.61 2001/11/17 00:38:34 andyh Exp $
*/

import net.paymate.jpos.common.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.ivicm.*;
import net.paymate.ivicm.comm.*;
import net.paymate.serial.*;
import jpos.services.EventCallbacks;
import net.paymate.jpos.Terminal.LinePrinter;//jpos printer is too complex...

import jpos.events.*;
import jpos.*;

import javax.comm.*;
import java.util.Vector;


public final class ET1K extends SerialDevice implements QActor {
  static final ErrorLogStream dbg= new ErrorLogStream(ET1K.class.getName());
  static final ErrorLogStream rcvr=new ErrorLogStream(ET1K.class.getName()+".rcvr");
  static final ErrorLogStream qusr=new ErrorLogStream(ET1K.class.getName()+".qusr");

  static final String driverVersion="(C)2000-2001 Paymate ET1K $Revision: 1.61 $";

  //////////////////
  //link state
  /**
  * currentCommand is the last one sent EXCEPT for retry commands (05.04.06.07)
  */
  Command currentCommand;
  /**
  * this is true when a whole packet seems to have been received.
  */
  boolean phasedin=false;
  /**
  * true when we THINK that the enTouch MIGHT be sending a response
  */
  Reply rcvbuf;
  //end link state
  ///////////////

  Monitor qlock;//still neededuntil objFifo implements uniqueness per this module's needs

  //////////////////
  /**
  * inter character timeout
  */
  public final static int ICT=5999;//inter character timeout
  /**
  * initial response timeout
  */
  public final static int plentyOfTime=6000;//packet initial response timeout

  int resendCount=0;//"request to send last reply again" counter
  public static double minlatency =- .050; //pessimistic setting
  ////////////////////////////////
  // command queuing

  QAgent CommandProcessor;
  Vector atStartup=new Vector();//commands to reissue on a reconnect

  /**
  * thread=any that trigger PosTerminal, especially system initialization
  */

  public void setStartup(Command cmd){
    qusr.Enter("setting startup:"+cmd.errorNote);
    try {
      //at front cause list is reverse iterated when used. we want order to be preserved.
      atStartup.insertElementAt(cmd,0);
      QueueCommand(cmd);//and execute now as well.
    }
    finally {
      qusr.Exit();
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
      qusr.Enter(complete.errorNote);
      String blather="attempting to enque:"+complete.errorNote;
      if(complete.isaPoller){
        qusr.VERBOSE(blather);
      } else {
        qusr.WARNING(blather);
      }
      if(CommandProcessor.putUnique(complete,complete.highPriority)){
        qusr.WARNING("Command already in queue:"+complete.errorNote);
        return;
      }
    } finally {
      qusr.Exit();
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

  private void cmsg(String msg){
    if(currentCommand.isaPoller){
      dbg.VERBOSE(msg+currentCommand.errorNote);
    } else {
      dbg.WARNING(msg+currentCommand.errorNote);
    }
  }

  /**
  * thread=QAgent
  * notifier= super.myPort. an object reference that definitely doesn't change while we are running.
  */
  private void interact(Command cmd){
    dbg.Enter("Interact");
    if(cmd!=null){
      try {
        setCommand(cmd);
        cmsg("beginning:");
        rcvbuf= Reply.New(); //rvbuf is allocated outside of this function due to use of legacy 'onByte()' routine
        sendCommand();
        //      long sleepuntil=Safe.utcNow()+plentyOfTime;
        do {
          cmsg("about to wait for ");
          if(ThreadX.waitOn(myPort,plentyOfTime,dbg)){//true: timeout or had an exception
            dbg.VERBOSE("waitOn returned true");
            if(!suckInput(dbg)){//one last chance for hanging data to come in.
              //sometimes we get here with a packet that is complete but not handled,
              //hwat the fuck gives!!!!
              if(rcvbuf.isComplete()){
                dbg.ERROR("timeout but data was complete");
                int whatgives=onCompletion(rcvbuf,dbg);//try this one more freaking time
                dbg.ERROR("onCompletion now gives ("+TimeoutNever+" desired):"+whatgives);
                if(whatgives==TimeoutNever){
                  continue;//test completion flag, break would skip that test.
                }
              }
              if(++resendCount<1000){//start with half an hour of retries...
                dbg.ERROR("After timeout retrying "+resendCount+" of "+currentCommand.outgoing().toSpam(5));
                dbg.ERROR("want "+ rcvbuf.EndExpectedAt()+ " rcv'd "+ Safe.ox2(rcvbuf.ptr())+" so far:"+rcvbuf.toSpam());
                sendBuffer(resync.outgoing());
                rcvbuf.reset();
                //currentCommand.processed will be false so we will loop back to the waitOn()
              } else {
                dbg.ERROR("Giving up after Timeout on "+currentCommand.outgoing().toSpam(5));
                onCompletion(rcvbuf,dbg);//complete, with failure.
              }
            }  //else was ok, we just didn't get timely notification
          } else {
            dbg.VERBOSE("waitOn returned false");//a good thing
          }
        } while(!currentCommand.processed);
        cmsg("got reply: "+rcvbuf.toSpam(15)+" on:");
        //else we have finished an interaction
      } finally{
        dbg.Exit();
      }
    }
  }

  /**
  * thread=QAgent
  */
  public void runone(Object fromq){
    try {
      while(!phasedin){//until one worksse
        dbg.WARNING("attempting resync");
        interact(resync);
      }
      dbg.VERBOSE("running command fromq");
      interact((Command) fromq);
    }
    catch (ClassCastException cce) {
      dbg.ERROR("Non command object in command queue:"+fromq.getClass().getName());
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
  */
  private int onCompletion(Reply response,ErrorLogStream somedbg){
    somedbg.WARNING("Completed "+response.toSpam(10));
    phasedin=response.isOk(); //used by detector
    somedbg.VERBOSE("phasedin:"+phasedin);
    currentCommand.failed=!phasedin; //legacy, needs cleanup

    if(phasedin && currentCommand.outgoing().opCode()!=response.opCode()){
      currentCommand.failed=currentCommand.outgoing().opCode()!=Codes.RESEND_LAST_DATA_BLOCK;//resync code
      if (currentCommand.failed) {
        //stay in listening state for another timeout period.
        if(++resendCount<1000){//start with half an hour of retries...
          somedbg.ERROR("op mismatch, pausing in the hopes it will fix itself ");
          myPort.reallyFlushInput(100); //desperate gamble.
          rcvbuf.reset();
          return plentyOfTime;
        } else {
          somedbg.ERROR("op mismatch, giving up");
        }
      }
      else {
        somedbg.WARNING("op mismatch ok, is resync");
      }
    }
    currentCommand.incoming=response;//Services better copy what they want to keep!
    Command chained=null;
    if(currentCommand.failed){
      chained=PostFailure("Input Failure",somedbg);
    } else if(currentCommand.onReception !=null){
      /*
      * once I finally got a spec I had to inspect every individual command to
      * establish that contrary to some of their descriptions the error codes are universal.
      * so... we can inspect the communications related ones HERE.
      */
      int fart=currentCommand.response();
      switch (fart) {
        case Codes.POWERUP:{//power failed
          somedbg.ERROR("PowerupEvent");
          chained=currentCommand;//reissue
          //add in powerup config commands
          onReconnect(somedbg);
        } break;

        //the following are problems the device had with our last sending(s).
        case Codes.INVALIDSEQUENCENUMBER: //
        somedbg.ERROR("InvalidSequenceNumber:"+currentCommand.outgoing().toSpam(5));
        if(currentCommand instanceof BlockCommand){
          ((BlockCommand) currentCommand).dumpSent(somedbg,somedbg.ERROR);
        }
        //join.
        case Codes.PACKETERROR: //length doesn't jibe, unlikely without LRC error as well
        case Codes.MESSAGEOVERFLOW://aka receive buffer full
        case Codes.INVALIDLRC: //we check before we send.
        case Codes.PACKETTIMEOUT:{// packet ain't likely to fix itself...
          chained=PostFailure("PacketError "+Safe.ox2(fart),dbg);
        } break;

        default:{//command specific meaning and response
          somedbg.WARNING("Posting "+currentCommand.errorNote);
          chained=Post(currentCommand);
        } break;
      }
    }
    if(chained!=null){
      somedbg.WARNING("Chaining command:"+chained.errorNote);
      QueueCommand(chained.boost());//boost is needed to keep BlockCommand together
    }
    markDone(currentCommand);
    return TimeoutNever;//legacy trigger for Done with last command.
  }

  /**
  * thread=serialReader
  */
  private void markDone(Command currentCommand){
    currentCommand.processed=true;
    dbg.VERBOSE("Notifying interactor");
    ThreadX.notify(myPort);
  }

  /**
  * thread=initialization(main or appliance),serialReader
  */
  private void onReconnect(ErrorLogStream somedbg){
    somedbg.VERBOSE("onReconnection");
    for(int i=atStartup.size();i-->0;){
      Command cmd=(Command)atStartup.elementAt(i);
      somedbg.WARNING("queuing startup:"+cmd.errorNote);
      QueueCommand(cmd);
    }
  }

  ////////////////
  //receiver state machine
  /**
  * thread=serialReader,QAgent on timeout
  */
  public int onByte(int commchar) {
    rcvr.Enter("onByte:"+Safe.ox2(commchar));
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
              rcvbuf.reset();
            }
          } return ICT;

          case 2:{ // length byte
            //do nothing, almost any value is valid
          } return ICT;

          case 3: {
            if(currentCommand!=null){
              int expected = currentCommand.opCode();
              if(commchar != expected) {
                if(expected == 6) {
                  rcvr.WARNING("resync will toss:"+Safe.ox2(commchar));
                } else {
                  rcvr.ERROR("command "+Safe.ox2(expected)+" reply "+Safe.ox2(commchar));
                }
                rcvr.VERBOSE("+rcvbuf:"+rcvbuf.toSpam());
              }
            }
            //but we don't actually change state
          } return ICT;

          default:{
            if(rcvbuf.isComplete()) {//test length AND checksum et al.
              rcvr.WARNING("Rcv done:"+currentCommand.responseTime.Stop());
              return onCompletion(rcvbuf,rcvr);
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
      rcvr.Exit();
    }
  }

  /**
  * @return "done"
  */
  private boolean suckInput(ErrorLogStream rcvr){
    int nexttimeout= TimeoutNever;
    while(myPort.available()>0){//got bytes
      nexttimeout= onByte(myPort.ezRead());//might extend the thread sleep...
      rcvr.VERBOSE("onByte returned:"+nexttimeout);
      if(nexttimeout==TimeoutNever){
        return true;//packet done.
      }
    }
    return false;
  }

  int reentranceCheck=0;
  //javax.comm interface
  int eventcount=0;
  /**
  * thread=serialReader
  */
  public void serialEvent(SerialPortEvent serialportevent) {
    rcvr.VERBOSE("Event "+ ++eventcount);
    try {
      if(reentranceCheck++>0){
        rcvr.ERROR("Re-entered ISR:"+reentranceCheck);
        //trust the lock to prevent bad things from happening
      }
      //      LOCK("ISR");//entry point for javax.comm
      if(reentranceCheck>1){
        rcvr.ERROR("Re-entered past lock:"+reentranceCheck);
        //--reentr is in a finally clause
        return; //data loss will eventually create a timeout and protocol restart.
      }

      myPort.boostCheck();//adjust priority
      switch(serialportevent.getEventType()) {
        case SerialPortEvent.DATA_AVAILABLE:{
          rcvr.VERBOSE("got data interrupt");
          suckInput(rcvr);
        } break;

        case SerialPortEvent.FE:
        case SerialPortEvent.PE:
        case SerialPortEvent.OE:{
          rcvr.ERROR("Receive Error "+serialportevent.getEventType());
        } break;

        default:{
          rcvr.ERROR("Ignored Unexpected Serial Event");
        } break;
      }
    }
    catch(Exception unknown) {
      rcvr.Caught("on processing serial event ", unknown);
    }
    finally {
      --reentranceCheck;
    }
  }

  private Exception sendBuffer(LrcBuffer outgoing) {
    cmsg(currentCommand.outgoing().toSpam(5)+" ");
    return myPort.lazyWrite(outgoing.packet(), 0,outgoing.ptr());
  }

  private void sendCommand(){//only called from synched methods.
    dbg.Enter("sendCommand");
    try {
      resendCount=0;
      sendBuffer(currentCommand.outgoing());
      currentCommand.responseTime.Start();
    }
    catch(NullPointerException npe){
      dbg.ERROR("npe on "+currentCommand.errorNote);
    }
    finally {
      dbg.Exit();
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

  Command resync=new Command(Codes.RESEND_LAST_DATA_BLOCK,"resync");

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
    qusr.Enter("onConnect");
    try {
      int flushed=myPort.reallyFlushInput(Reply.RcvSize);
      qusr.WARNING("Flushed "+flushed+" bytes");
      CommandProcessor.Clear();//(re) starts driver
      QueueCommand(Versions());//doesn't work right cuase it has no service to watch over it.
      onReconnect(qusr); //re connects do NOT recreate poll commands.
    }
    finally {
      qusr.Exit();
    }
  }

  /**
  * while we only expect this to be called once per ET1K object creation,
  * we don't trust the underlying serial port to not already have data to send.
  */
  public JposException Connect(SerialConnection sc){
    qusr.VERBOSE("connecting:"+myName);
    if(sc!=null&& sc.parms!=null){//config reload failed here
      sc.parms.setFlow(SerialPort.FLOWCONTROL_NONE);
      sc.parms.obufsize=Codes.maxPacketSize+1; //+1 for luck
      //      qusr.mark(sc.parms.getPortName());
    }
    return super.Connect(sc);//this calls back to onConnect()
  }

  public ET1K(String givenName){
    super(givenName);
    qlock=new Monitor(givenName,qusr);
    CommandProcessor= QAgent.New(givenName,this);
    CommandProcessor.config(dbg);
    qusr.VERBOSE("constructed:"+givenName);
  }

  ////////////////////////////////////
  // Service providers:

  public static Command Versions(){
    BlockCommand cmd= new BlockCommand();
    cmd.errorNote="DriverVersions";
    cmd.addCommand(Command.JustOpcode(Codes.CLEAR_SCREEN));
    int row=0;
    cmd.addCommand(DisplayTextAt(++row,1,0,driverVersion));
    cmd.addCommand(DisplayTextAt(++row,1,0,FormService.VersionInfo));
    cmd.addCommand(DisplayTextAt(++row,1,0,MSRService.VersionInfo));
    cmd.addCommand(DisplayTextAt(++row,1,0,PINPadService.VersionInfo));
    return cmd;
  }

  //+_+ make a formal LineDisplayService???
  public static final LrcBuffer DisplayTextAt(int row, int col, int fontcode, String s){
    int cutoff=Math.min(s.length(),Codes.maxPacketBody-4);//4==size,row,col,font
    LrcBuffer cmd = Command.Buffer(cutoff+5);
    cmd.append(Codes.DISPLAY_TEXT_STRING);
    cmd.append(cutoff); //string size for entouch
    cmd.append(row);
    cmd.append(col);
    cmd.append(fontcode);
    cmd.append(s.substring(0,cutoff));
    cmd.end();
    return cmd;
  }

  public jpos.services.FormService14    FormService(String jname){
    return new FormService              (jname, this);
  }

  public jpos.services.MSRService14     MSRService(String jname){
    return new MSRService               (jname, this);
  }

  public jpos.services.PINPadService14  PINPadService(String jname){
    return new PINPadService            (jname, this);
  }

  public /*jpos omission*/ LinePrinter  AuxPrinter(String jname){
    return new RCBPrinter               (jname, this);
  }

  public Service rawAccess(){
    return new Service("Raw Access",this);
  }

  /////////////////////////////////////////
  //  public LinePrinter ScreenPrinter(){
    //    return new ScreenPrinter(myName,this);
  //  }


}
//$Id: ET1K.java,v 1.61 2001/11/17 00:38:34 andyh Exp $
