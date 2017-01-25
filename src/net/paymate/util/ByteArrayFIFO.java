/**
 * Title:        ByteArrayFIFO
 * Description:  Implements a FIFO for bytes, including input and output streams
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ByteArrayFIFO.java,v 1.5 2001/06/26 20:14:53 andyh Exp $
 *
 * This class implements an output stream in which the data is
 * written into and read from a byte array. The buffer automatically grows as data
 * is written to it.
 *
 * This class implements an input stream and contains
 * an internal buffer that contains bytes that
 * may be read from the stream. An internal
 * counter keeps track of the next byte to
 * be supplied by the <code>read</code> method.
 */

package net.paymate.util;
//import  java.io.UnsupportedEncodingException;  // for toString(String enc)
import  java.io.IOException;
import  java.io.OutputStream;
import  java.io.InputStream;

// +++ should this block on a read?

public class ByteArrayFIFO {
// !!! Can't use this here since the ByteArrayFIFO is used in one!  Causes infinite loop!
//  private static final ErrorLogStream dbg = new ErrorLogStream(ByteArrayFIFO.class.getName());

  /**
   * The buffer where data is stored.
   */
  protected byte buf[];
  /**
   * The number of valid bytes in the buffer.
   */
  protected int count;

  // the stream stuff
  protected ByteArrayFIFOOutputStream bafos = null;
  protected ByteArrayFIFOInputStream  bafis = null;

  /**
   * Creates a new byte array output stream. The buffer capacity is
   * initially 32 bytes, though its size increases if necessary.
   */
  public ByteArrayFIFO() {
    this(32);
  }

  /**
   * Creates a new byte array output stream, giving the buffer the initial
   * capacity specified by the <code>size</code> parameter,
   * though its size increases if necessary.
   */
  public ByteArrayFIFO(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Negative initial size: " + size);
    }
    buf = new byte[size];
    count = 0;
    bafos = new ByteArrayFIFOOutputStream(this);
    bafis = new ByteArrayFIFOInputStream(this);
  }

  /**
   * Resets the <code>count</code> field of this object
   * to zero, so that all currently accumulated output in the
   * buffer is discarded. The object can be used again,
   * reusing the already allocated buffer space.
   */
  public synchronized void clear() {
    count = 0;
  }

  /**
   * Returns the current size of the buffer.
   *
   * @return  the value of the <code>count</code> field, which is the number
   *          of valid bytes in this output stream.
   */
  public int size() {
    return count;
  }

  /**
   * Creates a newly allocated byte array. Its size is the current
   * size of this object, and the valid contents of the buffer
   * have been copied into it.
   *
   * @return  the current contents of this object, as a byte array.
   */
  public synchronized byte toByteArray()[] {
    byte newbuf[] = new byte[count];
    System.arraycopy(buf, 0, newbuf, 0, count);
    return newbuf;
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into
   * characters according to the platform's default character encoding.
   *
   * @return String translated from the buffer's contents.
   */
  public String toString() {
    return new String(buf, 0, count);
  }

  /**
   * Converts the buffer's contents into a string, translating bytes into
   * characters according to the specified character encoding.
   *
   * @param   enc  a character-encoding name.
   * @return String translated from the buffer's contents.
   * @throws UnsupportedEncodingException
   *         If the named encoding is not supported.
   */
/*
  public String toString(String enc) throws UnsupportedEncodingException {
    return new String(buf, 0, count, enc);
  }
*/

///////////////////////////////////////
// stream-based stuff
///////////////////////////////////////

  public long lastWrite = -1; // 4deferred flushing, use stopwatch // there are other considerations to flushing, and there already exists a thread elsewhere that is handling this (no need for 2 threads to do it)

  public synchronized void write(int b) {
    int newcount = count + 1;
    if (newcount > buf.length) {
      byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
      System.arraycopy(buf, 0, newbuf, 0, count);
      buf = newbuf;
    }
    buf[count] = (byte)b;
    count = newcount;
    //notify read() that bytes have come in!
  }

  /**
   * +++ optimize!
   * blocking read, now
   */
  public synchronized int read() {
    int ret = -1;
    if(count > 0) {
      ret = buf[0] & 0xff;
      System.arraycopy(buf, 1, buf, 0, --count); // --- cross your fingers and pray this works!
    } else {
    //+++block until bytes have arrived.
    //+++ can't do while 'synchronized' at method level.
    //need to make set&get for the count member and synchronize around use of those.
    }
    return ret;
  }

  public synchronized int available() {
    return count;
  }

  public OutputStream getOutputStream() {
    return bafos;
  }

  public InputStream getInputStream() {
    return bafis;
  }

//////////////////////////////////
// testing stuff
//////////////////////////////////

  public static String Usage() {
    return "Usage: net.paymate.util.Tester util.ByteArrayFIFO \"test string goes in here\"";
  }

  public static void Test(String[] args) {
    try {
      if(args.length < 1) {
        System.out.println(Usage());
      } else {
        String str = args[0];
        ByteArrayFIFO fifo = new ByteArrayFIFO();
        // all at once
        System.out.println("Stuffing the whole string in: '" + str + "'");
        OutputStream os = fifo.getOutputStream();
        InputStream is = fifo.getInputStream();
        os.write(str.getBytes());
        System.out.println("Getting the string back out all at once yields:");
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        Streamer.swapStreams(is, baos);
        System.out.println(baos.toString());
        // interleaved writes
        System.out.println("Stuffing the string in, interleaved with the character position: '" + str + "'");
        byte [] bytes = str.getBytes();
        for(int i = 0; i < bytes.length; i++) {
          os.write(bytes[i]);
          os.write((""+i).getBytes());
        }
        System.out.println("Getting the string back out all at once yields:");
        baos.reset();
        Streamer.swapStreams(is, baos);
        System.out.println(baos.toString());
        // interleaved writes & reads
        System.out.println("Stuffing the string in, interleaved with reading it out: '" + str + "'");
        bytes = str.getBytes();
        String test = "";
        for(int i = 0; i < bytes.length; i++) {
          os.write(bytes[i]);
          test += (char)(byte)is.read();
        }
        System.out.println("Getting the string back out interleaved yields: '" + test + "'");
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }
}

class ByteArrayFIFOOutputStream extends OutputStream {
  ByteArrayFIFO fifo = null;

  public ByteArrayFIFOOutputStream(ByteArrayFIFO fifo) {
    this.fifo = fifo;
  }

  public synchronized void write(int b) throws IOException {
    if(fifo == null) {
      throw new IOException("Stream closed");
    } else {
      fifo.write(b);
    }
  }

  public synchronized void close() throws IOException {
    if(fifo == null) {
      throw new IOException("Stream already closed");
    } else {
      fifo = null;
    }
  }

}

class ByteArrayFIFOInputStream extends InputStream {
  ByteArrayFIFO fifo = null;

  public ByteArrayFIFOInputStream(ByteArrayFIFO fifo) {
    this.fifo = fifo;
  }

  public synchronized int read() throws IOException {
    if(fifo == null) {
      throw new IOException("Stream closed");
    } else {
      return fifo.read();
    }
  }

  public synchronized int available() throws IOException {
    if(fifo == null) {
      throw new IOException("Stream closed");
    } else {
      return fifo.available();
    }
  }

  public synchronized void close() throws IOException {
    if(fifo == null) {
      throw new IOException("Stream already closed");
    } else {
      fifo = null;
    }
  }
}
