/**
 * Title:        Cleaner<p>
 * Description:  Cleaner cleans up old user sessions based on certain expiration times<p>
 *                this will effectively free up memory and db connections
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SessionCleaner.java,v 1.32 2001/10/30 19:37:22 mattm Exp $
 */

package net.paymate.servlet;
import  javax.servlet.http.*;
import  javax.servlet.*;
import  java.util.Date;
import  java.util.Vector;
import  java.util.Enumeration;
import  net.paymate.util.*;

/**
 * Sessions begun from our client application will not be monitored for
 * inactivity by the server.  However, the client application will be required
 * to logout+in on a roughly 24-hour period.  This means that after a terminal
 * has been logged in for 24 hours, it will be automatically logged out.
 *
 * This ensures that we do certain kinds of cleanup at the server side, and also
 * ensures that stale sessions do not remain alive forever.  This also means
 * that we do NOT have to spam the server with "I'm alive" messages all the
 * time.
 *
 * Sessions begun from a browser will be monitored for inactivity and disconnected
 * after either 5 minutes (5 * 60 * 1000 =  300000 milliseconds) of inactivy
 * or after two hours (2 * 60 * 60 * 1000 = 7200000 milliseconds) of connectivity, with activity.
**/

public class SessionCleaner {

  private static final CleanerThread broom = new CleanerThread(SessionCleaner.class.getName());
  private static final ErrorLogStream dbg = new ErrorLogStream(SessionCleaner.class.getName());

  static {
    try {
      broom.start();
    } catch (IllegalThreadStateException itse) {
      // don't care
    }
  }

  public static final Counter count = new Counter();

  // +++ add some sort of thread protection here?; stop the broom?
  public static final void addContext(HttpSessionContext newContext) {
    broom.addContext(newContext);
  }

  public static final void kill() {
    broom.kill();
    dbg.ERROR("SessionCleaner is dead.");
  }
}

class ServerContextList extends Vector {
  // returns true if it was a new one.
  public boolean addContext(HttpSessionContext newContext) {
    // first, check to see if we already have it
    for(int i = size(); i-->0; ) {
      if(getContext(i) == newContext) {
        return false;
      }
    }
    // if you are here, it is a new one
    addElement(newContext);
    return true;
  }
  public HttpSessionContext getContext(int i) {
    return (HttpSessionContext) elementAt(i);
  }
}

/** This has statics in it in places, but there really should only be ONE,
 *  and a real non-static one at that
 *  as the containing class should declare an instance of this static!  (+++ fix !!!)
 *  However, you can't make the class totally static, since it is a thread.
 *  Instead, you HAVE to create a single instance.
 */
class CleanerThread extends Thread {

  private static final ErrorLogStream dbg = new ErrorLogStream(CleanerThread.class.getName());

  private static final ServerContextList contextList = new ServerContextList();
  private boolean requestStop = false;

  public CleanerThread(String name) {
    super(name);
  }

  public void setStopRequested() {
    requestStop = true;
  }

  private boolean getStopRequested() {
    return requestStop;
  }

/**
 * This function takes the current sessions' contexts and expiration times for the sessions,
 * NOTE that millisAge should always be larger than millisAccessed.
 */

