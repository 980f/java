package net.paymate.ivicm.ec3K;
/**
* Title:        $Source: /cvs/src/net/paymate/ivicm/ec3K/Command.java,v $
* Description:  @todo replace with subvariant of visaBuffer
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Command.java,v 1.7 2003/01/14 14:55:25 andyh Exp $
*/

import net.paymate.util.*;
public class Command {

  final static int StartFrame=Ascii.STX;
  final static int EndFrame  =Ascii.ETX;
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
    outgoing[0]=(byte)StartFrame;
    System.arraycopy(block,0,outgoing,1,blocksize);
    outgoing[1+blocksize]=(byte)EndFrame;
  }

}
//$Id: Command.java,v 1.7 2003/01/14 14:55:25 andyh Exp $
