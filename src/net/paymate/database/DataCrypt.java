package net.paymate.database;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/database/DataCrypt.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.data.UniqueId;
import net.paymate.lang.StringX;
import net.paymate.util.*;
import net.paymate.util.codec.*;

public class DataCrypt {
  public DataCrypt() {
  }

  private static final int RANDOMSIZE = 4;

  // Changing ANY of the below will make it so that you can't read cardnumbers from the database!
   /*
   Salt = picked from a list of 32 different random numbers (hardcoded) by txnid % 32
   Key = nothing
   The "data" will be chaff+expiry + cardnumber.  EG: XXXX12124123456789012349.
   The chaff will be 4 random characters.
   */
  private static final int Salt(int modex) {
    if(modex < 1) { // this is a hack.  Something is wrong!
      modex = 0;
    }
    int index = modex % SHAKER.length;//??shaker is a power of two, could & rather than div here.
    return Math.abs(SHAKER[index]);//this loses a bit, why bother??
  }
  // BYTES GENERATED USING: http://random.hd.org/getBits.jsp?numBytes=256&type=hex
  private static final int [ ] SHAKER = {
      0x5e2cfbb6, // 1
      0xfd6de34c,
      0x5cf83f56,
      0x6aef8939,
      0x54a73a3d,
      0x4662a703,
      0xd236912e,
      0x82fb2b46,
      0x5b999e1f,
      0xac9c485e,
      0xea4a2ade,
      0xdf7d6950,
      0x24f365dd,
      0xe9f87377,
      0xf85b5f05,
      0x88dfaac7, // 16
      0xb48a4281,
      0x7db3ed9b,
      0x63e6b63a,
      0x58d3daf0,
      0x6ce2bc46,
      0x1f2faf54,
      0x1f71101b,
      0x6979ddd1,
      0x96bdaf4f,
      0x7348e946,
      0x22f66be9,
      0x4164f4e7,
      0xf3d70e4b,
      0x972a7a98,
      0x852ce902,
      0xb9fa6e80, // 32
      0x689763f7,
      0xe9f0cb35,
      0xd5779644,
      0xe5a2c5ff,
      0xfc46b3e5,
      0x5cf0595f,
      0xbe66bbbb,
      0x7a3facaa,
      0xc99f30f5,
      0x415066e9,
      0xc9772acf,
      0xc21918f3,
      0xf5d5399a,
      0x575f6720,
      0xb92f4e69,
      0x8699cb9a, // 48
      0xe4051dca,
      0x1e462124,
      0x2985871c,
      0x0a42113e,
      0x60071547,
      0x96bcb08f,
      0x32dea531,
      0x9f2e8f81,
      0x0789a732,
      0x560c7923,
      0x6ef09203,
      0xa7c9c39d,
      0x9f0a84b2,
      0x45473d42,
      0x0f878d9b,
      0xe8ea195b, // 64
  };

  // 4 random bytes in the front that we throw away (ONLY if string is nontrivial!)
  // 32 randomized ints in code, selected for seeding encryption lagorithm via id mod 32..
  public static final String databaseEncode(byte [ ] value, UniqueId id) {
    if((value != null) && (value.length > 0)) {
      byte [ ] toencrypt = new byte[RANDOMSIZE+value.length];
      System.arraycopy(value, 0, toencrypt, RANDOMSIZE, value.length);
      AsciiPadder.rand4(toencrypt);
      String preurl = new String(CipherP86B2.crypt(Salt(id.value()), null, toencrypt));
      return EasyUrlString.encode(preurl);
    } else {
      return ""; // don't do anything fancy if there is nothing to encrypt!
    }
  }
  public static final byte [ ] databaseDecode(String value, UniqueId id) {
    if(StringX.NonTrivial(value)) {
      String posturl = EasyUrlString.decode(value);
      byte [ ] withchaff = CipherP86B2.crypt(Salt(id.value()), null, posturl.getBytes());
      // first 4 are for chaff, rest is data
      if((withchaff != null) && (withchaff.length > RANDOMSIZE)) {
        int newlen = withchaff.length-RANDOMSIZE;
        byte[] withoutchaff = new byte[newlen];
        System.arraycopy(withchaff, RANDOMSIZE, withoutchaff, 0, newlen);
        return withoutchaff;
      } else {
        // return empty byte array
      }
    } else {
      // return empty byte array
    }
    return new byte[0]; // don't do anything fancy if there is nothing to decrypt!
  }


}