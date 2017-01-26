package net.paymate.data.sinet.hardware;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/sinet/hardware/Applianceid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.UniqueId;

public class Applianceid extends UniqueId {

  public Applianceid() {
  }

  public Applianceid(int value) {
    super(value);
  }

  public Applianceid(String value) {
    super(value);
  }
}