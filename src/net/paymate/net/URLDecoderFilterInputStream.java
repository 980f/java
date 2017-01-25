/**
 * Title:        URLDecoderFilterInputStream
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: URLDecoderFilterInputStream.java,v 1.5 2001/10/05 18:47:44 mattm Exp $
 */

package net.paymate.net;
import  java.io.*;
import  net.paymate.util.*;

public class URLDecoderFilterInputStream extends FilterInputStream {

  protected StringBuffer tmpBuff = new StringBuffer(3); // really shouldn't use "String" anything

  /**
   * Creates a new URLEncoded intput stream to write data to the
   * specified underlying output stream.
   *
   * @param   out   the underlying output stream.
   */
  public URLDecoderFilterInputStream(InputStream in) {
    super(in);
  }

  public int available() throws IOException{
    return super.available() + tmpBuff.length();
  }

  public void reset() throws IOException /* literally */{
    throw(new IOException("Can't reset a " + URLDecoderFilterInputStream.class.getName()));
  }

  public void mark(int readlimit) {
    // see markSupported
  }

  public boolean markSupported() {
    // too difficult for me to mess with right now
    return false;
  }

  public long skip(long n) throws IOException {
    long count = 0;  // probably cleaner way -- i'm in a hurry; works
    for(long i = 0; i < n; i++) {
      if(read() == -1) {
        break;
      } else {
        count++;
      }
    }
    return count;
  }

  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  public int read(byte[] b, int off, int len) throws IOException {
    int end = off+ len;
    int count = 0;
    for(int i = off; i < end; i++) {
      int c = read();
      if(c == -1) {
        break;
      }
      b[i] = (byte)((char)c);
      count++;
    }
    return ((count == 0) && (len > 0)) ? -1 : count;
  }

  private Monitor thisMonitor = new Monitor("URLDecoderFilterInputStream");

  /**
   * Reads the specified byte from this URLEncoded input stream.
   *
   * @exception  IOException  if an I/O error occurs.
   */
  public int read() throws IOException {
    int retval = 0;
    try {
      thisMonitor.getMonitor();
      int c = -1;
      // be sure we have at least one character in the buffer
      if(tmpBuff.length() == 0) {
        getOne();
      }
      // after attempting to read one, if it is still empty, return -1
      if(tmpBuff.length() > 0) {
        // otherwise, something's in the buffer; see if it needs conversion
        if(tmpBuff.charAt(0) == '%') {
          while((tmpBuff.length() < 3) && getOne()) {
          }
          // we got what we could; so attempt the conversion
          if(tmpBuff.length() >= 3) {
            int chr = -1;
            try {
              String toParse = tmpBuff.substring(1,3);
              chr = Integer.parseInt(toParse,16);
            } catch (NumberFormatException e) {
              // if it is misformatted, just spew as normal
            }
            if(chr != -1) { // conversion took
              tmpBuff.delete(0, 3);   // shouldn't except (deletes the 3 chars starting at position 1)
              tmpBuff.insert(0, (char)chr); // prepend the converted character
            }
          }
        } else {
          if(c == '+') {
            c = ' ';
          }
        }
        // return the first available char
        c = tmpBuff.charAt(0);
        tmpBuff.deleteCharAt(0);
      }
// this was converting +'s to ' 's that ought not be (that were +'s before encoding), so I moved it into the function above.
//      if(c == '+') {
//        c = ' ';
//      }
      retval = (byte)c; // one exit point
    } finally {
      thisMonitor.freeMonitor();
      return retval;
    }
  }

  private boolean getOne() throws IOException {
    int chr = super.read();
    if(chr != -1) {
      tmpBuff.append((char)chr);
      return true;
    }
    return false;
  }

/*
  public static final void main(String [] args) {
    String test = "This is a test!\n\r\nPlease tell me how it went.  :)";
    System.out.println("Original: " + test);
    System.out.println("Encoding ...");
    String bytes = null;
    {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      URLEncoderFilterOutputStream uefos = new URLEncoderFilterOutputStream(baos);
      {
        PrintWriter pw = new PrintWriter(uefos, true);
        pw.print(test);
        pw.flush();
        pw.close();
      }
      bytes = baos.toString();
    }
    System.out.println("... done.");
    System.out.println("Original: " + test);
    System.out.println("Encoded: " + bytes);
    System.out.println("Decoding ...");
    String orig2 = null;
    {
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes.getBytes());
      StringBuffer sb = new StringBuffer();
      URLDecoderFilterInputStream udfis = new URLDecoderFilterInputStream(bais);
      {
        int i = -1;
        try {
          while((i = udfis.read()) != -1) {
            sb.append((char)i);
          }
          udfis.close();
        } catch (Exception e) {
          System.out.println("Excepted! reading udfis");
        }
      }
      orig2 = sb.toString();
    }
    System.out.println("... done.");
    System.out.println("Original: " + test);
    System.out.println("Encoded: " + bytes);
    System.out.println("Decoded: " + orig2);
    System.out.println("original and decoded are " + (test.equals(orig2) ? "" : "NOT ") + "equal");
  }
*/

}
