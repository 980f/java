package net.paymate.data.sinet;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/EntityList.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import java.util.Hashtable;
import java.util.Enumeration;
import net.paymate.util.TextList;
import net.paymate.data.UniqueId;

// this entity list is not the global entity list.  It is to be extended and used by independent objects
// for instance, AssociateList would extend this class,
// then a particular enterprise object will contain an AssociateList with just that enterprise's associates in it

public class EntityList {

  // +++ should we provide an ordered list instead?
  private Hashtable myList = new Hashtable();

  protected EntityBase getEntityById(UniqueId id) {
    return (EntityBase)(myList.get(id.hashKey()));
  }

  protected EntityList addEntity(EntityBase e) {
    // if put() returns an object, we had already entered an object by that id.
    myList.put(e.id().hashKey(), e);
    return this;
  }

  protected EntityList removeEntity(EntityBase e) {
    myList.remove(e.id().hashKey()); // if remove() returns nothing, the value wasn't in the list to being with!
    return this;
  }

  // this returns the VALUES, not the KEYS
  public Enumeration entities() {
    return myList.elements();
  }

  public int size() {
    return myList.size();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public TextList spam() {
    TextList tl = new TextList();
    for(Enumeration enum = myList.keys(); enum.hasMoreElements();) {
      Object key = enum.nextElement();
      if(key != null) {
        Object value = myList.get(key);
        tl.add("" + key + "=" + value);
      }
    }
    return tl;
  }

}