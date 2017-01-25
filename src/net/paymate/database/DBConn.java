/**
 * Title:        DBConn<p>
 * Description:  Manages a database connection<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: DBConn.java,v 1.30 2001/11/16 01:34:28 mattm Exp $
 */

package net.paymate.database;
import  net.paymate.util.*;
import  java.sql.*;
import  java.util.Properties;

public class DBConn {

  private static final ErrorLogStream dbg = new ErrorLogStream(DBConn.class.getName());

  // !!! do not make this public.  If you need the info, make functions!
  private DBConnInfo connInfo = null;

  public DBConn(DBConnInfo connInfo) {
    this.connInfo = connInfo;
  }

  private Connection con = null;
  private ConCloser closer= new ConCloser();

  public boolean validated = false;

  private static final Monitor dbConnMonitor = new Monitor("DBConn.class");

  // statements should be closed regularly, though
  /*package*/ static final void close(Connection con) {
      // undo the database connection!
    try {
      dbConnMonitor.getMonitor();
      dbg.Enter("close");
      if((con != null) && (!con.isClosed())) {
        con.close();
        con = null;
      }
    } catch (Exception t) {
      // who cares?
      dbg.Caught(t);
    } finally {
      dbConnMonitor.freeMonitor();
      dbg.Exit();
    }
  }

  public Connection getCon() {
    try {
      dbConnMonitor.getMonitor();
      if(con != null) {
        try {
          if(con.isClosed()) {
            con = null;
          }
        } catch (Exception ex) {
          // stub ??? +++
        }
      }
      if (con == null) {
        try {
          dbg.Enter("getCon");
          Properties lProp = new java.util.Properties();
          lProp.put("user", connInfo.connUser);
          lProp.put("password", connInfo.connPass);
          dbg.VERBOSE("Attempting connection to: '" + connInfo.connDatasource + "'.");
          con = DriverManager.getConnection(connInfo.connDatasource, lProp); // +++ problem is here
          closer.conn = con;
          con.setAutoCommit(true);
          logAutocommit();
          // getDatabaseMetadata(); // hope removing this doesn't kill anything
        } catch (Exception e) {
          con = null;
          boolean wasSql = (e.getClass() == SQLException.class);
          dbg.ERROR("Couldn't connect to datasource '" + connInfo.connDatasource + "' via user: '" + connInfo.connUser + "'\n");
          dbg.Caught(e);
        } finally {
          dbg.Exit();
        }
      }
    } finally {
      dbConnMonitor.freeMonitor();
      return con;
    }
  }

  private void logAutocommit() {
    int ac = -1;
    try {
      ac = (con.getAutoCommit() ? 1 : 0);
    } catch (Exception e) {
      dbg.Caught(e);
    }
    dbg.ERROR("Autocommit is " + ((ac == 0) ? "OFF" : ((ac == 1) ?  "ON" : "UNKNOWN")));
  }

  // so we only make one connection per database
  public boolean is(String connName) {
    return Safe.NonTrivial(connName) && (connInfo!=null) && Safe.NonTrivial(connInfo.connDatasource) && connInfo.connDatasource.equals(connName);
  }

}

class ConCloser {
  public Connection conn = null;
  protected void finalize() {
    if(conn != null) {
      DBConn.close(conn);
      // +++ report this?
    }
  }
}

