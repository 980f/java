package net.paymate.data.sinet;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/CfgEntityHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */


import net.paymate.data.UniqueId;
import net.paymate.util.MapCache;
import net.paymate.util.ErrorLogStream;
import java.util.Iterator;
import java.util.Set;

// Extend this class for each kind of CFG entity.
// Eg: for Enterprise class, create an EnterpriseHome extension of this class.

public abstract class CfgEntityHome extends EntityHome {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CfgEntityHome.class);
  private Class myClass = null; // used for loading stuff

  protected CfgEntityHome(SinetClass myclass, Class Klass) {
    super(myclass);
    cache = staticcache;
    myClass = Klass;
  }

  // this gets all from DISK.  Do we need a separate GETALL for the cache?
  // I like always going to disk until we get this stuff nailed down solid.
  protected UniqueId [ ] getAll(Class uniqueidExtentClass) {
    return cfg().getIds(myclass, uniqueidExtentClass);
  }

  // this preloads by getting all from disk
  protected UniqueId [ ] preload(Class uniqueidExtentClass) {
    UniqueId [ ] ids = getAll(uniqueidExtentClass);
    // load them all (so that the entire table is loaded from disk)
    for(int i = ids.length; i-->0;) {
      getEntity(ids[i], myClass);
    }
    return ids;
  }

  protected final EntityBase get(UniqueId id) {
  EntityBase e = null;
  if(UniqueId.isValid(id)) {
    e = super.getEntity(id, myClass);
  } else {
    dbg.ERROR("Cannot Get "+myClass.getName()+" for invalid id ["+id+"]!");
  }
  return e;
}

  protected final EntityBase New(UniqueId parentid) {
    return super.New(parentid, myClass);
  }

  // STATIC STUFF
  private static final MapCache staticcache = MapCache.HashtableCache(new SinetClass());
}

