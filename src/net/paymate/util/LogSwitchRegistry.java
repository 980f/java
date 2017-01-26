package net.paymate.util;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/LogSwitchRegistry.java,v $</p>
 * <p>Description: LogSwitch registry </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.Vector;

public class LogSwitchRegistry {

  private LogSwitchRegistry() {
    // can't make one!
  }

  public static final Vector registry = new Vector(100,100);
  public static final Vector printForkRegistry = new Vector(100,100);

}