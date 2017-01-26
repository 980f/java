package net.paymate.database;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/database/PayMateDBDispenser.java,v $</p>
 * <p>Description: Both associates a PayMateDB with a thread and also acts as a
 *                 ServiceConfigurator</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.ServiceConfigurator;
import net.paymate.util.EasyCursor;
import net.paymate.data.sinet.ConfigurationManagerDispenser;
import net.paymate.data.sinet.ConfigurationManager;

public class PayMateDBDispenser
    implements ServiceConfigurator, ConfigurationManagerDispenser {

  // static stuff ...
  public static void init(DBConnInfo connInfo) {
    tlpmdb = new ThreadLocalPayMateDB(connInfo);
  }
  private static ThreadLocalPayMateDB tlpmdb = null;
  public static PayMateDB getPayMateDB() {
    return (tlpmdb == null) ? null : (PayMateDB) tlpmdb.get();
  }

  public ConfigurationManager getConfigurationManager() {
    return getPayMateDB();
  }

  // ServiceConfigurator stuff
  public String getServiceParam(String serviceName, String paramname, String defaultValue) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getServiceParam(serviceName, paramname, defaultValue) : defaultValue;
  }
  public double getDoubleServiceParam(String serviceName, String paramname, double defaultValue) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getDoubleServiceParam(serviceName, paramname, defaultValue) : defaultValue;
  }
  public long getLongServiceParam(String serviceName, String paramname, long defaultValue) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getLongServiceParam(serviceName, paramname, defaultValue) : defaultValue;
  }
  public int getIntServiceParam(String serviceName, String paramname, int defaultValue) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getIntServiceParam(serviceName, paramname, defaultValue) : defaultValue;
  }
  public boolean getBooleanServiceParam(String serviceName, String paramname, boolean defaultValue) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getBooleanServiceParam(serviceName, paramname, defaultValue) : defaultValue;
  }
  public EasyCursor getServiceParams(String serviceName, EasyCursor ezc) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getServiceParams(serviceName, ezc) : ezc;
  }
  public EasyCursor setServiceParams(String serviceName, EasyCursor ezc) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.setServiceParams(serviceName, ezc) : ezc;
  }
  public boolean setServiceParam(String serviceName, String paramname, String paramvalue) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.setServiceParam(serviceName, paramname, paramvalue) : false;
  }
  public EasyCursor getAllServiceParams(String serviceName) {
    PayMateDB db = getPayMateDB();
    return (db != null) ? db.getAllServiceParams(serviceName) : new EasyCursor();
  }
}

class ThreadLocalPayMateDB extends ThreadLocal {
  private DBConnInfo connInfo = null;
  public ThreadLocalPayMateDB(DBConnInfo connInfo) {
    this.connInfo = connInfo;
  }
  public Object initialValue() {
    return new PayMateDB(connInfo);
  }
}
