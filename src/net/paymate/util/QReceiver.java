package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/QReceiver.java,v $
 * Description:  super simple listener
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface QReceiver {
  /**
   * @return false if object is not accepted. true if successfully received
   */
  public boolean Post(Object arf);
}
//$Id: QReceiver.java,v 1.2 2002/03/14 23:31:41 andyh Exp $