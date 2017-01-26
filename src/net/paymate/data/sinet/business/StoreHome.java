package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/StoreHome.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.ErrorLogStream;
import net.paymate.data.sinet.*;
import net.paymate.data.*;

public class StoreHome extends CfgEntityHome {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreHome.class);
  private static StoreHome myhome = new StoreHome(); // singleton

  ///////////////////////
  // STATICS
  public static final Store Get(Storeid id) {
    return myhome.get(id);
  }

  public static final Store New(Enterpriseid parent) {
    return myhome.knew(parent);
  }

  public static final Storeid [ ] GetAllIds() {
    return (Storeid [ ])myhome.preload(Storeid.class);
  }

  // slower than the above query, most likely
  public static final Store [ ] GetAllTxnCountOrder() {
    return StoresFromIds(myhome.getAllTxnCountOrder());
  }

  public static final Store [ ] GetAll() {
    return StoresFromIds((Storeid [])GetAllIds());
  }

  private static final Store [ ] StoresFromIds(Storeid [ ] ids) {
    Store [ ] ret = null;
    if(ids != null) {
      ret = new Store[ids.length];
    } else {
      ret = new Store[0];
    }
    for(int i = ret.length; i-->0;) {
      ret[i] = Get(ids[i]);
    }
    return ret;
  }
  // END STATICS
  ///////////////////////

  public StoreHome() {
    super(new SinetClass(SinetClass.Store), Store.class);
  }

  private final Store get(Storeid id) {
    EntityBase base = super.get(id);
    return ((base != null) && (base instanceof Store)) ? (Store)base : (Store)null;
  }

  public final Storeid [ ] getAllTxnCountOrder() {
    return ((BusinessCfgMgr)cfg()).getAllStoresTxnCountOrder();
  }

  private synchronized final Store knew(Enterpriseid parent) {
    EntityBase base = New(parent, Store.class);
    if(base instanceof Store) {
      Store newStore = (Store) base;
      return newStore;
    } else {
      return null;
    }
  }

}