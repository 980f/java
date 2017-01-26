package net.paymate.data.sinet.server;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/server/Serviceid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Serviceid extends UniqueId {

  public Serviceid() {
  }

  public Serviceid(int value) {
    super(value);
  }

  public Serviceid(String value) {
    super(value);
  }
}
