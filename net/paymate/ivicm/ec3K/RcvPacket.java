/**
* Title:        RcvPacket
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: RcvPacket.java,v 1.5 2001/10/22 23:33:38 andyh Exp $
*/
package net.paymate.ivicm.ec3K;

import net.paymate.authorizer.Packet;

public class RcvPacket extends Packet {

//  public void fixstx(){//bug in jpos101 mcode
//    append(buffer[0]);
//    buffer[0]=EC3K.STX;
//  }

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
   public boolean isOk(){//usually overridden
    //parens for emphasis:
    return (ptr()>0) && (bight(0)==EC3K.STX) && (last()==EC3K.ETX) && (Size()==3 || (paysize()+5==nexti));
  }

  public RcvPacket(int maxsize) {
    super(maxsize);
  }

//  public RcvPacket() {
//    super();
//  }

}
//$Id: RcvPacket.java,v 1.5 2001/10/22 23:33:38 andyh Exp $
