package net.paymate.ivicm.et1K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/Command.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Command.java,v 1.27 2001/11/14 01:47:49 andyh Exp $
*/

import net.paymate.util.ErrorLogStream;
import net.paymate.util.*;
import net.paymate.util.timer.*;

//package specific
class Command {
  static final ErrorLogStream dbg=new ErrorLogStream(Command.class.getName());
//  boolean lockedByDsr=false; //+_+ protect, belongs to driver
  final static int biggerThanNeeded=256;
  //formatted command
  boolean processed=false;

  protected LrcBuffer outgoing=LrcBuffer.NewSender(biggerThanNeeded);//constrcutors depend upon this.
  public LrcBuffer outgoing(){
    return outgoing;
  }

  public String errorNote;
  public boolean isaPoller=false;//used to omit messages from debug stream
  public boolean highPriority=false; //where in queue this goes.

/**
 * useful when reissuing a command due to a glitch.
 */
  public Command boost(){
    highPriority=true;
    return this;
  }

  public Command next(){//@see BlockCommand for why this exists.
    return null;
  }

  public Command restart(){//called to restart
// is there anythign we should erase?
    incoming=null;//+_+ check for NPE dangers
    return this;
  }

  //loaded by service before handing to hardware:
  public Callback onReception;
  public StopWatch responseTime=new StopWatch(false);//false keeps it from running
  public Service  service;  //for posting Error events

  //loaded by hardware before posting back to caller:
  public Reply incoming;
  public boolean failed;

  public int rcvLength(){
    return incoming!=null?incoming.sizeCode():0;
  }

  public int opCode(){
    return outgoing!=null?outgoing.opCode():-1;
  }

  public int response(){
    return incoming!=null?incoming.response():-1;
  }

  public static final boolean nothingThere(int response){
    return (response == Codes.NO_DATA_READY || response == Codes.CONTROL_NOT_DISPLAYED);//no data || invalid mode
  }

  public boolean nothingThere(){
    return nothingThere(response());
  }

/**
 * @param offset bytes to ignore from front of payload.
  */
  public byte [] payload(int offset){
    return incoming!=null ? incoming.payload(offset) : new byte[0];
  }

  public byte [] payload(){
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

  public static final LrcBuffer Max(){
    LrcBuffer newone=LrcBuffer.NewSender(biggerThanNeeded);
    newone.append(Codes.STX);
    newone.append(0);//end() now copmutes these, when 0
    return newone;
  }

  public static final LrcBuffer Op(int opcode){
    LrcBuffer newone=LrcBuffer.NewSender(biggerThanNeeded);
    newone.append(Codes.STX);
    newone.append(0);//end() now copmutes these, when 0
    newone.append(opcode);//end() now copmutes these, when 0
    return newone;
  }

  public static final LrcBuffer Buffer(int payloadsize){
    LrcBuffer newone=LrcBuffer.NewSender(biggerThanNeeded);
    newone.append(Codes.STX);
    newone.append(payloadsize+3);//3==stx,length,lrc
    return newone;
  }

  public static final LrcBuffer Buffer(int opcode,int payloadsize){
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

  public Command(int opcode, int moreop, byte [] legacy, String forError){
    this(opcode, moreop, legacy, legacy.length , forError);
  }

  public Command(int opcode, byte [] legacy, int leglength, String forError){
    prep(forError);
    int size=4+leglength;
    outgoing.start(size);
    outgoing.append(Codes.STX);
    outgoing.append(size);
    outgoing.append(opcode);
    fill(legacy,leglength);
  }

  public Command(int opcode, byte [] legacy,String forError){
    this(opcode,legacy,legacy.length,forError);
  }

  public Command(int opcode, int moreop, int thirdByte, String forError){
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

  public static final LrcBuffer JustOpcode(int opcode){
    LrcBuffer newone=Buffer(opcode,0);
    newone.end();
    return newone;
  }

  public static final LrcBuffer OpArg(int opcode, int moreop){
    LrcBuffer newone=Buffer(opcode,1);
    newone.append(moreop);
    newone.end();
    return newone;
  }

  public static final LrcBuffer OpTwoArg(int opcode, int moreop,int thirdeye){
    LrcBuffer newone=Buffer(opcode,2);
    newone.append(moreop);
    newone.append(thirdeye);
    newone.end();
    return newone;
  }

  public Command(int opcode, int moreop, String forError){
    this(opcode,moreop,(byte[])null,0,forError);
  }

  public Command(int opcode, String forError){
    this(opcode, (byte[])null, 0, forError);
  }

  public Command(LrcBuffer preFormatted, String forError){
    prep(forError);
    outgoing=preFormatted;
    //--it was nice to make the soruce do this each time to make
    // an obvious end to command creation:    outgoing.end();
  }

  protected Command(){//used by BlockCommand
  //
  }

}
//$Id: Command.java,v 1.27 2001/11/14 01:47:49 andyh Exp $
