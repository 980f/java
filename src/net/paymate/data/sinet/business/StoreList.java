package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/StoreList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.data.UniqueId;
import java.util.Enumeration;
import net.paymate.data.sinet.EntityList;
import net.paymate.data.sinet.SinetClass;

public class StoreList extends EntityList {

  public StoreList() {

  }

  public Store [ ] getAll() {
    Store [ ] ret = new Store[size()];
    Enumeration enum = entities();
    for(int i = ret.length; i-->0;) {
      try {
        ret[i] = (Store) enum.nextElement();
      } catch (Exception ex) {
        // +++
      }
    }
    return ret;
  }

  public Store getById(Storeid id) {
    return (Store)getEntityById(id);
  }

  public StoreList addStore(Store a) {
    addEntity(a);
    return this;
  }

  public StoreList removeStore(Store a) {
    removeEntity(a);
    return this;
  }

}
