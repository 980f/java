package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/AdminPermid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class AdminPermid extends UniqueId {

  public AdminPermid() {
  }

  public AdminPermid(int value) {
    super(value);
  }

  public AdminPermid(String value) {
    super(value);
  }
}
