package net.paymate.data.sinet.business;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/sinet/business/Associateid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.UniqueId;

public class Associateid extends UniqueId {

  public Associateid() {
  }

  public Associateid(int value) {
    super(value);
  }

  public Associateid(String value) {
    super(value);
  }
}