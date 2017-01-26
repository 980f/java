package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/Phoneid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.UniqueId;

public class Phoneid extends UniqueId {

  public Phoneid() {
  }
  public Phoneid(int value) {
    super(value);
  }
  public Phoneid(String value) {
    super(value);
  }
}