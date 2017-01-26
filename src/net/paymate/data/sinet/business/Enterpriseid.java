package net.paymate.data.sinet.business;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/sinet/business/Enterpriseid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.UniqueId;

public class Enterpriseid extends UniqueId {

  public Enterpriseid() {
  }

  public Enterpriseid(int value) {
    super(value);
  }

  public Enterpriseid(String value) {
    super(value);
  }
}