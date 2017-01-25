/**
 * Title:        NullOutputStream<p>
 * Description:  The bit bucket<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: NullOutputStream.java,v 1.2 2001/04/13 00:15:20 mattm Exp $
 */
package net.paymate.util;
import  java.io.IOException;
import  java.io.OutputStream;

// +++ need a net.paymate.io!

public class NullOutputStream extends OutputStream {

  public NullOutputStream() {
    super();
  }

  // the only method required
  public void write(int b) throws IOException {
  }

/*
  public void close() throws IOException {
  }

  public void flush() throws IOException {
  }

  public void write(byte[] b) throws IOException {
  }

  public void write(byte[] b, int off, int len) throws IOException {
  }
*/

}