package net.paymate.database;

/**
 * Title:        GenericDB
 * Description:  Base class for all DB classes
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: GenericDB.java,v 1.9 2001/11/17 06:16:58 mattm Exp $
 */

import  java.sql.*;
import  java.util.*;
import  net.paymate.util.*;

public class GenericDB {

  private static final ErrorLogStream dbg = new ErrorLogStream(GenericDB.class.getName(), ErrorLogStream.WARNING);

  // used once per database (even if)
  // EXTEND AND OVERLOAD TO VALIDATE YOUR DATABASE, but call it before you proceed!
  protected void validate() {
    dbg.ERROR((Safe.preloadClass(drivername) ? "Preloaded" : "Could not preload") + " " + drivername);
  }

  protected boolean validated() {
    return (conn != null) && conn.validated;
  }

  protected void validated(boolean value) {
    if(conn != null) {
      conn.validated = value;
    }
  }

  private static final ConnectionList list = new ConnectionList();
  public static final int connectionCount() {
    return list.size();
  }
  private static final Monitor genericDBclassMonitor = new Monitor("GenericDB.class");

  protected String drivername = "";

  // +++ eventually package the connDataSource, username, and password into a single object
  public GenericDB(DBConnInfo connInfo) {
    try {
      this.drivername = connInfo.drivername;
      genericDBclassMonitor.getMonitor();
      conn = list.get(connInfo.connDatasource);
      if(conn == null) {
        conn = new DBConn(connInfo);
        list.add(conn);
      }
    } finally {
      genericDBclassMonitor.freeMonitor();
    }
  }

  private DBConn conn = null;
  private Monitor connMonitor = new Monitor("DBMacros.conn");
  private DatabaseMetaData dbmd; // the metadata for the database
  public final DatabaseMetaData getDatabaseMetadata() {
    try {
      dbg.Enter("getDatabaseMetadata");
      Connection mycon = getCon();
      if(mycon != null) {
        try {
          connMonitor.getMonitor();
          dbmd = mycon.getMetaData();
        } finally {
          connMonitor.freeMonitor();
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


  public final Connection getCon() {
    return conn.getCon();
  }

}

class ConnectionList extends /*WeakSet*/ Vector {
  public ConnectionList() {
  }
  /**
   * Eventually make this function deal with usernames and passwords?
   */
  public DBConn get(String connName) {
    DBConn conn = null;
    for(Iterator i = this.iterator() ; i.hasNext();) {
      Object o = i.next();
      if(o instanceof DBConn) {
        DBConn connTest = (DBConn) o;
        if(connTest.is(connName)) {
          conn = connTest;
        }
        break;
      }
    }
    return conn;
  }
}

