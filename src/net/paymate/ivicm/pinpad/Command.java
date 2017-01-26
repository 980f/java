package net.paymate.ivicm.pinpad;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/Command.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.14 $
 * @todo contain an AsciiBuffer rather than extending it. CODED but not tested. +_+
 * extending Packetizer WOULD be reasonable.
 */

import net.paymate.ivicm.*;
import net.paymate.serial.*;
import net.paymate.lang.MathX;
import net.paymate.util.*;
import net.paymate.data.*;

/*package*/ class Command {
  private Packetizer packer;
  public  AsciiBuffer buffer;
  private String prefix;//used for comparing
  boolean wait4EOT;
  boolean getsResponse;

  public final static String SetPupMessage= "0A";
  public final static String ConfigKeys=    "0B";
  public final static String SetBaud=       "13";
  public final static String SetPinMode=    "15";
  public final static String CancelSession= "72";

  public final static String DisplayOne=    "Z2";
  public final static String DisplayMany=   "Z3";
  public final static String Cancel=        "Z7";

  public final static String GetKeystroke=  "Z42";
  public final static String GetString=     "Z50";
/**
 * maximum packet of any kind is receive string with max string legnth:
 * note: stx/etx/lrc is not the responsibility of this class to perceive or
 * compensate for, beyond indicating which char set is in use (stx vs SI).
 */
 //this is the max used, not the max possible for the device! must change when pinpad functionality is added.
  public final static int MaxLength= 52;//"Z51".length()+49;

  private final boolean isDisplayCommand(){
    return prefix.equals(DisplayOne) || prefix.equals(DisplayMany);
  }


  /**
   * used to remove commands from queue. "this" is the new command, arg is from queue
   * @return true to remove @param any based on what 'this' is.
   * note: driver removes all input commands from queue when ANY command is posted
   */
  public boolean equals(Object any){
    if(any instanceof Command ){
      Command inq=(Command)any;
      return inq.getsResponse ||  //remove all commands requiring input.
      //any display command conflicts with any other display command.
      (isDisplayCommand() && inq.isDisplayCommand());
    }
    return false;//copacetic commands
  }

  /**
   * trusting prefix for "type of command"
   */
  public boolean isa(String stdprefix){
    return prefix.equals(stdprefix);
  }

  private Command needsEOT(){//for when we UNconditionally expect one
    this.wait4EOT=true;
    return this;
  }

  private Command getsResponse(){
    this.getsResponse=true;
    return this;
  }

  public Packetizer packer(){
    return packer;
  }

  public Buffer body(){
    return packer.body();
  }

  private Command(String commandcode,int sizeofargs,boolean stxer){
    buffer= (AsciiBuffer) AsciiBuffer.Newx(commandcode.length()+sizeofargs);
    buffer.append(prefix=commandcode);//command for display string
    packer=Packetizer.Ascii(-1).Config(stxer?Ascii.STX:Ascii.SI, stxer?Ascii.ETX:Ascii.SO,true,MathX.INVALIDINTEGER);
    packer.attachTo(buffer);
  }

/**
 * create a command buffer, @param commandcode is saved for uniqueness check and is also put into the command
 */
  public static Command Normal(String commandcode,int sizeofargs) {
    return new Command(commandcode,sizeofargs,true);
  }

  public static Command Config(String commandcode,int sizeofargs) {
    return new Command(commandcode,sizeofargs,false);
  }

  public static Command mkDisplay(String monstratus){
    Command cmd=Normal(DisplayOne,1+monstratus.length());
    cmd.buffer.append(Ascii.SUB); //clear present display, else would append and scroll
    cmd.buffer.append(monstratus);//add body
    return cmd;
  }

  public static Command mkDisplayAppend(char c){
    Command cmd=Normal(DisplayOne,1);
    cmd.buffer.append(String.valueOf(c));
    return cmd;
  }

  private Command appendTimeout(int timeoutseconds){
    if(timeoutseconds>255){
      timeoutseconds=255;
    }
    if(timeoutseconds<0){
      timeoutseconds=0;
    }
    buffer.appendNumber(3,timeoutseconds);//ec100 tolerates excessleasing zeroes
    getsResponse(); //this is the thing that the timeout times out.
    return this;
  }

  public static Command getKeyStroke(int timeoutseconds){
    Command newone=Normal(GetKeystroke,3);//maximum 255!
    return newone.appendTimeout(timeoutseconds);
  }

  public static Command mkEnableKey(){//enable single keystrokes
    return getKeyStroke(0);
  }
/**
 * @param echomode 0=for password, 1=digits, 2=no echo
 */
  public static Command mkEnableString(int echomode,int timeoutseconds){
    Command cmd=Normal(GetString,6);
    cmd.buffer.appendNumber(1,echomode);
    cmd.appendTimeout(timeoutseconds);
//    cmd.buffer.appendNumber(2,20); //max str len to return.1..49 legal, field can be omitted
//anything with a timeout sets this already:    cmd.getsResponse();
    return cmd;
  }

  public static Command mkEnableString(){
    return mkEnableString(1,120); //normal input, wait two minutes.
  }

  public static Command configDukpt(){
    String pinmode="DKEY";
    Command cmd=Config(SetPinMode,pinmode.length());
    cmd.buffer.append(pinmode);
    return cmd;
  }

  public static Command configBaud(int baud){
    Command cmd=Config(SetBaud,1);
    cmd.buffer.append('1'+MathX.log2(baud/1200));//character '1' through '4' are legal
    return cmd;
  }

  /**
   * @param setting true makes function keys work
   */
  public static Command configFunctionKey(boolean setting){
    Command cmd= Config(ConfigKeys,1);
    cmd.buffer.append(setting?'3':'2');
    cmd.needsEOT();
    return cmd;
  }

  /**
   * @param setting if true makes CANCEL key work as its name suggests.
   */
  public static Command configCancelKey(boolean setting){
    Command cmd= Config(ConfigKeys,1);
    cmd.buffer.append(setting?'1':'0');
    cmd.needsEOT();
    return cmd;
  }

  public static Command configBrag(String bragger){
    Command cmd=Config(SetPupMessage,bragger.length());
    cmd.buffer.append(bragger);
    cmd.needsEOT();
    return cmd;
  }

  public static Command cancelSession(){
    return Config(CancelSession,0); //does not NEED EOT, but may generate one
  }

  /**
   * @param arg 0 is annoying, 1 disables other people, 2 is required at powerup.(but see later comments)
   */
  public static Command mkCancel(int arg){
    Command cmd= Normal(Cancel,1);
    cmd.buffer.appendNumber(1,arg);
    return cmd;
  }

// bogus stuff from manual. Called this mandatory when it isn't even legal
//  public static Command powerupReset(){
//    return mkCancel(2);
//  }

  public static Command powerupClear(){
    return mkCancel(1);
  }

  public String forSpam(){
    return prefix;
  }

}
//$Id: Command.java,v 1.14 2003/10/01 04:23:44 andyh Exp $