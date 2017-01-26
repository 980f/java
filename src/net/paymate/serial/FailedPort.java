package net.paymate.serial;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/serial/FailedPort.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class FailedPort extends Port {

  protected FailedPort(String name, InputStream is, OutputStream os) {
    super(name, new deadInputStream(), new deadOutputStream());
  }

  protected FailedPort(String name) {
    super(name);
  }
}

//$Id: FailedPort.java,v 1.2 2003/07/24 16:47:34 andyh Exp $