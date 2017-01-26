package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplianceKeyid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class ApplianceKeyid extends UniqueId {

  public ApplianceKeyid() {
  }

  public ApplianceKeyid(int value) {
    super(value);
  }

  public ApplianceKeyid(String value) {
    super(value);
  }
}
