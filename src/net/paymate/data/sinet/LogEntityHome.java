package net.paymate.data.sinet;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/LogEntityHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.MapCache;
import net.paymate.data.UniqueId;
import net.paymate.util.EasyProperties;
// for reporting
import java.util.Iterator;

// Extend this class for each kind of LOG entity.
// Eg: for Txn class, create an TxnHome extension of this class.

public class LogEntityHome extends EntityHome {
  public LogEntityHome(SinetClass myclass) {
    super(myclass);
    cache = staticcache;
  }

  // this is JUST for reporting!  Don't use it for anything else!
  public static final EasyProperties status() {
    EasyProperties ezp = new EasyProperties();
    try {
      SinetClass myclass = new SinetClass();
      int [] counts = staticcache.counts(myclass);
      for(int i = counts.length; i-->0;) {
        myclass.setto(i);
        ezp.setInt(myclass.Image(), counts[i]);
      }
    } catch (Exception ex) {
      // +++
    }
    return ezp;
  }

  // STATIC STUFF
  private static final MapCache staticcache = MapCache.SoftHashtableCache(new SinetClass());
}

