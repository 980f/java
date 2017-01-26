package net.paymate.data.sinet.softwareDevel;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/softwareDevel/Revisionid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Revisionid extends UniqueId {

  public Revisionid() {
  }

  public Revisionid(int value) {
    super(value);
  }

  public Revisionid(String value) {
    super(value);
  }
}
