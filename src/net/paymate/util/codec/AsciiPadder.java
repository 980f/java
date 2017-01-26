package net.paymate.util.codec;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/codec/AsciiPadder.java,v $</p>
 * <p>Description: Pads a byte[] (or makes one) with n*4 bytes of random ascii data</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.Random;
import net.paymate.util.codec.*;

public class AsciiPadder {
  private static Random radner = new Random();

  // tell it how many bytes you want, in multiples of 4 (mult4=3 to get 12 bytes)
  public static final byte [ ] asciiPad4s(int mult4) {
    if(mult4 < 1) {
      mult4 = 0;
    }
    if(mult4 == 0) {
      return new byte[0];
    } else {
      // in order that we get RANDOMSIZE (4*n) random printable characters,
      byte[] interim = new byte[mult4*3]; // we need to encrypt 3*n random bytes
      radner.nextBytes(interim);          // get random bytes
      // convert the random bytes to acceptable filler
      return Base64Codec.toString(interim).getBytes(); // +++ create a Base64Codec.toBytes() !
    }
  }

  public static final void asciiPad4s(byte [ ] bytes, int mult4) { // should this NOT be synchronized ???
    if(mult4 < 1) {
      mult4 = 0;
    }
    byte [ ] interim = asciiPad4s(mult4);
    int minlen = Math.min(bytes.length, interim.length);
    System.arraycopy(interim, 0, bytes, 0, minlen);
  }

  // legacy for net.paymate.database.DataCrypt; deprecate!
  public static final void rand4(byte[] fillfirst4) {
    asciiPad4s(fillfirst4, 1); // just 4 bytes
  }
}

// $Id: AsciiPadder.java,v 1.2 2004/03/08 22:54:18 mattm Exp $
