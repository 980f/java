package net.paymate.data.sinet.softwareDevel;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/softwareDevel/Bugid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Bugid extends UniqueId {

  public Bugid() {
  }

  public Bugid(int value) {
    super(value);
  }

  public Bugid(String value) {
    super(value);
  }
}
