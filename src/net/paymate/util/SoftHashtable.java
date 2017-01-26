package net.paymate.util;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/SoftHashtable.java,v $</p>
 * <p>Description: implements a hashtable with strong keys and soft objects</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import java.util.*;
import java.lang.ref.ReferenceQueue;

// Can be used as a MEMORY-SENSITIVE CACHE!

// this class is primarily to be used for SS2 Entity and EntityList stuff.
// so, if you change this class, be SURE it works okay with SS2 stuff!

public class SoftHashtable extends Dictionary implements Map {

  private Hashtable content = null;

  /* Reference queue for cleared WeakObject */
  private ReferenceQueue queue = new ReferenceQueue();

  /* Remove all invalidated entries from the hashtable, that is, remove all entries
     whose keys have been discarded.  This method should be invoked once by
     each public mutator in this class.  We don't invoke this method in
     public accessors because that can lead to surprising
     ConcurrentModificationExceptions. */
  private synchronized void processQueue() {
    SoftObject wk;
    while ((wk = (SoftObject)queue.poll()) != null) {
      content.remove(wk);
    }
  }

  public SoftHashtable() {
    content = new Hashtable();
  }
  public SoftHashtable(int initialCapacity) {
    content = new Hashtable(initialCapacity);
  }
  public SoftHashtable(int initialCapacity, float loadFactor) {
    content = new Hashtable(initialCapacity, loadFactor);
  }

  public int size() {
    return content.size();
  }

  public boolean isEmpty() {
    return content.isEmpty();
  }

  public synchronized Object put(Object key, Object value) {
    processQueue();
    try {
      // check the return value of the next call?
      content.put(key, SoftObject.create(value, queue));
    } catch (Exception ex) {
      // ???
    }
    return null;
  }

  // get the object out that is
  public synchronized Object get(Object key) {
    Object ret = null;
    try {
      processQueue();
      Object value = content.get(key);
      if(value != null) {
        // +++ if not value instanceof WeakObject, do somethign drastic, as everything is broken!  (can't happen)
        SoftObject wo = (SoftObject)value;
        ret = wo.get();
      }
    } catch (Exception ex) {
      // ???
    } finally {
      return ret;
    }
  }

  //////////////////////////
  // Hashtable satisfactions (Map and Dictionary)
  //////////////////////////
  // should we send "unimplemented exceptions" instead of doing bogus things?
  public boolean containsKey(Object k) {
    return false; // don't do this!
  }
  public boolean containsValue(Object v) {
    return false; // don't do this!
  }
  public void putAll(Map m) {
    // don't do this!
  }
  public void clear() {
    // don't do this!
  }
  public Set keySet() {
    return null; // don't do this!  Shuld never be needed!
  }
  public Collection values() {
    return null; // don't do this!
  }
  public Set entrySet() {
    return null; // don't do this!
  }
  public Object remove(Object o) {
    return null; // don't do this!
  }
  public Enumeration elements() {
    return null; // don't do this!
  }
  public Enumeration keys() {
    return null; // don't do this!
  }
  //////////////////////////
  // END Hashtable satisfactions
  //////////////////////////
}