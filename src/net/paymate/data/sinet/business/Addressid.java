package net.paymate.data.sinet.business;

import net.paymate.data.UniqueId;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/Addressid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class Addressid extends UniqueId {

  public Addressid() {
  }
  public Addressid(int value) {
    super(value);
  }
  public Addressid(String value) {
    super(value);
  }
}