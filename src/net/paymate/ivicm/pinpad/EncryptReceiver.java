package net.paymate.ivicm.pinpad;

import net.paymate.serial.*;
import net.paymate.data.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/EncryptReceiver.java,v $
 * Description:  implementes two flavors of stx/etx encodings
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */
import net.paymate.util.*;
import net.paymate.lang.MathX;

public class EncryptReceiver extends PacketReceiver {
  int initialTimeout;

  boolean acknaked;
  int ackornak;

  void setAck(int cc){//trust that caller has validated the character
    ackornak=cc;
    acknaked=true;
    setDone();
  }

  boolean isAckNak(){
    return acknaked;
  }

  boolean isAcked(){
    return isAckNak()&&ackornak==Ascii.ACK;
  }


   /**
   * @return whether we have a well formed packet.
   */
  public boolean isWellFormed(){
    return isAckNak() || super.isWellFormed();
  }

  public PacketReceiver restart(){
    acknaked=false;
    return super.restart();
  }

  private EncryptReceiver configStyle(){
    packer.Config(Ascii.SI,Ascii.SO,true,MathX.INVALIDINTEGER);
    return this;
  }

  private EncryptReceiver normalStyle(){
    packer.Config(Ascii.STX,Ascii.ETX,true,MathX.INVALIDINTEGER);
    return this;
  }

  /** auto detect incoming packet style and adjust receiver.
   * @see PacketReceiver.receive()
   * @todo (pro forma) only switch styles when awaiting header.
   * i.e. make sure we don't misfire on lrc bytes +++
   */
  public boolean receive(int bite){//ack nak etc. already handled
    switch (bite) {//auto switch between packet styles
      case Ascii.SI:  this.configStyle();  break;
      case Ascii.STX: this.normalStyle();  break;
    }
    return super.receive(bite);
  }

  public EncryptReceiver(Packetizer packer){
    super(packer);
  }

  public static EncryptReceiver Create(Packetizer packer){
    return new EncryptReceiver(packer);
  }

}
//$Id: EncryptReceiver.java,v 1.7 2003/07/27 05:35:06 mattm Exp $