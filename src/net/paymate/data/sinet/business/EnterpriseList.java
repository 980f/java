package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/EnterpriseList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.Enumeration;
import net.paymate.data.sinet.EntityList;

public class EnterpriseList extends EntityList {

  public EnterpriseList() {

  }

  public Enterprise getById(Enterpriseid id) {
    return (Enterprise)getEntityById(id);
  }

  public EnterpriseList addEnterprise(Enterprise a) {
    addEntity(a);
    return this;
  }

  public EnterpriseList removeEnterprise(Enterprise a) {
    removeEntity(a);
    return this;
  }

}
