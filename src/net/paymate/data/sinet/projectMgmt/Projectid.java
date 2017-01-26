package net.paymate.data.sinet.projectMgmt;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/projectMgmt/Projectid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class Projectid extends UniqueId {

  public Projectid() {
  }

  public Projectid(int value) {
    super(value);
  }

  public Projectid(String value) {
    super(value);
  }
}
