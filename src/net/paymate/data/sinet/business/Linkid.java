package net.paymate.data.sinet.business;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/sinet/business/Linkid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Linkid extends UniqueId {

  public Linkid() {
  }

  public Linkid(int value) {
    super(value);
  }

  public Linkid(String value) {
    super(value);
  }
}