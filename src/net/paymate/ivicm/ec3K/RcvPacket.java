package net.paymate.ivicm.ec3K;
/**
* Title:        RcvPacket
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: RcvPacket.java,v 1.10 2003/01/07 00:52:51 andyh Exp $
*/

import net.paymate.data.*;//Packet;
import net.paymate.util.Ascii;

public class RcvPacket extends Packet {

  public int response(){//status like return from device
    return bight(2);
  }

  public int incode(){//packet type from device.
    return bight(1);
  }

  public int paysize(){
    return bight(3); //is an unsigned byte, java always sign extends
  }

  public StringBuffer payload(){
    return super.subString(4,paysize());
  }

  //overrides:
   public boolean isOk(){
    //parens for emphasis:
    return (ptr()>0) && (bight(0)==Ascii.STX) && (last()==Ascii.ETX) && (Size()==3 || (paysize()+5==nexti));
  }

  public boolean isComplete() {
    return isOk();
  }

  public RcvPacket(int maxsize) {
    super(maxsize);
  }

}
//$Id: RcvPacket.java,v 1.10 2003/01/07 00:52:51 andyh Exp $
