package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/TermDevicePropertyid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class TermDevicePropertyid extends UniqueId {

  public TermDevicePropertyid() {
  }

  public TermDevicePropertyid(int value) {
    super(value);
  }

  public TermDevicePropertyid(String value) {
    super(value);
  }
}
