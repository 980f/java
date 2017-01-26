package net.paymate.serial;

import java.io.OutputStream;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/serial/deadOutputStream.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

class deadOutputStream extends OutputStream {
  public void write(int b) {
    // do absolutely nothing.
  }
}
//$Id: deadOutputStream.java,v 1.2 2003/07/24 16:47:35 andyh Exp $

