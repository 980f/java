package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IceCommand.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.15 $
 */
import net.paymate.data.AsciiBuffer;
import net.paymate.util.LogSwitch;
import net.paymate.util.Ascii;

public class IceCommand extends AsciiBuffer {
  final static String protocolId="T0"; //gets added in by sending routine.
   //first char is device within the terminal
  final static byte Proceed=Ascii.POUND;//command pacing character

  final static byte KeyInput=Ascii.K;
  final static byte FormInput=Ascii.F;
  final static byte Printer=Ascii.P;
  final static byte Sigcap=Ascii.S;
  final static byte Swiper=Ascii.C;
  final static byte Pinpad=Ascii.Z;
  final static byte System=Ascii.H;

  final static byte StringInput=Ascii.I;
  final static byte Logger=Ascii.L;

  //second char is command to that device
  final static byte Prompt=Ascii.P;  //display device,pinpad announce
  final static byte Echo=Ascii.E;    //display device keyboard feedback

  final static byte Start=Ascii.ONE;   //enable input from component (if possible)
  final static byte Stop=Ascii.ZERO;   //abort input and shut down component (if possible)

  final static byte PrintLine=Ascii.L;//printer
  final static byte FormFeed=Ascii.F; //printer
  final static byte Version=Ascii.V; //printer

  final static byte GetBlock=Ascii.G;//sigcap

  final static byte Ahem=Ascii.I; //system

  final static byte defaultOutLength=20;
  final static int maxReceivePacket=1372+100;//copied from C code + big margin

  public boolean isValid(){
    return used()>=2;//at least two bytes for each command. can get fancier later.
  }
// caching the command led to all sorts of problems with ENQ and application startups
//  private static IceCommand ahemCommand;
  static IceCommand Ahem(){
    IceCommand ahemCommand;
//    if(ahemCommand==null){
      ahemCommand=new IceCommand(2);
//    }
//    if(!ahemCommand.isValid()){//restore in case buffer got wiped by iceterminal.commandFailure
      ahemCommand.append(System);
      ahemCommand.append(Ahem);
//    }
    return ahemCommand;
  }

  private static IceCommand pollCommand;
  static IceCommand Poll(){
    if(pollCommand==null){
      pollCommand=new IceCommand(1);
    }
    if(pollCommand.used()<1){//the only valid command that is only one character :(
      pollCommand.append(Ascii.POUND);
    }
    return pollCommand;
  }

  static IceCommand Simple(byte icedevice,byte opcode){
    IceCommand newone=new IceCommand(2);
    newone.append(icedevice);
    newone.append(opcode);
    return newone;
  }

  static IceCommand Enabler(byte devicecode,boolean beon){
    return Simple(devicecode,beon?Start:Stop);
  }

  static IceCommand Create(int bigone) {
    return new IceCommand(bigone);
  }

  static IceCommand Create() {
    return Create(defaultOutLength);
  }

  private IceCommand (int bigone) {
    super(bigone);
  }

  private IceCommand () {
    super(defaultOutLength);
  }

  public static IceCommand FromString(String rawcommand){
    IceCommand raw=IceCommand.Create(rawcommand.length());
    raw.append(rawcommand);
    return raw;
  }

  ///////////////////
  /**
   * used to remove conflicting commands from queue
   * if this equals(rhs) then rhs is removed.
   * */
  public boolean equals(Object rhs){
    if(rhs==null  || ! (rhs instanceof IceCommand)|| this==rhs){//exclude trash and repeats
      return true;
    }
    if(this.buffer[0]==System && this.buffer[1]==Ahem){// query for terminal type
      return true; //... wipes queue. it is only sent as a "reset protocol"
    }
    IceCommand inQ= (IceCommand ) rhs;//rename for sanity's sake!
    if(this.buffer[0]==inQ.buffer[0]){
      switch(this.buffer[0]){
        case Sigcap:
        case Swiper:
        case Pinpad: //capture device commands
          switch(this.buffer[1]){
            case Start:
            case Stop:  //no point in sending a command we are about to override.
              return true; //new command displaces old
            default: //configuration, other requests
              return false;//leaves existing commands in queue
          }
          //break;
        case KeyInput:{
          if(inQ.buffer[1]=='E'){//any command overrides echo
            return true; //cause we are changing the question or echoing a modified value.
          }
          if(inQ.buffer[1]==this.buffer[1]){//like commands override previous
            return true;
          }
        } break;
      }//type
    }
    return false;//default is to leave exisitng command in queue
  }
}
//$Id: IceCommand.java,v 1.15 2003/08/08 17:31:05 andyh Exp $
