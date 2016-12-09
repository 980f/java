/*
 * $Id: URLEncoderFilterOutputStream.java,v 1.7 2001/05/22 19:13:35 mattm Exp $
 *
 * Copyright 2000 PayMate.Net, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of PayMate.Net, Inc.
 * Use is subject to license terms.
 *
 */

package net.paymate.net;
import  java.io.*;
import  java.util.BitSet;
import  net.paymate.util.*;

/**
 * The class implements a URL encoder FilterOutputStream. By setting up such
 * an output stream, an application can write bytes to the underlying
 * output stream in URL-encoded format, converting characters outside the
 * sets {0-9}, {A-Z}, {a-z} into their hexadecimal %'ed formats
 * (eg: %20 for space).
 *
 * NOTE: can probably replace all super.write(whatever) with out.append(whatever)
 *
 */



public class URLEncoderFilterOutputStream extends FilterOutputStream {

// stolen from URLEncoder.java
    static BitSet dontNeedEncoding;
    static final int caseDiff = ('a' - 'A');
    static {
      dontNeedEncoding = new BitSet(256);
      int i;
      for (i = 'a'; i <= 'z'; i++) {
        dontNeedEncoding.set(i);
      }
      for (i = 'A'; i <= 'Z'; i++) {
        dontNeedEncoding.set(i);
      }
      for (i = '0'; i <= '9'; i++) {
        dontNeedEncoding.set(i);
      }
      dontNeedEncoding.set(' '); /* encoding a space to a + is done in the encode() method */
      dontNeedEncoding.set('-');
      dontNeedEncoding.set('_');
      dontNeedEncoding.set('.');
      dontNeedEncoding.set('*');
    }

    /**
     * Creates a new URLEncoded output stream to write data to the
     * specified underlying output stream.
     *
     * @param   out   the underlying output stream.
     */
    public URLEncoderFilterOutputStream(OutputStream out) {
      super(out);
    }

    private Monitor thisMonitor = new Monitor("URLEncoderFilterOutputStream");

    /**
     * Writes the specified byte to this URLEncoded output stream.
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException {
      try {
        thisMonitor.getMonitor();
        // check to see if it needs converting.  If so, conert it
        // otherwise, pass it through
        if (dontNeedEncoding.get(b)) {
          if (b == ' ') {
            b = '+';
          }
          super.write(b);
        } else {
          super.write('%');
          for(int i = 0; i < 2; i++) {
            int b2 = (i == 0) ? (b >> 4) : b;
            char ch = Character.forDigit(b2 & 0xF, 16);
            // converting to use uppercase letter as part of
            // the hex value if ch is a letter.
            if (Character.isLetter(ch)) {
              ch -= caseDiff;
            }
            super.write((byte)ch);
          }
        }
      } finally {
        thisMonitor.freeMonitor();
      }
    }
}

//$Id: URLEncoderFilterOutputStream.java,v 1.7 2001/05/22 19:13:35 mattm Exp $
