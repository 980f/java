/**
 * Title:        ObjectPool
 * Description:  Allows the insertion of an object, returning a
 *               key, and the subsequent removal of it when presenting the key.
 *               Is it possible that Hashtable does this all by itself and no additional code is needed?
 *               +_+ then call this a KeyedObjectPool. A SimpleObjectPool can be made out of a vector.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ObjectPool.java,v 1.5 2001/10/05 20:39:32 andyh Exp $
 */

package net.paymate.util;
import  java.util.Hashtable;

public class ObjectPool extends Hashtable {

  private Hashtable table = new Hashtable();

  private int incr = -1;
  public String setObject(Object rs) {
    String retval = null;
    if(rs != null) {
      Monitor mon = new Monitor("ObjectPool");
      try {
        mon.getMonitor();
        // if(containsValue(stmt) // presume it isn't in here for now; code the check later +++
        incr++;
        retval = "" + incr;
        table.put(retval, rs);
      } catch (Exception e) {
      } finally {
        if(mon != null) {
          mon.freeMonitor();
        }
      }
    }
    return retval;
  }
  public Object getObject(String key) {
    Object rs = table.get(key);
    table.remove(key);
    return rs;
  }

  private String name = "AnonymousObjectPool";

  public String name() {
    return name;
  }

  public ObjectPool(String name) {
    this.name = name;
  }

  public Hashtable cloneList() {
    return (Hashtable)table.clone();
  }
}
