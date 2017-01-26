/**
 * Title:        StringFIFO
 * Description:  FIFO of strings
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: StringFIFO.java,v 1.1 2003/07/27 19:36:55 mattm Exp $
 */

package net.paymate.io;
import  java.io.PrintStream;
import  java.io.BufferedReader;
import  java.io.Reader;
import  java.io.InputStreamReader;
import net.paymate.io.NullOutputStream;
import net.paymate.io.NullInputStream;
import net.paymate.util.ObjectFifo;
import net.paymate.util.DateX;

public class StringFIFO extends ObjectFifo{

  private StringFIFOPrintStream ps = null;
  private StringFIFOFBufferedReader br = null;
  public long lastWrite = -1; // 4deferred flushing, use stopwatch // there are other considerations to flushing, and there already exists a thread elsewhere that is handling this (no need for 2 threads to do it)

  // mostly casting, here

  public synchronized String nextString(){
    return (String)super.next();
  }

  public synchronized void put(String obj){
    lastWrite = DateX.utcNow();
    super.put(obj);
  }

  public synchronized void atFront(String obj){
    super.atFront(obj);
  }

  public synchronized void Clear(){
    lastWrite = DateX.utcNow();
    super.Clear();
  }

  public StringFIFO(){
    ps = new StringFIFOPrintStream(this);
    br = new StringFIFOFBufferedReader(this);
  }

  public PrintStream getPrintStream() {
    return ps;
  }
  public BufferedReader getBufferedReader() {
    return br;
  }

}

/**
 * +_+ Only uses the println(String) function!
 * +++ make this safer so that any function can be used !!!!
 * the frapping java library writers privatized the essential function needing overloading.
 * they do that SOOO often.
 */
class StringFIFOPrintStream extends PrintStream {
  StringFIFO fifo = null;
  public StringFIFOPrintStream(StringFIFO fifo) {
    super(new NullOutputStream());
    this.fifo = fifo;
  }

  public void print(String str) {
    fifo.put(str);
  }

  public void println(String str) {
    fifo.put(str);
  }

  public void println(Object o) {
    println(String.valueOf(o));
  }
}

/**
 * Only use the readLine() function!
 */
class StringFIFOFBufferedReader extends BufferedReader {
  StringFIFO fifo = null;
  public StringFIFOFBufferedReader(StringFIFO fifo) {
    super(new InputStreamReader(new NullInputStream()));
    this.fifo = fifo;
  }
  public String readLine() {
    return fifo.nextString();
  }
}
