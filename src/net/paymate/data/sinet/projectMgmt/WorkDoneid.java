package net.paymate.data.sinet.projectMgmt;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/projectMgmt/WorkDoneid.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.data.UniqueId;

public class WorkDoneid extends UniqueId {

  public WorkDoneid() {
  }

  public WorkDoneid(int value) {
    super(value);
  }

  public WorkDoneid(String value) {
    super(value);
  }
}
