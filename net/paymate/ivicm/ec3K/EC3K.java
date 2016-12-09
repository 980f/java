package net.paymate.ivicm.ec3K;

/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/ec3K/EC3K.java,v $
* Description:  checkmanager 3000 driver
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: EC3K.java,v 1.24 2001/09/14 21:10:38 andyh Exp $
*/

import net.paymate.ivicm.comm.*;
import net.paymate.ivicm.*;
import net.paymate.serial.*;

import javax.comm.*;

import net.paymate.jpos.common.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;

import jpos.services.EventCallbacks;
import jpos.events.*;
import jpos.JposException;

public class EC3K extends SerialDevice
implements TimeBomb, Constants {
  static final ErrorLogStream dbg=new ErrorLogStream(EC3K.class.getName());
  static final ErrorLogStream dbk=new ErrorLogStream(EC3K.class.getName()+":comm");
  static final String VersionInfo= "CM3000 I/F, (C) PayMate.net 2000-2001 $Revision: 1.24 $";
  static final int ACK= 0x06;
  static final int NACK=0x15;
  public static final byte STX=0x02;
  public static final byte ETX=0x03;

  StopWatch responseTime=new StopWatch(false);

  POSKeyboardService  Keypad;
  MICRService         Miker;
  LineDisplayService  LDS; //not needed yet?? has error event queue??

  //class Sender
  String monstratus;   //"to be shown"
  boolean buffered=false;
  boolean myCTS=true;  //startup with permission to send
  Alarmum timeout;

  RcvPacket inbuffer=new RcvPacket(Command.RcvSize);//closes timing hole on first command
  RcvPacket inPacket; //double buffering of above.
  int EndExpectedAt;

  protected synchronized void onCompletion(boolean failed){//was too deeply indented
    dbg.VERBOSE("endOk:"+!failed);
    inPacket=inbuffer;      //save just received
    startReception();
    switch(inPacket.incode()){
      case FX_POLL:{
        if(Miker!=null){
          Show(monstratus);//conditionally refresh the display... if available
          Miker.Post(failed,inPacket);
        }
      } break;
      case FX_POLL_KEYBOARD:{
        if(Keypad!=null){
          Keypad.Post(failed,inPacket);
        }
      } break;
      case NACK:{ //resend stored string, no state changes
        sendCommand();
      } break;
      case ACK:{ //ok to send another string.
        if(buffered){
          sendCommand();
        }
      } break;
      default:{
        dbg.ERROR("Unknown return code:"+inPacket.incode());
      } break;
    }
  }

  public void onTimeout(){
    sendCommand();//teh only thing timed is the sending of a string.
  }

  public int onByte(int commchar){
    if(commchar<=Receiver.TimedOut){//lump other serious errors in with timeout
      onTimeout();
    } else {
      if(inbuffer.append(commchar)){//if there is room in the buffer
        if (commchar==STX&&inbuffer.ptr()!=1) {
          dbk.ERROR("Abrupt start of frame");
          startReception();    //start framed
        } else {
          dbk.VERBOSE("ptr:"+inbuffer.ptr()+" hex:"+Safe.ox2(commchar));
          switch(inbuffer.ptr()){
            case 1:{
              if (commchar!=STX) {
                dbk.ERROR("Seeking start of frame");
                startReception();    //start framed
              }
            } break;

            case 2:{
              //dbg.VERBOSE("response code");
            } break;

            case 3:{//status byte for data, ETX for display op acknolwedge
              if (inbuffer.incode()==ACK || inbuffer.incode()==NACK) {//ack or nack packet
                Alarmer.Defuse(timeout);//this timeout is related to just display commands
                myCTS=true;
                dbg.VERBOSE(" Response time:"+responseTime.Stop());
                onCompletion(commchar!=ETX);
              } else if(commchar >0 ){
                dbg.VERBOSE("device reports error");
              }
            } break;

            case 4:{//expected remaining bytes
              EndExpectedAt=commchar+5;//5 is framed overhead
            } break;

            default:{
              if(inbuffer.ptr()>=EndExpectedAt){//end packet
                onCompletion(commchar!=FX_ETX);
              } else { // not done yet.
                if(commchar==FX_ETX){
                  dbk.VERBOSE("Possible etx at "+(inbuffer.ptr()-1)+" out of "+EndExpectedAt);
                }
              }
            } break;
          }//end protocal state machine
        }
      }
    }
    return Receiver.TimeoutNever;
  }

  public void serialEvent(SerialPortEvent serialportevent){
    try {
      dbg.Enter("ec3k.rcv");
      myPort.boostCheck();
      int evt=serialportevent.getEventType();
      switch(evt){
        case SerialPortEvent.DATA_AVAILABLE: {
          int commchar;
          while( (commchar = myPort.is.read()) !=-1){
            onByte(commchar);//we aren't doing timeouts at byte level in this packacge
          }//while buffer not empty
        } break;
        default: { //other comm events aren't registered for...
          dbg.ERROR("Unexpected Comm Event:"+evt);
        }
      }
    }
    catch(Exception caught){
      dbg.ERROR("IOException on reception");
      dbg.Caught(caught);
    }
    finally {
      dbg.Exit();
    }
  }

  protected void startReception(){
    EndExpectedAt=0;
    inbuffer= new RcvPacket(Command.RcvSize);
  }

  protected synchronized void sendCommand(){
    if(myPort!=null){
      dbg.Enter("sendCommand");
      try {
        dbk.VERBOSE("Sending:"+monstratus);
        myCTS=false;
        myPort.os.write(STX);
        myPort.os.write(monstratus.getBytes());
        responseTime.Start();
        myPort.os.write(ETX);
        buffered=false;
        timeout=Alarmer.New(2000,this);//+_+ should compute from packet values, til then make it grossly big.
      }
      catch (Exception caught){
        dbg.ERROR(caught.toString());
        myCTS=true; //allow another attempt via Show()
      } finally {
        dbg.Exit();
      }
    }
  }
  ////////////////////
  // interface RawDisplay

  public synchronized void Show(String forDisplay){
    monstratus= Safe.tail(forDisplay,FX_DISPLAY_MAXCHARS);
    buffered=true;

    if(myCTS){
      sendCommand();
    } else {
      dbk.VERBOSE("Buffered:"+monstratus);
      if (!Alarmer.isTicking(timeout)) {//don't wait for ever, try to send after a bit.
        timeout=Alarmer.New(1500,this);//way bigger than needed.
      }
    }
  }

  public void Display(String forDisplay){
    Show(forDisplay);
  }

  public void refresh(){
    Show(monstratus);
  }

  ////////////////////
  public void Start(){//start the ec3k service loop, which is NOT a thread by the way.
    monstratus="";
    Show("PayMate.net EC3K");
  }

  public void onConnect(){
    startReception();//better name: prepare for reception
    Start();
  }

  /**
  * @deprecated needs to change for multiple terminals
  */
  public EC3K( SerialConnection sc) {
    super("legacyConstructor");
    dbg.Enter("EC3K()");
    if(sc!=null){
      sc.parms.setFlow(SerialPort.FLOWCONTROL_NONE);//+_+ move to config, asap
    }
    Connect(sc);
    dbg.Exit();
  }

  public EC3K(String id) {
    super(id);
  }

  ///////////////////////////////////
  //services provided by this device:
  public jpos.services.LineDisplayService14 LineDisplay(String jname){
    return LDS=  new LineDisplayService(jname,this);
  }

  public jpos.services.MICRService14        MICR(String jname){
    return Miker= new MICRService(jname,this);
  }

  public jpos.services.POSKeyboardService14 Keypad(String jname){
    return Keypad= new POSKeyboardService(jname,this);
  }

}
//$Id: EC3K.java,v 1.24 2001/09/14 21:10:38 andyh Exp $
