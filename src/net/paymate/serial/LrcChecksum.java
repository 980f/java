package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/LrcChecksum.java,v $
 * Description:  longitudinal redundancy check for Packetizer classes
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.data.*;
import net.paymate.util.Ascii;

public class LrcChecksum extends Checksum {
  byte lrcsum;
  public String algorithm(){
    return "LRC.Longitudinal Redundancy Check";
  }

  /**
 * @return checksum value
 */
  public byte[] Sum(){
    return Ascii.byteAsArray(lrcsum);
  }

  public Checksum reset(){
    lrcsum=0;
    return super.reset();
  }

  public Checksum checksum(int onechar){
    if(onechar>=0){
      lrcsum ^= onechar;
    }
    return this;
  }

  /**
   * @return number of bytes in checksum
   */
  public int length(){
    return 1;
  }

  /**
   * makes a new one
   */
  public Checksum Clone(){
    return new LrcChecksum();
  }

  public static LrcChecksum Create(){
    return new LrcChecksum();
  }

}
//$Id: LrcChecksum.java,v 1.8 2003/06/19 15:22:00 andyh Exp $
