package net.paymate.data.sinet.business;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/sinet/business/Storeid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.UniqueId;

public class Storeid extends UniqueId {

  public Storeid() {
  }

  public Storeid(int value) {
    super(value);
  }

  public Storeid(String value) {
    super(value);
  }
}