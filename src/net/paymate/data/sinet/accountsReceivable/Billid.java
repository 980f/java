package net.paymate.data.sinet.accountsReceivable;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/accountsReceivable/Billid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Billid extends UniqueId {

  public Billid() {
  }

  public Billid(int value) {
    super(value);
  }

  public Billid(String value) {
    super(value);
  }
}
