package net.paymate.database;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/database/ConnectionPool.java,v $</p>
 * <p>Description: Database connection pool </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.29 $
 *
 * NOTE: Can't pool JDBC Statement's.  They are use-once only.
 * However, connection pooling is available in JDBC (newest).
 * However, that will only be beneficial if the pooling happens with discarded connections only.
 * Having multiple users of live connections will still need to be managed manually.
 * Here's how we do it:
 *
 * This pool consists of two inner pools:
 * 1) The pool of unused connections.
 * 2) The pool of in-use connections.
 *
 * In addition, it contains a counter for how many connections were found "bad" and thrown away.
 */

// +++  need to mark when the last time a connection was removed from the pool
// +++    for external usage (not for keepalive),
// +++    and when a connection is N minutes/hours old, close it ???

import java.sql.*;
import java.util.*;
import net.paymate.util.*;
import net.paymate.util.timer.StopWatch;
import net.paymate.lang.StringX;
import net.paymate.lang.ReflectX;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

public class ConnectionPool {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ConnectionPool.class, ErrorLogStream.WARNING);

  private DBConnInfo connInfo;

  public void getProfile(EasyProperties ezc) {
    ezc.setProperty(connInfo.connDatasource, "avail="+available()+", used="+used()+", discarded="+discarded());
  }

  public boolean validated = false;

  public ConnectionPool(DBConnInfo connInfo) {
    this.connInfo = connInfo;
    boolean test = ReflectX.preloadClass(connInfo.drivername);
    dbg.ERROR((test ? "Preloaded" : "Could not preload") + " " + connInfo.drivername);
    caretaker = new ConnectionCaretaker(this);
  }

  public boolean is(DBConnInfo conninfo) {
    return (this.connInfo != null) && this.connInfo.is(conninfo);
  }

  private ConnectionFifo mConnections = new ConnectionFifo();
  public int available() {
    int s = mConnections.size();
    dbg.VERBOSE("returning size " + s);
    return s;
  }

  private Vector checkedOut = new Vector(100,100);
  public int used() {
    return checkedOut.size();
  }

  private Counter thrownAway = new Counter();
  public long discarded() {
    return thrownAway.value();
  }

  public long liveCount() {
    return available() + used();
  }

  private ConnectionCaretaker caretaker = null;

  public void startCaretaker() {
    caretaker.setPrebuild(connInfo.oversize);
    caretaker.setIntervalSecs(connInfo.intervalsecs);
    caretaker.start();
  }

  /**
   * Returns a database connection from the pool.
   * Occasionally, this will take about 1.5 seconds to complete when a new one has to be created.
   * When this happens, it will ONLY affect the thread acquiring the connection, not any other threads!
   * DO NOT synchonize this function or you will make all threads needing a cnxn wait on the one acquiring one!
   *
   * @return           Database connection
   */
  private static final Accumulator coacc = new Accumulator();
  public Connection checkOut() {
    Connection dbConnection = null;
    StopWatch getcontime = new StopWatch();
    if (mConnections.size() > 0) {
      dbConnection = mConnections.next();
      if (!liveConnection(dbConnection, connInfo.keepaliveSQL)) { // if it isn't good
        throwAway(dbConnection);
        dbConnection = checkOut(); // get a new one
      } else {
        // put it in the checkedout list
        checkedOut.add(dbConnection);
      }
    } else { // create one
      try {
        dbg.Enter("checkOut()");
        this.connInfo = connInfo;
        Properties lProp = new java.util.Properties();
        lProp.put("user", connInfo.connUser);
        lProp.put("password", connInfo.connPass);
        dbg.VERBOSE("Attempting connection to: '" + connInfo.connDatasource + "'.");
        dbConnection = DriverManager.getConnection(connInfo.connDatasource, lProp);
        dbConnection.setAutoCommit(true);
        logAutocommit(dbConnection);
        setEnableSeqScanOff(dbConnection);
        // put it in the checkedout list
        checkedOut.add(dbConnection);
        // +++ create the prepared statements here +++ !!! ??? -- NO point.  PG doesn't do anything with them.
        // getDatabaseMetadata(); // hope removing this doesn't kill anything
      } catch (Exception e) {
        dbConnection = null;
        boolean wasSql = (e.getClass() == SQLException.class);
        dbg.ERROR("Couldn't connect to datasource '" + connInfo.connDatasource + "' via user: '" + connInfo.connUser + "'\n");
        dbg.Caught(e);
      } finally {
        dbg.VERBOSE("Done attempting connection.");
        dbg.Exit();
      }
    }
    coacc.add(getcontime.Stop());
    return dbConnection;
  }

  private void logAutocommit(Connection dbConnection) {
    int ac = -1;
    try {
      dbg.VERBOSE("Calling Connection.getAutoCommit() ...");
      ac = (dbConnection.getAutoCommit() ? 1 : 0);
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.VERBOSE("Done calling Connection.getAutoCommit().");
      dbg.WARNING("Autocommit is " + ((ac == 0) ? "OFF" : ((ac == 1) ?  "ON" : "UNKNOWN")));
    }
  }

  private static final Accumulator lcacc = new Accumulator();
  // this is public so other lines of code can check their connection ...
  public static boolean liveConnection(Connection tocheck, String keepaliveSQL) {
    boolean ret = false;
    ResultSet rs = null;
    dbg.Enter("liveConnection");
    try {
      StopWatch sw = new StopWatch();
      // this is a quick check to make sure that the connection is live
      if(tocheck == null) {
        dbg.ERROR("tocheck == null");
      } else if(tocheck.isClosed()) {
        dbg.ERROR("tocheck is closed");
      } else {
        // check that it is good
        try {
          dbg.VERBOSE("Calling keepalive ...");
//          rs = tocheck.getMetaData().getTableTypes();
          /* other suggested queries:
           select '';
           select NULL;
           select current_user;
           select now();
           select version();
           */
          Statement stmt = tocheck.createStatement();
          rs = stmt.executeQuery(StringX.TrivialDefault(keepaliveSQL, "select 1"));
        } catch (Exception ex) {
          dbg.Caught(ex);
        } finally {
          dbg.VERBOSE("Done calling keepalive.");
        }
        ret = (rs != null);
        if (!ret) {
          dbg.ERROR("table types are null");
        } else {
          dbg.VERBOSE("connection is live");
        }
      }
      lcacc.add(sw.Stop());
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      dbg.Exit();
      DBMacros.closeRS(rs);
      return ret;
    }
  }

  private void setEnableSeqScanOff(Connection tocheck) {
    Statement stmt = null;
    try {
      dbg.VERBOSE("Calling Connection.createStatement() ...");
      stmt = tocheck.createStatement();
      dbg.VERBOSE("Done calling Connection.createStatement().");
      if (stmt != null) {
        // insert it into the table
        dbg.VERBOSE("Calling Connection.executeUpdate() ...");
        /*retval = */stmt.executeUpdate("SET ENABLE_SEQSCAN = OFF");// bad habit! Use querystring!
        dbg.VERBOSE("Done calling Connection.executeUpdate().");
      }
    } catch (Exception ex) {
        dbg.Caught(ex);
    } finally {
      if(stmt != null) {
        DBMacros.closeStmt(stmt);
      }
    }
  }

  /**
   * Check a connection back into the pool.
   *
   * @param            pConnection          Database connection to return to the pool.
   */
  public void checkIn(Connection pConnection) {
    if(liveConnection(pConnection, connInfo.keepaliveSQL)) {
      checkedOut.removeElement(pConnection); // remove it from the checked out list
      mConnections.put(pConnection); // add it to the list
    } else {
      dbg.ERROR("Connection is no longer live!");
      thrownAway.incr(); // add up that you threw it away
      // be sure to close it.  It may be live, but have trouble!
      closeConnection(pConnection); // to be sure
    }
  }

  public void prebuild(int howmany) {
    // make sure you can get N connections ...
    Connection [] cps = new Connection[howmany];
    try {
      for(int i = howmany; i-->0;) {
        cps[i] = checkOut();
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      for(int i = howmany; i-->0;) {
        checkIn(cps[i]);
      }
    }
  }

  private void throwAway(Connection conn) {
    if(conn != null) {
      checkedOut.removeElement(conn); // to be sure
      closeConnection(conn); // to be sure
      try {
        PGConnection pgc = (PGConnection)conn;
        PGNotification[] notices = pgc.getNotifications();
        if((notices != null) && (notices.length > 0)) {
          TextList tl = new TextList();
          for(int i = 0; i < notices.length; i++) { // print them in the order they occurred
            PGNotification pgn = notices[i];
            tl.add("PID_"+pgn.getPID()+":"+pgn.getName());
            dbg.ERROR("Throwing away connection [" + conn.toString() +
                      "] resulted in notifications: " + tl.asParagraph(","));
          }
        }
      } catch (Exception ex) {
        dbg.Caught(ex);
      } finally {
        thrownAway.incr(); // add up that you threw it away
      }
    }
  }



  private void closeConnection(Connection conn) {
    try {
      if(conn != null) {
        conn.close();
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  // refresh the ones that exist and are unused
  public void checkUnused() {
    try {
// this only checks the same one N times! ...
//      for(int i = available(); i-->0;) {
//        Connection conn = checkOut();
//        if(conn != null) {
//          checkIn(conn);
//        }
//      }
      // This works better ...
      ConnectionFifo cf = new ConnectionFifo();
      // checkout all connections that are available
      while(available() > 0) {
        cf.put(checkOut());
      }
      // then put them all back!
      while(cf.size() > 0) {
        checkIn(cf.next());
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    }
  }

  // this is a way to dump to log all of the currently-in-use connections
  // +++ however, before this can be used (or coded),
  // we need to create a class that CONTAINS a Connection
  // and make ConnectionFifo use that instead of Connection.
  // The reason for this is that the NAME of the thread that is using the connection
  // cannot easily be associated with the connection, but an object could contain both.
  public void spamUsed() {

// THIS WORKS!
//    new String(new char[] {'a','b'});

//    try {
//      Iterator connit =  checkedOut.iterator();
//      while(connit.hasNext()) {
//        Object o = connit.next();
//        Connection c = (Connection)o;
//
//      }
//    } catch (Exception ex) {
//
//    }
  }

  public void releaseUnused() {
// +++
  }

  /**
   * Shutdown the connection pool by closing all connections to the JDBC driver.
   */
  public synchronized void shutdown() {
    caretaker.stop(); // to prevent it from creating new ones
    Connection tempConnection = null;
    while(mConnections.size() > 0) {
      throwAway(mConnections.next());
    }
  }

  public void finalize() {
    shutdown();
  }

}


//$Id: ConnectionPool.java,v 1.29 2004/03/13 01:29:30 mattm Exp $
