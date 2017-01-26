package net.paymate.util;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/MapCache.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import net.paymate.lang.TrueEnum;

// this class uses Integer keys and TrueEnum indexes into an array of a Map.
// it provides for synchronization for creation of the different Maps.
// might not be necessary, but, oh well.
// HashTable and SoftHashtable are the big users of it in SS2.
// PLEASE do not break this class.  It is the core of SS2, along with SoftHashtable.

public class MapCache {
  private Map [ ] caches = null;
  private Class classForArray = null;
  public MapCache(int fixedSize, Class classForArray) {
    caches = new Map[fixedSize];
    this.classForArray = classForArray;
  }
  public MapCache(TrueEnum values, Class classForArray) {
    this(values.numValues(), classForArray);
  }

  private synchronized Map getCache(TrueEnum klass) {
    // first, get the correct cache
    Map cache = caches[klass.Value()];
    if(cache == null) {
      try {
        cache = (Map)classForArray.newInstance();
        caches[klass.Value()] = cache;
      } catch (Exception ex) {
        // ???
      }
    }
    return cache;
  }

  public synchronized void put(Integer key, TrueEnum klass, Object o) {
    getCache(klass).put(key, o);
  }

  public synchronized Object get(Integer key, TrueEnum klass) {
    return getCache(klass).get(key);
  }

  public synchronized Set keys(TrueEnum klass) {
    return getCache(klass).keySet();
  }

  public int size(TrueEnum klass) {
    return getCache(klass).size();
  }

  public int [] counts(TrueEnum klass) {
    int [ ] ret = new int[klass.numValues()];
    for(int i = klass.numValues(); i-->0;) {
      klass.setto(i);
      ret[i] = getCache(klass).size();
    }
    return ret;
  }

  ///////////////////////
  // STATICS

  public static final MapCache HashtableCache(TrueEnum values) {
    return new HashtableCache(values);
  }
  public static final MapCache SoftHashtableCache(TrueEnum values) {
    return new SoftHashtableCache(values);
  }
}

class HashtableCache extends MapCache {
  public HashtableCache(TrueEnum values) {
    super(values.numValues(), Hashtable.class);
  }
}

class SoftHashtableCache extends MapCache {
  public SoftHashtableCache(TrueEnum values) {
    super(values.numValues(), SoftHashtable.class);
  }
}
