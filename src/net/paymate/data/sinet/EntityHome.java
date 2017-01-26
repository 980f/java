package net.paymate.data.sinet;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/EntityHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.data.UniqueId;
import net.paymate.util.MapCache;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.Monitor;

// Extend this to either a CfgEntityHome or a LogEntityHome.
// Then, extend those for your data types.

public abstract class EntityHome {

  /////////////////////////
  // static stuff

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EntityHome.class);

  // +++ replace this with the ConnectionPool manager, who can give us a pooled connection to PayMateDB when we need it!
  // for now, don't use this for transactions, drawers, batches, or any other "log" type of data, only configuration type data
  // cfg type data will tend to have few edits for its long lifetime, and have relatively few instances
  // txn type data will tend to have many edits for its short lifetime, and have very many instances
  // ++++  create a setCfg() function that the constructor calls. Don't do this for the whole system all at once!  Mutex the function!
  public static final void init(ConfigurationManagerDispenser newconfigger) { // called by entitybase
    if(!inited) {
      configger = newconfigger; // for getting it on subsequent calls
      inited = true;
    }
  }
  private static boolean inited = false;

  private static ConfigurationManagerDispenser configger = null;
  protected static final ConfigurationManager cfg() { // was /*package*/
    if(configger != null) {
      return configger.getConfigurationManager();
    } else {
      return null;
    }
  }

  // get/new monitors
  private static final Monitor [] monitors = new Monitor[(new SinetClass()).numValues()];
  static {
    // create them all NOW
    SinetClass sc = new SinetClass();
    for(int i = monitors.length; i-->0;) {
      sc.setto(i);
      monitors[i] = new Monitor(sc.Image()+"GetMutexMonitor");
    }
  }

  // end static stuff
  ///////////////////

  protected final SinetClass myclass = new SinetClass();
  protected MapCache cache = null; // extended constructors will set this
  protected EntityHome(SinetClass klass) {
    myclass.setto(klass);
  }

  // these will be used by the final extensions, but not by the public.
  // the public will use the functions written on the extensions themselves
  protected final EntityBase getFromCache(UniqueId id) {
    return (EntityBase)cache.get(id.hashKey(), myclass);
  }

  protected final void putIntoCache(UniqueId id, EntityBase ebase){
    cache.put(id.hashKey(), myclass, ebase);
  }

  // Here we have the get/new functions, to be called by the extended classes, which each have their own get functions ...

  // creates a brand new empty entity!
  protected final EntityBase New(UniqueId parentid, Class entityclass) {
    // +++ check that entityclass matches myclass
    EntityBase ret = null;
    Monitor tgmm = monitors[myclass.Value()];
    tgmm.getMonitor();
    try {
      if(SinetClass.IsLegal(myclass)) {
        UniqueId id = cfg().New(myclass, parentid);
        if(UniqueId.isValid(id)) {
          ret = getEntity(id, entityclass);
        } else {
          // +++ need a panic!
          dbg.ERROR("New(): cfg().New("+myclass+", "+parentid+") returned invalid index!");
        }
      } else {
        // +++ need a panic!
        dbg.ERROR("New(): myclass is illegal!");
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      tgmm.freeMonitor();
      return ret;
    }
  }

  protected final EntityBase getEntity(UniqueId id, Class entityclass) {
    // +++ check that entityclass matches myclass ?
    return getEntityBySinetClass(id, entityclass, myclass);
  }

  // when getting an object,
  // 1: try to get it from the cache
  // 2: or else have the configger load it and then put it in the cache
  private final EntityBase getEntityBySinetClass(UniqueId id, Class entityclass, SinetClass sinetclass) {
    // +++ check that entityclass and sinetclass match?
    String termidstr = String.valueOf(id);
    Monitor tgmm = monitors[sinetclass.Value()];
    EntityBase ret = null;
    tgmm.getMonitor();
    try {
      // first, try to find one
      ret = getFromCache(id);
      // then, try to load one from storage
      if(ret == null) {
        // create and load one!
        ret = (EntityBase) entityclass.newInstance();
        if(ret != null) {
          if (!ret.load(id)) { // +++ do something with return value!
            dbg.ERROR("Entity Not Found! " + id);
            ret.id().Clear(); // erase the id to indicate failure
          } else {
            putIntoCache(id, ret);
          }
        }
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      tgmm.freeMonitor();
      return ret;
    }
  }
}
