package net.paymate.util;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/util/SoftObject.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import java.lang.ref.SoftReference;
import java.lang.ref.ReferenceQueue;

public final class SoftObject extends SoftReference {
  private SoftObject(Object k) {
    super(k);
  }
  private static final SoftObject create(Object k) {
    return (k == null) ? null : new SoftObject(k);
  }
  private SoftObject(Object k, ReferenceQueue q) {
    super(k, q);
  }
  public static final SoftObject create(Object k, ReferenceQueue q) {
    return (k == null) ? null : new SoftObject(k, q);
  }
  /* A WeakObject is equal to another WeakObject iff they both refer to objects
  that are, in turn, equal according to their own equals methods */
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SoftObject)) {
      return false;
    }
    Object t = this.get();
    Object u = ((SoftObject)o).get();
    if ((t == null) || (u == null)) {
      return false;
    }
    if (t == u) {
      return true;
    }
    return t.equals(u);
  }
}

