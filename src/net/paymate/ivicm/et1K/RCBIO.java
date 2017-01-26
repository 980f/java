package net.paymate.ivicm.et1K;

/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/RCBIO.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.41 $
*/

import net.paymate.ivicm.SerialDevice;
import net.paymate.jpos.Terminal.LinePrinter;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.lang.ThreadX;
import net.paymate.text.Formatter;

// Referenced classes of package net.paymate.ivicm.et1K:
//      Command, ET1K, Callback

public class RCBIO {
  private int printPriority=0;
final static int priorityText=1;
final static int priorityInit=priorityText+1;

  boolean busy;
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(RCBIO.class);
  ET1K hw;
  Alarmum  deadlock;
  protected LinePrinter user;
  public static double delayPerLine=0.100;//value is 4debug //static and public 4gui tweaking

  protected int lastPayload=0;
  protected final double auxchartime=9.0/9600.0; //should derive from aux params, this==9600N81

  boolean haveEnabled=false;

  static final byte enablemsg[] = {
    AuxCode.AUX_PORT_1,
    AuxCode.AUX_BAUD_9600,
    AuxCode.AUX_PARITY_NONE,
    AuxCode.AUX_DATABITS_8,
    AuxCode.AUX_STOPBITS_1
  };

  public RCBIO(String s, ET1K hw) {
    busy = false;
    lastPayload = 0;
    this.hw = hw;
    dbg.VERBOSE("My et1l:".concat(String.valueOf(String.valueOf(String.valueOf(hw)))));
    hw.setStartup(toEnable());
  }

  int commonresponse(Command cmd) {
    Alarmer.Defuse(deadlock);
    switch(cmd.response()) {
      case ResponseCode.INVALID_PORT_STATE:  return -1;
      case ResponseCode.SUCCESS:             return 0;
    }
    dbg.ERROR("reply code:".concat(String.valueOf(String.valueOf(Formatter.ox2(cmd.response())))));
    return 1;
  }

  class DataResponse implements Callback {
    public Command Post(Command cmd) {
      switch(commonresponse(cmd)) {
        case -1:{
          busy = true;
        } return toEnable();
        case ResponseCode.SUCCESS:{ // '\0'
          sendNext(cmd.responseTime.seconds());
        } break;
        default:{ //errors
          sendNext(cmd.responseTime.seconds());
        } break;
      }
      return null;
    }
  }

  class EnableResponse implements Callback {
    public Command Post(Command cmd) {
      haveEnabled=commonresponse(cmd)<=0;
      //to ensure that if we have to enable again the enable cmd precedes any hanging print commands
      printPriority=haveEnabled?Command.priorityExpress:Command.priorityFront-1;

      user.CTSEvent(true);
      return null;
    }
  }
/**
 * @param waited is the number of seconds (usually around .05) that have already
 * been expended waiting for the previous text to be sent to printer.
 */
  void sendNext(double waited) {
    if(!haveEnabled){
      //resend the enable
      hw.QueueCommand(toEnable());
    } else {
//      double waited = waiting.seconds();
      dbg.VERBOSE("Xmit Time:"+waited);
      double padding = lastPayload * auxchartime;//4debug, gets overwritten:
      padding = delayPerLine;
      padding -= waited;
      if(padding > 0.0) {
        dbg.VERBOSE("Padding:"+padding);
        ThreadX.sleepFor(padding);
      } else {
        dbg.VERBOSE("Done for:"+(-padding));//wonderful syntax...
      }
//opens up a timing gap      busy = false;
      user.CTSEvent(true);
    }
  }

  public void onTimeout() {
    if(busy) {
      dbg.ERROR("timedout, trying to proceed");
      user.CTSEvent(true);
    }
  }

  public Exception Attach(LinePrinter user) {
    this.user = user;
    return null;
  }

  protected Command toEnable(){
    Command cmd=new Command(OpCode.AUX_FUNCTION,AuxCode.AUX_ENABLE, enablemsg, "RCBsetParms");
    cmd.onReception=new EnableResponse();
    cmd.boostTo(priorityInit);//just above any print
    dbg.WARNING("toEnable:"+cmd.outgoing().toSpam());
    return cmd;
  }

  protected Command toPrint(byte msg[]){
    Command cmd=new Command(OpCode.AUX_FUNCTION,AuxCode.AUX_SEND, msg, "RCBPrint");
    cmd.onReception=new DataResponse();
    cmd.boostTo(priorityText);
    cmd.service=null; ///who references this? alh:error handlers
    return cmd;
  }

  public synchronized boolean setRTS(boolean on){
    if(on){
      if(!busy){
        user.CTSEvent(true);//which often calls SendLine
      }
    } else {//trying to stop dropped lines
      busy = false;
    }
    return true;
  }

  public int maxByteBlock(){
    return Codes.maxPacketSize-1-1-4-1;//buffer-"auxfunction"-"auxsend"-"stx etc"-insurance..
  }

  public boolean SendLine(byte msg[]) {
    if(!haveEnabled){//need to resend the enable
      hw.QueueCommand(toEnable());
    }
    lastPayload=msg.length; //for directIO to add padding.
    busy=true;
    //+++ have ot lock these together better, may have to make et1k do it.
    hw.QueueCommand(toPrint(msg));
    return true;
  }

}
/**
* the cycle starts with a setRTS(true).
*
* when the enTouch has finished transferring the data from the command into the'
* buffer for the aux port it replies which calls back to post, which calls
* the CTS function which calls back into this module at sendline.
*/
//$Id: RCBIO.java,v 1.41 2003/07/27 05:35:04 mattm Exp $
