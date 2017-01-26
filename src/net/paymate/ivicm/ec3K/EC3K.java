package net.paymate.ivicm.ec3K;

/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/ec3K/EC3K.java,v $
* Description:  checkmanager 3000 driver
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: EC3K.java,v 1.48 2003/07/27 05:35:03 mattm Exp $
* <strike>@todo incorporate stxetxPacket.</strike>
* @todo: this class is deprecated, gut it if it gets in the way of new stuff.
*/

import net.paymate.ivicm.comm.*;
import net.paymate.ivicm.*;
import net.paymate.serial.*;
import net.paymate.lang.StringX;
import net.paymate.awtx.DisplayHardware;
import net.paymate.lang.ContentType;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.util.Ascii;

public class EC3K extends DisplayDevice implements DisplayHardware,TimeBomb {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(EC3K.class);
  static final ErrorLogStream dbk=ErrorLogStream.getExtension(EC3K.class,"comm");
  static final String VersionInfo= "CM3000 I/F, (C) PayMate.net 2000-2001 $Revision: 1.48 $";

  StopWatch responseTime=new StopWatch(false);

  POSKeyboardService  Keypad;
  MICRService         Miker;
  LineDisplayService  LDS; //not needed yet?? has error event queue??

  EC3KDisplayPad checkfixer;

  //class Sender
  String monstratus;   //"to be shown"
  boolean buffered=false;
  boolean myCTS=true;  //startup with permission to send
  Alarmum displayTimeout;

  RcvPacket inbuffer=new RcvPacket(Command.RcvSize);//closes timing hole on first command
  RcvPacket inPacket; //double buffering of above.
  int EndExpectedAt;

  public static final byte FX_POLL                      = 33;
  public static final byte FX_POLL_KEYBOARD             = 51;
  public static final byte FX_DISPLAY_MAXCHARS          = 16;

  public static final byte STATUS_SUCCESS               = 0;


  private synchronized void onCompletion(boolean failed){//was too deeply indented
    dbg.VERBOSE("endOk:"+!failed);
    inPacket=inbuffer;      //save just received
    startReception();
    switch(inPacket.incode()){
      case FX_POLL:{
        if(Miker!=null){
//          Show(monstratus);//conditionally refresh the display... if available
          Miker.Post(failed,inPacket);
          checkfixer.onCheck();
        }
      } break;
      case FX_POLL_KEYBOARD:{
        if(Keypad!=null){
          Keypad.Post(failed,inPacket);
        }
      } break;
      case Ascii.NAK:{ //resend stored string, no state changes
        sendCommand();
      } break;
      case Ascii.ACK:{ //ok to send another string.
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
    if(commchar==Receiver.TimedOut){//always false for this device
      return Receiver.TimeoutNever;
    }
    if(commchar<Receiver.TimedOut){//lump other serious errors in with timeout
      onTimeout();
    } else {
      if(inbuffer.append(commchar)){//if there is room in the buffer
        if (commchar==Ascii.STX&&inbuffer.ptr()!=1) {
          dbk.ERROR("Abrupt start of frame");
          startReception();    //start framed
        } else {
          dbk.VERBOSE("ptr:"+inbuffer.ptr()+Receiver.imageOf(commchar));
          switch(inbuffer.ptr()){
            case 1:{
              if (commchar!=Ascii.STX) {
                dbk.ERROR("Seeking start of frame");
                startReception();    //start framed
              }
            } break;

            case 2:{
              //dbg.VERBOSE("response code");
            } break;

            case 3:{//status byte for data, ETX for display op acknolwedge
              if (inbuffer.incode()==Ascii.ACK || inbuffer.incode()==Ascii.NAK) {//ack or nack packet
                Alarmer.Defuse(displayTimeout);//this timeout is related to just display commands
                myCTS=true;
                dbg.VERBOSE(" Response time:"+responseTime.Stop());
                onCompletion(commchar!=Ascii.ETX);
              } else if(commchar >0 ){
                dbg.VERBOSE("device reports error");
              }
            } break;

            case 4:{//expected remaining bytes
              EndExpectedAt=commchar+5;//5 is framed overhead
            } break;

            default:{
              if(inbuffer.ptr()>=EndExpectedAt){//end packet
                onCompletion(commchar!=Ascii.ETX);
              } else { // not done yet.
                if(commchar==Ascii.ETX){
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

  private void startReception(){
    EndExpectedAt=0;
    inbuffer= new RcvPacket(Command.RcvSize);
  }

  private synchronized void sendCommand(){
    if(myPort!=null){
      dbg.Enter("sendCommand");
      try {
        dbk.VERBOSE("Sending:"+monstratus);
        myCTS=false;
        myPort.os.write(Ascii.STX);
        myPort.os.write(monstratus.getBytes());
        responseTime.Start();
        myPort.os.write(Ascii.ETX);
        buffered=false;
        displayTimeout=Alarmer.New(2000,this);//+_+ should compute from packet values, til then make it grossly big.
      }
      catch (Exception caught){
        dbg.ERROR(String.valueOf(caught));
        myCTS=true; //allow another attempt via Show()
      } finally {
        dbg.Exit();
      }
    }
  }
  ////////////////////
  // interface RawDisplay

  public synchronized void Show(String forDisplay){
    monstratus= StringX.tail(forDisplay,FX_DISPLAY_MAXCHARS);
    dbk.VERBOSE("Show:"+forDisplay+" ["+monstratus+"]");
    buffered=true;

    if(myCTS){
      sendCommand();
    } else {
      dbk.VERBOSE("Buffered");
      if (!Alarmer.isTicking(displayTimeout)) {//don't wait for ever, try to send after a bit.
        displayTimeout=Alarmer.New(1500,this);//way bigger than needed.
      }
    }
  }

  public void Display(String forDisplay){//shared with tester
    Show(forDisplay);
  }


  public boolean doesStringInput(ContentType ct){
    return false;
  }
  public void getString(String prompt,String preload,ContentType ct){}

  public boolean hasTwoLines(){ //has *at least* two lines
    return false;
  }
  public void Echo(String forDisplay){//for keystroke echoing on second line
    Display(forDisplay);
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
  * @..deprecated needs to change for multiple terminals
  */
  public EC3K(SerialConnection sc) {
    super("legacyConstructor");
    dbg.Enter("EC3K()");
    if(sc!=null){
      sc.parms.setFlow(Parameters.FLOWCONTROL_NONE);//+_+ move to config, asap
    }
    Connect(sc);
    dbg.Exit();
  }

  public EC3K(String id) {
    super(id);
  }

  ///////////////////////////////////
  //services provided by this device:

  public MICRService        MICR(String jname){
    return Miker= new MICRService(jname,this);
  }

  public MICRService  MICR(){
    return Miker= new MICRService(myName,this);
  }

  public POSKeyboardService Keypad(String jname){
    return Keypad= new POSKeyboardService(jname,this);
  }

  public POSKeyboardService Keypad(){
    return Keypad= new POSKeyboardService(myName,this);
  }

  public EC3KDisplayPad DisplayPad(){
    if(checkfixer==null){
      checkfixer=EC3KDisplayPad.makeFrom(this);
    }
    return checkfixer;
 }
  ////////////////////
  /**
   *
   */
  static public void main(String[] args) {
    EC3K totest=new EC3K("EC3K.tester");

    totest.testConnect2(9600,dbg);

//    dbk.setLevel(LogSwitch.ERROR);
    testapad padtester=new testapad(totest.DisplayPad());
    totest.testloop(dbg);
  }

}
//$Id: EC3K.java,v 1.48 2003/07/27 05:35:03 mattm Exp $
