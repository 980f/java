package net.paymate.util;

import java.util.Hashtable;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Bag.java,v $
 * Description:  hashtable where key is object.toString()
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class Bag extends Hashtable {
  /**
   * replace or insert object, return previous one that had the same key
   */
  public Object put(Object obj){
    return super.put(obj.toString(),obj);
  }

  public Object get(Object key){
    return super.get(key.toString());
  }

  public Bag(int initialCapacity) {
    super(initialCapacity);
  }

  public Bag() {
    super();
  }

}
//$Id: Bag.java,v 1.1 2002/08/20 01:11:40 andyh Exp $