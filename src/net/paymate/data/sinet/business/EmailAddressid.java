package net.paymate.data.sinet.business;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/sinet/business/EmailAddressid.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.UniqueId;

public class EmailAddressid extends UniqueId {

  public EmailAddressid() {
  }

  public EmailAddressid(int value) {
    super(value);
  }

  public EmailAddressid(String value) {
    super(value);
  }
}