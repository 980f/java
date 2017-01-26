package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/PostalAddressid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class PostalAddressid extends UniqueId {

  public PostalAddressid() {
  }

  public PostalAddressid(int value) {
    super(value);
  }

  public PostalAddressid(String value) {
    super(value);
  }
}