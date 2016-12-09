package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/LoopbackPort.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: LoopbackPort.java,v 1.1 2001/06/16 03:19:27 andyh Exp $
 */

import net.paymate.util.*;

public class LoopbackPort extends Port {

  ByteArrayFIFO baf;

  /**
   * @param size determines how much output will be saved for use as input.
   */
  public LoopbackPort(String name, int size) {
    super(name+"Loopback");
    baf=new ByteArrayFIFO(size);
    super.is=baf.getInputStream();
    super.os=baf.getOutputStream();
  }

}
//$Source: /cvs/src/net/paymate/serial/LoopbackPort.java,v $