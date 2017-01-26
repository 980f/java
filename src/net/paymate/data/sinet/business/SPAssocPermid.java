package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/SPAssocPermid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class SPAssocPermid extends UniqueId {

  public SPAssocPermid() {
  }

  public SPAssocPermid(int value) {
    super(value);
  }

  public SPAssocPermid(String value) {
    super(value);
  }
}
