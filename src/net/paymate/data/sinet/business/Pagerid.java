package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/Pagerid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Pagerid extends UniqueId {

  public Pagerid() {
  }

  public Pagerid(int value) {
    super(value);
  }

  public Pagerid(String value) {
    super(value);
  }
}
