package net.paymate.data.sinet.support;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/support/Occurenceid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Occurenceid extends UniqueId {

  public Occurenceid() {
  }

  public Occurenceid(int value) {
    super(value);
  }

  public Occurenceid(String value) {
    super(value);
  }
}
