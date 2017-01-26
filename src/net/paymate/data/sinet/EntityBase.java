package net.paymate.data.sinet;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/EntityBase.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.17 $
 */

import net.paymate.data.*; // UniqueId
import net.paymate.util.*; // ErrorLogStream

// each field will also have a data class to represent it
// fields must be loadable, must be completely wrapped with get/set, and must completely support dirty bits
// dirty bits must roll up to containers
// if we do this, how do we export this data to the terminal?
// perhaps don't give them a container?

public abstract class EntityBase {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EntityBase.class);

  private static final ConfigurationManager cfg() {
    return EntityHome.cfg();
  }

  ////////////////////
  /// instance stuff

  public abstract SinetClass getSinetClass();
  public abstract void loadFromProps();
  public abstract void storeToProps();

  private UniqueId id = null;
  protected Monitor mymonitor;
  protected UniqueId setId(UniqueId id) {
    if(UniqueId.isValid(id)) {
      this.id = id;
      mymonitor = new Monitor(id.makeMutexName());
    }
    return id();
  }
  public UniqueId id() {
    return id;
  }

  // +++
  // the extended classes must all allow creation via some constructor that has no uniqueid passed
  // the user will need a relatively empty one to fill before inserting into the database!
  // ACTUALLY, DO NOT extend this.  Make it unaccessible
  // +_+ eventually will use FinderException from EJB package
  // +++ this needs to throw !!!
  // --- for now, we set the id to invalid if we didn't find it
  protected EntityBase() {
  }


  protected boolean load(UniqueId id) {
    boolean found = false;
    if(cfg() != null) {
      this.id = id;
      found = cfg().loadEntity(this);
    }
    dbg.ERROR("prior to loadFromProps: props="+myProps);
    loadFromProps();
    return found;
  }

  protected boolean store() {
    boolean stored = false;
    storeToProps();
    if(cfg() != null) {
      stored = cfg().storeEntity(this);
      // if it doesn't have an id, do we insert?
    }
    return stored;
  }

  private EasyProperties myProps = new EasyProperties(); // temp
  public boolean setProps(EasyProperties mynewProps) {
    // +++ diff them here and then write them to the database?  Currently, diff happens in database itself.  Can do it here later.
    myProps = mynewProps;
    return true;
  }
  public EasyProperties getProps() {
    return myProps;
  }
}
