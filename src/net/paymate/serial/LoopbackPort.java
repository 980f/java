package net.paymate.serial;

/**
 * Title:        $Source: /cvs/src/net/paymate/serial/LoopbackPort.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: LoopbackPort.java,v 1.4 2004/01/19 17:03:26 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.io.ByteFifo;

public class LoopbackPort extends Port {

  ByteFifo baf;

  /**
   * @param size determines how much output will be saved for use as input.
   */
  public LoopbackPort(String name, int size) {
    super(name+"Loopback");
    baf=new ByteFifo(size, true /*blocking*/); // blocknig or not ??? +++
    super.is=baf.getInputStream();
    super.os=baf.getOutputStream();
  }

}
//$Source: /cvs/src/net/paymate/serial/LoopbackPort.java,v $