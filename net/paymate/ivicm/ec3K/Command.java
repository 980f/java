/**
* Title:        Command
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Command.java,v 1.2 2001/07/19 01:06:49 mattm Exp $
*/
package net.paymate.ivicm.ec3K;
//import net.paymate.util.ErrorLogStream;

public class Command {
//  static final ErrorLogStream dbg=new ErrorLogStream("EC3K.Command");

  final static int StartFrame=2;  //^b, STX
  final static int EndFrame  =3;  //^c, ETX
  final static int RcvSize   =256;

  public byte[] outgoing;

  public String errorNote;
  public Service owner; //for generic event hacking.

  protected void callBack(String forError){//enumerated callbacks
    errorNote=forError;
  }

  public Command(int opcode, byte [] block, String forError){
    callBack(forError);

    int blocksize=block.length;
    //should find a maxsize to check against.
    outgoing= new byte[3+blocksize];
    outgoing[0]=StartFrame;
    System.arraycopy(block,0,outgoing,1,blocksize);
    outgoing[1+blocksize]=EndFrame;
  }

}
//$Id: Command.java,v 1.2 2001/07/19 01:06:49 mattm Exp $
