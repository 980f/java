package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/Techulator.java,v $
 * Description:  paymentech gateway terminal
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.net.*;
import net.paymate.data.*;
import net.paymate.terminalClient.PosSocket.*;
import net.paymate.serial.*;
import net.paymate.lang.StringX;

class delayedAction {
 /**
  * ennum of things that are time delayed
  */
  final static int sendEnq=0;
  final static int hangup=1;
  final static int offhook=2;
  final static int connect=3;
}

public class Techulator extends SerialTerminal implements TimeBomb {//implements LineServerUser,CnxnUser
  ErrorLogStream dbg;

  Alarmum delayer;
  //+_+ replace the next four with an array indexed by delayedAction.
  int connectTime;
  int enqDelayTime;
  int hangupAfter; //delay to ensure java has emitted the EOT before we up dtr
  int hangupFor;
  int delaying;

  boolean forcedtr=false;
  boolean dtrforce=true;

  private void sendCC(int cc){
    dbg.WARNING("Sending "+Ascii.image(cc));
    xterm.port.lazyWrite((byte)cc);
  }

  private void DTR(boolean up){
    dbg.WARNING("set DTR "+up+" force:"+forcedtr+" to:"+dtrforce);
    xterm.port.DTR().setto(forcedtr? dtrforce :up);
  }

  public void onTimeout(){//when enq delay timer expires
    dbg.WARNING("delayed event "+delaying);
    switch(delaying){
      case delayedAction.connect: {
        xterm.port.lazyWrite("CONNECT\r".getBytes());
        startDelayFor(delayedAction.sendEnq);
      } break;
      case delayedAction.sendEnq: {
        sendCC(Ascii.ENQ);
//just send one, can't stifle enq soon enough        startDelayFor(delaying);
      } break;
      case delayedAction.hangup:  {
        DTR(Shaker.OFF);
//        startDelayFor(delayedAction.offhook);
      } break;
      case delayedAction.offhook:  {
        DTR(Shaker.ON);
      } break;
    }
  }

  private void startDelayFor(int delaying){
    switch(this.delaying=delaying){
      case delayedAction.connect: Alarmer.reset(connectTime,  delayer); break;
      case delayedAction.sendEnq: Alarmer.reset(enqDelayTime, delayer); break;
      case delayedAction.hangup:  Alarmer.reset(hangupAfter , delayer); break;
      case delayedAction.offhook: Alarmer.reset(hangupFor,    delayer); break;
    }
    dbg.WARNING("started delay "+ delayer.toSpam());
  }

  private void stopENQ(){
    if(delaying==delayedAction.sendEnq){
      Alarmer.Defuse(delayer);
    }
  }

  private void hangitup(){
    dbg.VERBOSE("Hanging up RSN");
    startDelayFor(delayedAction.hangup);
  }

  private void goOffhook(){
    DTR(Shaker.ON);
  }

  protected void setPortDefaults(EasyCursor ezp){
    ezp.Assert(Parameters.baudRateKey,"9600");
    ezp.Assert(Parameters.protocolKey,"E71");
  }

  public byte [] onConnect(){//called when port is opened
    DTR(Shaker.ON);
    return LineServerUser.NullResponse; //we don't do anything on a simple port open.
    //we could consider sending an EOT to unhang an interrupted tranny.
  }

  byte [] resendableResponse;
  byte [] OK={Ascii.O/*'O'*/,Ascii.K/*'K'*/,Ascii.CR/*'\r'*/};

  /* the output of this is sent to omni terminal*/
  public byte[] onReception(byte[] line){
    stopENQ();
    switch(line[0]){
      case Ascii.PLUS/*'+'*/: {
        hangitup();
      }  return OK;
      case Ascii.ACK:{
        sendCC(Ascii.EOT);//presuming single, not multi.
        resendableResponse=null; //don't let it carry to the future.
        hangitup();//because omni foolishly checks for hangup BEFORE processing message
      } return LineServerUser.NullResponse;
      case Ascii.NAK:{
        if(resendableResponse!=null){
          xterm.port.lazyWrite(resendableResponse);
        }
      } return LineServerUser.NullResponse;
      case Ascii.STX:{
        //lrc checked in line recognizer?? parser??
        dbg.WARNING("txn packet");
        resendableResponse= super.onReception(line);
        if(ByteArray.NonTrivial(resendableResponse)){
          dbg.VERBOSE("non-trivial response");
          return resendableResponse;
        } else {
          dbg.WARNING("hanging up on not-stoodin");
          hangitup();
          return LineServerUser.NullResponse;
        }
      } //break;
      default: {
        String atcommand= new String(line);
        int atisat=atcommand.indexOf("AT");
        if(atisat>=0){
          if(atcommand.indexOf("ATDT",atisat)>=0){
            //can extract phone number here to use as function code.
            dbg.WARNING(Ascii.bracket(ByteArray.subString(line,atisat)));
            goOffhook();//as late as is convenient
            startDelayFor(delayedAction.connect);
            return LineServerUser.NullResponse;
          } else {
            dbg.WARNING(StringX.bracketed("Ok-ing[",atcommand));
            return OK;
          }
        } else {
          //don't respond to non AT stuff
          dbg.WARNING(StringX.bracketed("sinking garbage[",atcommand));
          return LineServerUser.NullResponse;
        }
      } //break;
    }
  }

  public void Start(EasyCursor hacks){//called once, local config tweaks
    super.Start(hacks);
    //the xterm doesn't exist until after the above call as it needs the serial parameters
    //what other info do we need?
//    dbg.ERROR("hack path is:"+hacks.fullKey("checkulator"));
    connectTime=hacks.getInt("connectTime",300);//millis
    enqDelayTime=hacks.getInt("enqDelayTime",1200);//millis
    hangupAfter =hacks.getInt("hangupAfter",11);//millis
    hangupFor   =hacks.getInt("hangupFor",351);//millis
    forcedtr=hacks.getBoolean("forcedtr" ,forcedtr);
    dtrforce=hacks.getBoolean("dtrforce",dtrforce);

    TextList dump=new TextList(6);
    dump.add("Techulator config:",termInfo.getNickName());
    dump.add("connectTime",connectTime);
    dump.add("enqDelayTime",enqDelayTime);
    dump.add("hupAfter",hangupAfter);
    dump.add("hupFor",hangupFor);
    dump.add("forcedtr",forcedtr);
    dump.add("dtrforce",dtrforce);
    dbg.WARNING(dump.asParagraph());
    super.xterm.framer= Techuliner.New();
  }

  public Techulator(TerminalInfo termInfo) {////public for load by reflection, uses config from server
    super(termInfo);//covers all serial startup stuff and line level protocol
    dbg=ErrorLogStream.getForClass(this.getClass());

    delayer=Alarmer.New(0,this);
  }
//  public Techulator() {////public for load by reflection, uses config from server

//  }

}
//$Id: Techulator.java,v 1.16 2003/12/10 02:16:53 mattm Exp $
