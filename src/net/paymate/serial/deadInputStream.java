package net.paymate.serial;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/serial/deadInputStream.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import java.io.InputStream;
import net.paymate.lang.ThreadX;

class deadInputStream extends InputStream {
  public int read() {
    ThreadX.sleepFor(100000);//a medium long time
    return Receiver.TimedOut;
  }
}

