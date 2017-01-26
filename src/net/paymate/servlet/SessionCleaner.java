/**
 * Title:        Cleaner<p>
 * Description:  Destroys SSlSessions based on certain expiration times<p>
 *                This will effectively free up a small amount of memory and db connections.
 *                PLEASE only create one of these in the VM!
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SessionCleaner.java,v 1.48 2004/03/13 01:29:31 mattm Exp $
 */

package net.paymate.servlet;
import  javax.servlet.http.*;
import  javax.servlet.*;
import  java.util.Date;
import  java.util.Vector;
import  java.util.Enumeration;
import  net.paymate.util.*;
import  net.paymate.web.UserSession;
import net.paymate.lang.ThreadX;

public class SessionCleaner extends Service {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SessionCleaner.class, ErrorLogStream.WARNING);

  public static final String NAME = "SSLSessionCleaner";

  public static final String INTERVALSECS = "intervalsecs";
  private int intervalSecs = 30;

  // service stuff ...
  public SessionCleaner(ServiceConfigurator configger, int interval) {
    super(NAME, configger, true);
    this.intervalSecs = interval;
    initLog();
    up();
  }
  public void down() {
    broom.kill();
    markStateChange();
    dbg.ERROR("SessionCleaner is dead.");
  }
  public void up() {
    try {
      if((broom == null) || !broom.isAlive()) {
        broom = new CleanerThread(this);
        broom.intervalSecs = this.intervalSecs;
        broom.setDaemon(true);
        broom.start();
        markStateChange();
      }
    } catch (IllegalThreadStateException itse) {
      // don't care
    }
  }
  public boolean isUp() {
    return broom.isAlive();
  }
  public String svcTimeouts() {
    return ""+count.value();//"Invalidated " + SessionCleaner.count.value() + " stale sessions.";
  }
  public String svcCnxns() {
    int i = 0;
    HttpSessionContext [] contexts = getContexts();
    for(int ci = contexts.length; ci-->0;) {
      for(Enumeration enum = contexts[ci].getIds(); enum.hasMoreElements();i++) {
        enum.nextElement();
      }
    }
    return ""+i;
  }

  private CleanerThread broom = null;

  public /*static final*/ Counter count = new Counter();

  // +++ add some sort of thread protection here?; stop the broom?
  public /*static final*/ void addContext(HttpSessionContext newContext) {
    broom.addContext(newContext);
  }

  public /*static final*/ HttpSessionContext [] getContexts() {
    return broom.getContexts();
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

class CleanerThread extends Thread {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CleanerThread.class);

  private final ServerContextList contextList = new ServerContextList();
  private boolean requestStop = false;
  private SessionCleaner cleaner = null;

  public CleanerThread(SessionCleaner cleaner) {
    super(cleaner.serviceName());
    this.cleaner = cleaner;
  }

  public void setStopRequested() {
    requestStop = true;
  }

  private boolean getStopRequested() {
    return requestStop;
  }

  public int intervalSecs = 30; // checking every 30 seconds is fast enough!

/**
 * This function takes the current sessions' contexts and expiration times for the sessions,
 * NOTE that millisAge should always be larger than millisAccessed.
 */

  Monitor contextListMon = new Monitor("CleanerThread.contextListMon");
  Monitor sessionMon = new Monitor("CleanerThread.sessionMon");
  public void run() {
    try {
      long sleepTime = Ticks.forSeconds(intervalSecs);
      // keep sleeping and cleaning on regular intervals
      while(!getStopRequested()) {
        try {
          int count = 0;
//          yield(); // we don't want this guy to eat up too many cycles
          try {
            ThreadX.sleepFor(sleepTime);
          } catch (Exception trash) {
            dbg.Caught(trash);
          }
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
                long systemMillis = DateX.utcNow();
                if(millisAccessed > 0) {
                  Date minsAgo  = new Date(systemMillis - millisAccessed);
                  Date accessed = new Date(session.getLastAccessedTime());
                  if(accessed.before(minsAgo)) {
                    dbg.VERBOSE("Invalidating http session id=" + id + ": Over " + DateX.millisToTime(millisAccessed) + " time since last access.");
                    session.invalidate();
                    cleaner.count.incr();
                    count++; // just for this run
                    continue;
                  }
                }
                if(millisAge > 0) {
                  Date hoursAgo  = new Date(systemMillis - millisAge);
                  Date created  = new Date(session.getCreationTime());
                  if(created.before(hoursAgo)) {
                    dbg.VERBOSE("Invalidating http session id=" + id + ": Over " + DateX.millisToTime(millisAge) + " time since creation.");
                    session.invalidate();
                    cleaner.count.incr();
                    count++; // just for this run
                    continue;
                  }
                }
                // if you made it here, the session was spared
//                yield(); // we don't want this guy to eat up too many cycles
              }
//              yield(); // we don't want this guy to eat up too many cycles
            }
          } catch (Exception mone) {
            dbg.Caught(mone);
          } finally {
            contextListMon.freeMonitor();
          }
          if(count > 0) {
            dbg.WARNING("Invalidated " + count + " stale sessions.");
          } else {
            dbg.VERBOSE("SessionCleaner.run() [inner loop]: No stale sessions found.");
          }
        } catch (Exception caught) {
          dbg.Enter("run");
          dbg.Caught(caught);
          dbg.Exit();
        } finally {
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

  public HttpSessionContext [] getContexts() {
    HttpSessionContext [] contexts = null;
    try {
      contextListMon.getMonitor();
      contexts = new HttpSessionContext[contextList.size()];
      for(int i = contextList.size(); i-->0;) {
        contexts[i] = contextList.getContext(i);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      contextListMon.freeMonitor();
      return contexts;
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
          // unbind all that aren't GODS
          if (session != null) {
            try {
              UserSession user = UserSession.extractUserSession(session);
              if (user != null) {
                user.AtExit();
              }
            } catch (Exception ex) {
              dbg.Caught(ex);
            }
          }
          session.invalidate();
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      contextListMon.freeMonitor();
    }
  }
}
