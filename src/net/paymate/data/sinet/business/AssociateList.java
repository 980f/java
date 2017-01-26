package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/AssociateList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import java.util.Enumeration;
import net.paymate.data.sinet.EntityList;

public class AssociateList extends EntityList {

  public AssociateList() {

  }

  public Associate [ ] getAll() {
    Associate [ ] ret = new Associate[size()];
    Enumeration ennum = entities();
    for(int i = ret.length; i-->0;) {
      try {
        ret[i] = (Associate) ennum.nextElement();
      } catch (Exception ex) {
        // +++
      }
    }
    return ret;
  }

  public Associate getById(Associateid id) {
    return (Associate)getEntityById(id);
  }

  public AssociateList addAssociate(Associate a) {
    addEntity(a);
    return this;
  }

  public AssociateList removeAssociate(Associate a) {
    removeEntity(a);
    return this;
  }
}
