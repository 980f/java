package net.paymate.ivicm.nc50;

import net.paymate.serial.*;
import net.paymate.data.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/nc50/AckNakReceiver.java,v $
 * Description:  implementes two flavors of stx/etx encodings
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */
import net.paymate.util.*;
/**
 * @--deprecated will convert to PacketServer reals soon now
 */
public class AckNakReceiver extends PacketReceiver {
  int initialTimeout;

  boolean acknaked;
//  int ackornak;
  int nakers[]={Ascii.ACK,Ascii.NAK,Ascii.ENQ,Ascii.EOT};//default is superset of most protocols.

/**
 * @param nakchars chars that are by them selves a complete response.
 */
  public AckNakReceiver setnakers(int []nakchars){
    nakers=nakchars;
    return this;
  }

  /**
   * stores special one char response.
   * terminates reception.
   */
  boolean setAck(int cc){//trust that caller has validated the character
    super.controlPacket(cc);
    acknaked=true;
    return true; //byte was legitimate.
  }

  boolean isAckNak(){
    return acknaked;
  }

/**
 * FUE @return whether receiver has just seen an ACK.
 */
  boolean isAcked(){
    return isAckNak()&& packer.body().bight(0)==Ascii.ACK;
  }
/**
 * @return whether receiver has just seen protocol char @param
 */
  boolean is(int naq){
    return isAckNak()&&packer.body().bight(0)==naq;
  }

   /**
   * @return whether we have a well formed packet.
   */
  public boolean isWellFormed(){
    return isAckNak() || super.isWellFormed();
  }

  /**
   * @return this. Prepare for new input.
   */
  public PacketReceiver restart(){
    acknaked=false;
    return super.restart();
  }

  /**
 * @return whether the given bite is the possible control char.
 */
  protected boolean isAckChar(int bite){
    for (int cf=nakers.length;cf-->0;){
      if(nakers[cf]==bite){
        return true;
      }
    }
    return false;
  }

  /** deal with ack nak recognition.
   * @see PacketReceiver.receive()
   */
  public boolean receive(int bite){
    if( ! escaping() && isAckChar(bite)){
      return setAck(bite);
    } else {
      return super.receive(bite);
    }
  }

  private AckNakReceiver (Packetizer packer){
    super(packer);
  }

  public static AckNakReceiver Create(Packetizer packer){
    return new AckNakReceiver(packer);
  }

}
//$Id: AckNakReceiver.java,v 1.6 2003/01/14 14:55:25 andyh Exp $