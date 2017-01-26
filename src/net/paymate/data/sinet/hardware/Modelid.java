package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/Modelid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Modelid extends UniqueId {

  public Modelid() {
  }

  public Modelid(int value) {
    super(value);
  }

  public Modelid(String value) {
    super(value);
  }
}
