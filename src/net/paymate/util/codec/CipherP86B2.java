package net.paymate.util.codec;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/codec/CipherP86B2.java,v $
 * Description:  Performs PayMate proprietary mid-security encryption.
 *               Algorithm derived from "DLH-POLY-86-B CIPHER" (C) 1986 by Dana L. Hoggatt.
 *
 *               MEANT FOR TEXT ONLY, NOT BINARY DATA !!!
 *
 *               Note: restricting the use of an instance of this class to a single stream
 *               guarantees that the salt and key only evolve with THAT data stream.
 *
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

// +++ turn this into a real cipher (javax.crypto.Cipher)!


import  java.io.*;
import net.paymate.util.ErrorLogStream;

public final class CipherP86B2 {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CipherP86B2.class);

  // use these!

  public static final OutputStream getOutputStream(OutputStream toFilter, byte [ ] key, int primerNot95X) {
    return new CipherP86B2(key, primerNot95X).getOutputStream(toFilter);
  }

  public static final InputStream getInputStream(InputStream toFilter, byte [ ] key, int primerNot95X) {
    return new CipherP86B2(key, primerNot95X).getInputStream(toFilter);
  }


  // internals

  private final OutputStream getOutputStream(OutputStream toFilter) {
    return new FilterOutputStream(toFilter) {
      public void write(int b) throws IOException {
        super.write(crypt(b));
      }
    };
  }

  private final InputStream getInputStream(InputStream toFilter) {
    return new FilterInputStream(toFilter) {
      public int read() throws IOException {
        return crypt(super.read());
      }
    };
  }

  private CipherP86B2(byte [ ] password, int primerNot95X) {
    // valid values are 0 - MAXINT
    // if < 0, default is used
    if(primerNot95X > -1) {
      key = primerNot95X;
      salt = primerNot95X;
    } else {
      key = 0;
      salt = 0;
    }
    crypt(password); // encrypt the password; this gets the whole stream into a specific "state"
  }

  private CipherP86B2() {
    // nobody can do this
  }

  // running state of the stream
  private int key;
  private int salt;

  private final void crypt(byte bytes []) {
    if(bytes != null) {
      for(int i = 0; i < bytes.length; i++) {
          /* for every character in the buffer */
        bytes[i] = (byte) (255 & crypt(bytes[i]));
      }
    }
  }

  /**
   * should only pass this "byte"s!
   */
  private final int crypt(int original) {
    int cc = original;
    /* only encipher printable characters */
    if((cc > 31) && (cc < 127)) {

// this next chunk might behave differently under java than it did under C:
      /**  If the upper bit (bit 29) is set, feed it back into the key.  This
      assures us that the starting key affects the entire message.  **/
      key &= 0x1FFFFFFF;   /* keep 29 bits */
      if ((key & 0x10000000) > 0) { //if 29th bit is a 1
        key ^= 0x0040A001;     /* feedback 4235265 */
      }

// this next chunk might behave differently under java than it did under C:
      /**  Down-bias the character, perform a Beaufort encipherment, and
      up-bias the character again.  We want key to be positive
      so that the left shift here will be more portable and the
      mod95() faster   **/
      //only problem with % vs modulus is negative remainder
      cc = (32 + key - original) % 95; //gratuitous 32 needed for historical reasons.
      cc += (cc < 0) ? 127 : 32; //restore to ascii range
      /**  the salt will spice up the key a little bit, helping to obscure
      any patterns in the clear text, particularly when all the
      characters (or long sequences of them) are the same.  We do
      not want the salt to go negative, or it will affect the key
      too radically.  It is always a good idea to chop off cyclics
      to prime values.  **/
      if (++salt > 20856) { //this number sure as hell ain't prime!
        salt = 0;             /* prime modulus (max = 20856) */
      }

      /**  our autokey (a special case of the running key) is being
      generated by a weighted checksum of clear text, cipher
      text, and salt.   **/
      key = key + key + cc + original + salt;
    } else if ((cc >= 0) && (cc < 32)) { /* handle the lower ones */
      cc = 31 - cc;
    } else { /* handle the upper ones */
      cc = 128 - (cc - 127) + 127;
    }
    return cc & 255;
  }

  /**
   * only ascii range data is encrypted,
   * control character and other nonprintables are obfuscated but
   * don't affect the encrypting of the text
   */
  private final int fastcrypt(int original) {
    int cc = original;
    /* only encipher printable characters */
    if( (cc > 31) && (cc < 127)) {

      key &= 0x1FFFFFFF;
          /* keep 29 bits, an arbitrary choice but not one we can change. */
      if( (key & 0x10000000) > 0) { //if 29th bit is a 1
        key ^= 0x0040A001; /* feedback 4235265 */
      }

      cc = (32 + key - original) % 95; //gratuitous 32 needed for historical reasons.
      cc += (cc < 0) ? 127 : 32; //restore to ascii range
      if(++salt > 20856) {
        salt = 0;
      }
      key = key + key + cc + original + salt;
    } else if( (cc >= 0) && (cc < 32)) { /* handle the lower ones */
      cc = 31 - cc;
    } else { /* handle the upper (negative) ones */
      cc = 382 - cc; //128 - (cc - 127) + 127;
    }
    return cc & 255;
  }

  public static final byte [ ] crypt(int primer, byte [ ] key, byte [ ] input) {
    CipherP86B2 mycrypt = new CipherP86B2(key, primer);
    mycrypt.crypt(input);
    // now, input becomes output
    return input;
  }

}
//$Id: CipherP86B2.java,v 1.9 2004/02/19 10:15:28 mattm Exp $

