/**
 * Title:        NullOutputStream<p>
 * Description:  The bit bucket<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: NullOutputStream.java,v 1.1 2003/07/27 19:36:55 mattm Exp $
 */
package net.paymate.io;
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