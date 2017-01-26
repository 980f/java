package net.paymate.ivicm.et1K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/Command.java,v $
* Description:  state and formatters for commands for an ncr5992/ivicm entouch
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Command.java,v 1.36 2004/02/26 18:40:50 andyh Exp $
*/

import net.paymate.util.ErrorLogStream;
import net.paymate.util.*;
import net.paymate.util.timer.*;

//package specific
class Command implements Comparable {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(Command.class);
  //  boolean lockedByDsr=false; //+_+ protect, belongs to driver
  final static int biggerThanNeeded=256;
  //formatted command
  boolean processed=false;
  int retried=0;

  protected LrcBuffer outgoing=LrcBuffer.NewSender(biggerThanNeeded);//constrcutors depend upon this.
  LrcBuffer outgoing(){
    return outgoing;
  }

  int latencyTicks=0; //how long command is expected to take, 0 = default/minimum
  String errorNote;
  boolean isaPoller=false;//used to omit messages from debug stream

  final static int priorityFront=10;  //such as application config
  final static int priorityExpress=1; //"greatest among equals"
  final static int priorityInit=100;  //such as powerup config

  int priority=0; //lowest
  public int compareTo(Object o){
    return this.priority-((Command)o).priority;
  }

  public Command boostTo(int newpriority){
    priority=newpriority;
    return this;
  }

  protected Command next(){//@see BlockCommand for why this exists.
    return null;
  }

  protected Command restart(){//called to restart
    // is there anythign we should erase?
    if(++retried<3){
      incoming=null;//+_+ check for NPE dangers
      return this;
    } else {
      service.PostFailure("gave up on retries");
      return null;
    }
  }

  //loaded by service before handing to hardware:
  Callback onReception;
  StopWatch responseTime=new StopWatch(false);//false keeps it from running
  Service  service;  //for posting Error events

  //loaded by hardware before posting back to caller:
  Reply incoming;
  boolean failed;

  int rcvLength(){
    return incoming!=null?incoming.sizeCode():0;
  }

  int opCode(){
    return outgoing!=null?outgoing.opCode():-1;
  }

  int response(){
    return incoming!=null?incoming.response():-1;
  }

  boolean forceResponse(int respcode){
    return incoming!=null && incoming.forceResponse(respcode);
  }

  static final boolean nothingThere(int response){
    return (response == ResponseCode.NO_DATA_READY || response == ResponseCode.CONTROL_NOT_DISPLAYED);//no data || invalid mode
  }

  boolean nothingThere(){
    return nothingThere(response());
  }

  /**
  * @param offset bytes to ignore from front of payload.
  */
  byte [] payload(int offset){
    return incoming!=null ? incoming.payload(offset) : new byte[0];
  }

  byte [] payload(){
    return payload(0);
  }

  protected void prep(String comment){
    onReception=null;
    isaPoller=false;
    errorNote=comment;
  }

  protected void fill(byte [] legacy, int leglength){
    for(int i=0;i<leglength;i++){//must be ordered
      outgoing.append(legacy[i]);
    }
    outgoing.end();
  }

  static final LrcBuffer Max(){
    LrcBuffer newone=LrcBuffer.NewSender(biggerThanNeeded);
    newone.append(Codes.STX);
    newone.append(0);//end() now copmutes these, when 0
    return newone;
  }

  static final LrcBuffer Op(int opcode){
    LrcBuffer newone=LrcBuffer.NewSender(biggerThanNeeded);
    newone.append(Codes.STX);
    newone.append(0);//end() now copmutes these, when 0
    newone.append(opcode);//end() now copmutes these, when 0
    return newone;
  }

  static final LrcBuffer Buffer(int payloadsize){
    LrcBuffer newone=LrcBuffer.NewSender(biggerThanNeeded);
    newone.append(Codes.STX);
    newone.append(payloadsize+3);//3==stx,length,lrc
    return newone;
  }

  static final LrcBuffer Buffer(int opcode,int payloadsize){
    LrcBuffer newone=LrcBuffer.NewSender(payloadsize+4);
    newone.append(Codes.STX);
    newone.append(newone.Size());
    newone.append(opcode);
    return newone;
  }

  private Command(int opcode, int moreop, byte [] legacy, int leglength, String forError){
    prep(forError);
    int size=5+leglength;
    outgoing.start(size);
    outgoing.append(Codes.STX);
    outgoing.append(size);
    outgoing.append(opcode);
    outgoing.append(moreop);

    fill(legacy,leglength);
  }

  Command(int opcode, int moreop, byte [] legacy, String forError){
    this(opcode, moreop, legacy, legacy.length , forError);
  }

  Command(int opcode, byte [] legacy, int leglength, String forError){
    prep(forError);
    int size=4+leglength;
    outgoing.start(size);
    outgoing.append(Codes.STX);
    outgoing.append(size);
    outgoing.append(opcode);
    fill(legacy,leglength);
  }

  Command(int opcode, byte [] legacy,String forError){
    this(opcode,legacy,legacy.length,forError);
  }

  Command(int opcode, int moreop, int thirdByte, String forError){
    prep(forError);
    int size=6;
    outgoing.start(size);
    outgoing.append(Codes.STX);
    outgoing.append(size);
    outgoing.append(opcode);
    outgoing.append(moreop);
    outgoing.append(thirdByte);
    outgoing.end();
  }

  static final LrcBuffer JustOpcode(int opcode){
    LrcBuffer newone=Buffer(opcode,0);
    newone.end();
    return newone;
  }

  static final LrcBuffer OpArg(int opcode, int moreop){
    LrcBuffer newone=Buffer(opcode,1);
    newone.append(moreop);
    newone.end();
    return newone;
  }

  static final LrcBuffer OpTwoArg(int opcode, int moreop,int thirdeye){
    LrcBuffer newone=Buffer(opcode,2);
    newone.append(moreop);
    newone.append(thirdeye);
    newone.end();
    return newone;
  }

  Command(int opcode, int moreop, String forError){
    this(opcode,moreop,(byte[])null,0,forError);
  }

  Command(int opcode, String forError){
    this(opcode, (byte[])null, 0, forError);
  }

  Command(LrcBuffer preFormatted, String forError){
    prep(forError);
    outgoing=preFormatted;
  }

  protected Command(String ernote){//used by BlockCommand
    this.errorNote=ernote;
  }

  public void log(ErrorLogStream otherbugger,String note){
    if(otherbugger.willOutput(isaPoller?dbg.VERBOSE:dbg.WARNING) ){
      dbg.VERBOSE(note+errorNote);
    }
  }

}
//$Id: Command.java,v 1.36 2004/02/26 18:40:50 andyh Exp $
