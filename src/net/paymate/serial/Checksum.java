package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/Checksum.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 * @see serial.LrcChecksum
 */

import net.paymate.data.*;
public class Checksum { // trivial base class

  public String algorithm(){
    return "None.";
  }

  public String toSpam(){
    return "No Checksum";
  }

  protected Buffer testees;//bytes to be tested
/**
 * @return checksum value
 */
  public byte[] Sum(){
    return new byte[0];
  }
  /**
   * @return this, after summing in one more char as returned by an InputStrea.read();
   */
  public Checksum checksum(int onechar){return this;}

  /**
   * @return this, after summing in an array of bytes
   */
  public Checksum checksum(byte[] ba) {
    for (int i = 0; i < ba.length; i++) { //#forward in case algorithm is order sensitive
      checksum(ba[i]);
    }
    return this;
  }

  /**
   * @return this, after summing in a buffer full of chars.
   */
  public Checksum checksum(Buffer buffer){
    checksum(buffer.packet());
    return this;
  }

  /**
   * @return this, after clearing internal
   */
  public Checksum reset(){
    if(testees==null){
      testees=Buffer.New(length());
    }
    testees.reset();
    return this;
  }

  /**
   * @return number of bytes in checksum, 0..4..8
   */
  public int length(){
    return 0;
  }

  /**
   * @return "checksum bytes received ok", not whether the sum is correct
   */
  public boolean test(int onechar){
    return testees.used()<length() && testees.append(onechar);
  }

  /**
   * @return whether test bytes have all been received
   */
  public boolean complete(){
    return testees.allocated()==testees.used();
  }

  public boolean matches(){
    return java.util.Arrays.equals(Sum(),testees.packet());//confirmed to work correctly for two zero length arrays
  }

  /**
   * @return a new Checksum of the same extended type, but not with the same values.
   */
  public Checksum Clone(){
    return new Checksum();
  }

}
//$Id: Checksum.java,v 1.7 2003/06/19 15:22:00 andyh Exp $