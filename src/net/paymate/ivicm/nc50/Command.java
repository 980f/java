package net.paymate.ivicm.nc50;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/nc50/Command.java,v $
 * Description:  enCrypt50, enough to be a DisplayDevice.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.ivicm.*;
import net.paymate.serial.*;
import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;
import net.paymate.lang.ContentType;

/*package*/ class Command extends Packetizer {
  boolean acked;//execution complete,not formatting.

  private AsciiBuffer buf;
  private int opcode;//used for comparing

  public final static int Reset=      1;//aka reset
  public final static int JustDisplay= 2;
  public final static int GetNumeric=  3;
  public final static int DisableInput=4;
  public final static int GetMoney=    5;
  public final static int GetPassword= 6;
  public final static int Toggler=7;
  public final static int GetAlphaNum= 8;
  public final static int GetKey= 9;
  //10 setmask
  //11 setkeyboard
  public final static int HereisKey=11; //only for function keys
  public final static int HereisString=13; //for string input.

  /**
   * maximum size a receiver packet's body will be.
   */
  public static int MaxReceivedBytes(){
    return 3+20;
  }

   static boolean stringInputter(int opcode){
    switch(opcode){
    case GetNumeric:
    case GetPassword:
    case GetMoney:
    case GetAlphaNum:
    return true;
    default:
    return false;
    }
  }

  boolean stringInputter(){
    return stringInputter(opcode);
  }

  /**
   * @return true if command results in device waiting for input
   */
  boolean isInputter(){
    return opcode==GetKey || stringInputter();
  }

  /**
   * used to remove commands from queue. "this" is the new command, arg is from queue
   * @return true to remove @param any based on what 'this' is.
   */
  public boolean equals(Object any){
    if(any instanceof Command ){
      Command inq=(Command)any;
      return stringInputter()&&inq.stringInputter()
      || inq.opcode==opcode;
    }
    return false;//copacetic commands
  }

  /**
   * trusting prefix for "type of command"
   */
  public boolean isa(int opcode){
    return this.opcode==opcode;
  }

  public Packetizer packer(){
    return this;
  }

  public Buffer body(){
    return buf;
  }

  private Command(int commandcode,int sizeofargs){
    super(Ascii.STX, Ascii.ETX,true,MathX.INVALIDINTEGER);
    buf=AsciiBuffer.Newx(3+sizeofargs);
    attachTo(buf);
    buf.appendNumber(2,opcode=commandcode);
    buf.append('.');//command separator
  }

/**
 * create a command buffer, @param commandcode is saved for uniqueness check and is also put into the command
 */
  public static Command Command(int commandcode,int sizeofargs) {
    return new Command(commandcode,sizeofargs);
  }

  public static Command String(int commandcode,String onearg) {
    Command cmd=Command(commandcode,StringX.lengthOf(onearg));
    cmd.buf.append(onearg);
    return cmd;
  }

  public static Command GetInput(int commandcode,String prompt) {
    Command cmd=Command(commandcode,2+2+StringX.lengthOf(prompt));
    cmd.buf.appendNumber(2,1);//minimum of 1, wish we could say zero.
    cmd.buf.appendNumber(2,20);//always ask for max allowed.
    cmd.buf.append(prompt);
    return cmd;
  }

  private static int codeFor(ContentType ct){
    switch(ct.Value()){
      case ContentType.arbitrary : return Command.GetAlphaNum ;
      case ContentType.purealpha : return Command.GetAlphaNum ;
      case ContentType.alphanum  : return Command.GetAlphaNum ;
      case ContentType.password  : return Command.GetPassword ;
      case ContentType.decimal   : return Command.GetNumeric ;
      case ContentType.hex       : return Command.GetAlphaNum ;
      case ContentType.money     : return Command.GetMoney ;
      case ContentType.ledger    : return Command.GetMoney ;
      case ContentType.cardnumber: return Command.GetNumeric ;
      case ContentType.expirdate : return Command.GetNumeric ;
      case ContentType.micrdata  : return Command.GetNumeric ;
      case ContentType.date      : return Command.GetNumeric ;
      case ContentType.time      : return Command.GetNumeric ;
      case ContentType.zulutime  : return Command.GetNumeric ;
      case ContentType.taggedset : return Command.GetAlphaNum ;
      case ContentType.select    : return Command.GetKey ;
      default:
      case ContentType.unknown   : return Command.GetKey ;
    }
  }

  public static Command GetAnswer(String prompt,String suggestion,ContentType ct){
    int code=codeFor(ct);
    switch (code) {
      case Command.GetPassword:
      case Command.GetAlphaNum:
      case Command.GetMoney:
      case Command.GetNumeric:
        return GetInput(code,prompt);//suggstoin not yet used.
      case Command.GetKey:
        return String(code,prompt);
      default: //software uerror
        return String(JustDisplay,"nc50 error:"+code);
    }
  }

  public static Command forReception(){
    return Command(0,MaxReceivedBytes());
  }

  /**
   * for simulation purposes:
   */
  public static Command fakeKeyStroke(char keystroke){
    Command cmd=Command(HereisKey,2);
    cmd.buf.append(keystroke);
    cmd.buf.appendNumber(1,0);//unknown filler of zero
    return cmd;
  }

  public static Command fakeString(String stuffed){
    return String(HereisString,stuffed);
  }

  public static Command cancelSession(){
    return Command(Reset,0);//has an optional display, but we don't use that
  }

  public static String opDecoder(int opcode){
    switch (opcode) {
      case Reset        : return "Reset";
      case JustDisplay  : return "JustDisplay";
      case GetNumeric   : return "GetNumeric";
      case DisableInput : return "DisableInput";
      case GetMoney     : return "GetMoney";
      case GetPassword  : return "GetPassword";
      case Toggler      : return "Toggler";
      case GetAlphaNum  : return "GetAlphaNum";
      case GetKey       : return "GetKey";
  //10 setmask
  //11 setkeyboard
      case HereisKey    : return "HereisKey"; //only for function keys
      case HereisString : return "HereisString"; //for string input.
    }

    return Ascii.bracket(opcode);
  }

  public String forSpam(){
    return opDecoder(opcode)+ (isInputter()?",gets string.":".");
  }

  public String toSpam(){
    return Ascii.bracket(buf.packet());
  }

}
//$Id: Command.java,v 1.10 2003/10/01 04:23:44 andyh Exp $