  Monitor contextListMon = new Monitor("CleanerThread.contextListMon");
  Monitor sessionMon = new Monitor("CleanerThread.sessionMon");
  public void run() {
    try {
      long sleepTime = Ticks.forSeconds(30); // checking every 30 seconds is fast enough!
      // keep sleeping and cleaning on regular intervals
      while(!getStopRequested()) {
        try {
          int count = 0;
          // clean all of the sessions in each context
          try {
            contextListMon.getMonitor();
            for(int contexti = contextList.size(); !getStopRequested() && (contexti --> 0);) {
              HttpSessionContext context = contextList.getContext(contexti);
              // clean all of the sessions in this context
              Enumeration ids = context.getIds();
              while(ids.hasMoreElements() && !getStopRequested()) {
                String id = (String)ids.nextElement();
                HttpSession session = context.getSession(id);
                long millisAccessed = 0;
                long millisAge = 0;
                boolean cont = false;
                try {
                  sessionMon.getMonitor();
                  // get the cleanup parameters
                  Object o = session.getValue(SessionTermParams.sessionTermParamsKey);
                  SessionTermParams sessionTerms = (o instanceof SessionTermParams) ? (SessionTermParams)o : (SessionTermParams)null;
                  if(sessionTerms == null) { // skip this session (it hasn't been setup by the webserver yet; it is still too green; next request will make it work)
                    cont = true;
                  } else { // modify one
                    millisAccessed = sessionTerms.maxUnaccessedMillis;
                    millisAge      = sessionTerms.maxAgeMillis;
                  }
                } catch (Exception mone2) {
                  dbg.Caught(mone2);
                } finally {
                  sessionMon.freeMonitor();
                }
                if(cont) {
                  continue;
                }
                long systemMillis = System.currentTimeMillis();
                if(millisAccessed > 0) {
                  Date minsAgo  = new Date(systemMillis - millisAccessed);
                  Date accessed = new Date(session.getLastAccessedTime());
                  if(accessed.before(minsAgo)) {
                    dbg.VERBOSE("Invalidating http session id=" + id + ": Over " + Safe.millisToTime(millisAccessed) + " time since last access.");
                    session.invalidate();
                    SessionCleaner.count.incr();
                    count++; // just for this run
                    continue;
                  }
                }
                if(millisAge > 0) {
                  Date hoursAgo  = new Date(systemMillis - millisAge);
                  Date created  = new Date(session.getCreationTime());
                  if(created.before(hoursAgo)) {
                    dbg.VERBOSE("Invalidating http session id=" + id + ": Over " + Safe.millisToTime(millisAge) + " time since creation.");
                    session.invalidate();
                    SessionCleaner.count.incr();
                    count++; // just for this run
                    continue;
                  }
                }
                // if you made it here, the session was spared
                yield(); // we don't want this guy to eat up too many cycles
              }
              yield(); // we don't want this guy to eat up too many cycles
            }
          } catch (Exception mone) {
            dbg.Caught(mone);
          } finally {
            contextListMon.freeMonitor();
          }
          if(count > 0) {
            dbg.VERBOSE("Invalidated " + count + " stale sessions.");
          } else {
            //dbg.VERBOSE("SessionCleaner.run() [inner loop]: No stale sessions found.");
          }
        } catch (Exception caught) {
          dbg.Enter("run");
          dbg.Caught(caught);
          dbg.Exit();
        } finally {
        }
//        if(getStopRequested()) { // handled by the while()
//          break;
//        }
        yield(); // we don't want this guy to eat up too many cycles
        try {
          ThreadX.sleepFor(sleepTime);
        } catch (Exception trash) {
          // who cares
        }
      }
    } catch (Throwable e) {
      dbg.Caught(e); // +++ move the lower-leveled exception handling up to here ?? !!
    }
  }

  // +++ add some sort of thread protection here?; stop the broom?
  public void addContext(HttpSessionContext newContext) {
    try {
      contextListMon.getMonitor();
      contextList.addContext(newContext);
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      contextListMon.freeMonitor();
    }
  }

  public void kill() {
    setStopRequested();
    ThreadX.waitOnStopped(this, Ticks.forSeconds(2)); // wait forever
    try {
      contextListMon.getMonitor();
      for(int contexti = contextList.size(); !getStopRequested() && (contexti --> 0);) {
        HttpSessionContext context = contextList.getContext(contexti);
        // clean all of the sessions in this context
        Enumeration ids = context.getIds();
        while(ids.hasMoreElements() && !getStopRequested()) {
          String id = (String)ids.nextElement();
          HttpSession session = context.getSession(id);
          session.invalidate();
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      contextListMon.freeMonitor();
    }
    System.gc(); // clean up the crap
  }
}
