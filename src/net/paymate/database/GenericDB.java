package net.paymate.database;

/**
 * Title:        GenericDB
 * Description:  Base class for all DB classes
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: GenericDB.java,v 1.29 2004/03/13 01:29:31 mattm Exp $
 */

import  java.sql.*;
import  java.util.*;
import  net.paymate.util.*;

public class GenericDB {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(GenericDB.class, ErrorLogStream.VERBOSE);

  protected boolean validated() {
    return (cpool != null) && cpool.validated;
  }

  protected void validated(boolean value) {
    if(cpool != null) {
      cpool.validated = value;
    }
  }

  public static final EasyProperties getPoolProfile() {
    return list.getProfile();
  }

  private static final ConnectionPoolList list = new ConnectionPoolList(); // list of ConnectionPools
  public static final int connectionCount() {
    int cnxns = list.connectionCount();
    dbg.ERROR("cnxns returned " + cnxns);
    return cnxns;
  }
  public static final int connectionsUsed() {
    int cnxns = list.connectionsUsed();
    dbg.ERROR("cnxns returned " + cnxns);
    return cnxns;
  }

  protected static final void startCaretakers() {
    list.startCaretakers();
  }

  private static final Monitor genericDBclassMonitor = new Monitor(GenericDB.class.getName());

  private static final Counter metaDataCounter = new Counter();

  private DBConnInfo connInfo;
  private ConnectionPool cpool;
  private Connection conn;
  private String myThreadName = "NOTSETYET";

  // +++ eventually package the connDataSource, username, and password into a single object
  public GenericDB(DBConnInfo connInfo, String threadname) {
    genericDBclassMonitor.getMonitor();
    try {
      this.myThreadName = threadname;
      this.connInfo = connInfo;
      connMonitor = new Monitor(GenericDB.class.getName()+".MetaData."+metaDataCounter.incr());
      cpool = list.get(connInfo);
      getCon();
    } finally {
      genericDBclassMonitor.freeMonitor();
    }
  }

  public void finalize() {
    releaseConn();
  }

  public void releaseConn() {
    try {
      if(conn != null) {
        list.get(connInfo).checkIn(conn);
        conn = null;
        dbg.WARNING("releasing connection for thread \"" + myThreadName + "\" !");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  private Monitor connMonitor = null;
  private DatabaseMetaData dbmd; // the metadata for the database
  public final DatabaseMetaData getDatabaseMetadata() {
    try {
      dbg.Enter("getDatabaseMetadata");
      Connection mycon = getCon();
      if(mycon != null) {
        connMonitor.getMonitor();
        try {
          dbg.VERBOSE("Calling Connection.getMetaData() ...");
          dbmd = mycon.getMetaData();
        } finally {
          connMonitor.freeMonitor();
          dbg.VERBOSE("Done calling Connection.getMetaData().");
        }
      }
    } catch (Exception t) {
      dbg.Caught(t);
      dbmd = null;
    } finally {
      dbg.Exit();
      return dbmd;
    }
  }

  // there is a mutex in this class, but it should be very fast.
  // +++ @@@ %%% should we put this in a loop so that it doesn't continue UNTIL it gets a connection?
  public final Connection getCon() {
    connMonitor.getMonitor();
    try {
      if(conn == null) {
        dbg.WARNING("conn is null, so getting new connection for thread \"" + myThreadName + "\" !");
        conn = cpool.checkOut();
      } else {
        if(!cpool.liveConnection(conn, connInfo.keepaliveSQL)) {
          conn = null;
          return getCon(); // potential infinite loop
        }
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      connMonitor.freeMonitor();
      return conn;
    }
  }

}

// allows us to have different pools for connections to different databases
// actually exists so that we can synchronize the connectioncount, but we might give that up later.
class ConnectionPoolList {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ConnectionPoolList.class);

  public ConnectionPoolList() {
  }

  private Vector content = new Vector(10,10);

  /**
   * Eventually make this function deal with usernames and passwords?
   */
  public synchronized ConnectionPool get(DBConnInfo conninfo) {
    ConnectionPool conn = null;
    for(Iterator i = content.iterator() ; i.hasNext();) {
      Object o = i.next();
      if(o instanceof ConnectionPool) {
        ConnectionPool connTest = (ConnectionPool) o;
        if(connTest.is(conninfo)) {
          conn = connTest;
        }
        break;
      }
    }
    if(conn == null) {
      conn = new ConnectionPool(conninfo);
      content.add(conn);
    }
    return conn;
  }

  public synchronized EasyProperties getProfile() {
    EasyProperties ezc = new EasyProperties();
    for(Iterator i = content.iterator() ; i.hasNext();) {
      try {
        Object o = i.next();
        ConnectionPool cp = (ConnectionPool)o;
        if(cp != null) {
          cp.getProfile(ezc);
        }
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    }
    return ezc;
  }

  public synchronized int connectionCount() {
    int count = 0;
    int ct = 0;
    for(Iterator i = content.iterator() ; i.hasNext();) {
      try {
        Object o = i.next();
        ct++;
        ConnectionPool cp = (ConnectionPool)o;
        if(cp != null) {
          count += cp.liveCount();
        }
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    }
    dbg.VERBOSE("returning " + count + " connections for " + ct + " pools.");
    return count;
  }

  public synchronized int connectionsUsed() {
    int count = 0;
    int ct = 0;
    for(Iterator i = content.iterator() ; i.hasNext();) {
      try {
        Object o = i.next();
        ct++;
        ConnectionPool cp = (ConnectionPool)o;
        if(cp != null) {
          count += cp.used();
        }
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    }
    dbg.VERBOSE("returning " + count + " used connections for " + ct + " pools.");
    return count;
  }


  protected synchronized void startCaretakers() {
    for(Iterator i = content.iterator() ; i.hasNext();) {
      try {
        Object o = i.next();
        ConnectionPool cp = (ConnectionPool)o;
        if(cp != null) {
          cp.startCaretaker();
        }
      } catch (Exception ex) {
        dbg.Caught(ex);
      }
    }
  }

}

