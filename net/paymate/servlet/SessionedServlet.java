/**
 * Title:        SessionedServlet<p>
 * Description:  Servlet base class that manages UserSessions<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SessionedServlet.java,v 1.55 2001/10/27 07:17:28 mattm Exp $
 */

package net.paymate.servlet;
import  net.paymate.web.*;
import  net.paymate.web.page.*;
import  net.paymate.web.color.*;
import  net.paymate.util.*;
import  net.paymate.util.timer.*;
import  javax.servlet.*;
import  javax.servlet.http.*;
import  java.io.*;
import  java.util.*;
import  net.paymate.net.*;
import  net.paymate.connection.*;
import  net.paymate.database.*;

public abstract class SessionedServlet extends HttpServlet {

  private static ErrorLogStream dbg=new ErrorLogStream(SessionedServlet.class.getName());

  // +++ maybe create subfunctions that these methods call that then get overridden in subclasses ???

  // override to do stuff
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    // do nothing.
    dbg.WARNING("Possible hack attack: " + req.getRemoteAddr());
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    // do nothing.
    dbg.WARNING("Possible hack attack: " + req.getRemoteAddr());
  }

  // (not really) anti-hacker devices:
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // do nothing.
    dbg.WARNING("Possible hack attack: " + req.getRemoteAddr());
  }

  protected void doTrace(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // do nothing.
    dbg.WARNING("Possible hack attack: " + req.getRemoteAddr());
  }

  protected void doPut(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    // do nothing.
    dbg.WARNING("Possible hack attack: " + req.getRemoteAddr());
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    // do nothing.
    dbg.WARNING("Possible hack attack: " + req.getRemoteAddr());
  }

  // here is the cleaner stuff ... override these if the defaults aren't what you want.
  public long maxAgeMillis() {
    return DEFAULT_BROWSER_MAX_AGE_MILLIS;
  }
  public long maxUnaccessedMillis() {
    return DEFAULT_BROWSER_MAX_UNACCESSED_MILLIS;
  }

  // this is used to influence the one stored in the session:
  private SessionTermParams terminatums = new SessionTermParams(maxAgeMillis(), maxUnaccessedMillis());
  /*
  Sessions begun from a browser will be monitored for inactivity and disconnected
  after either 10 minutes of inactivy
  or after two hours of connectivity, with activity.
  */
  // +++ maybe later load these from settings
  protected static final long DEFAULT_BROWSER_MAX_UNACCESSED_MILLIS = Ticks.forMinutes(10);
  protected static final long DEFAULT_BROWSER_MAX_AGE_MILLIS        = Ticks.forHours(2);

  public static final boolean WITHOUTSECURITY = false;
  public static final boolean WITHSECURITY = true;

  public static final UserSession getSession(HttpServletRequest req) {
// +++ mutex !!!!
    return UserSession.extractUserSession(req, true);// --- remove these passwords and put in config files !!!!
  }

  public static final boolean validSession(HttpServletRequest req) {
    /* +++ or if it has been inactive too long */
    return (req.getSession(true) != null) && req.isRequestedSessionIdValid();
  }

  // these are for logging bytes written and read
  public static final Accumulator incoming = new Accumulator(); // approximate
  public static final Accumulator outgoing = new Accumulator();
  public static final Accumulator httptimer = new Accumulator(); // this is for timing txns
  public static final Counter pending = new Counter(); // this is for counting them as they come in and leave

  // +++ going to have to create a public list of UserSessions if we want to
  // +++ keep track of people who login more than one at a time, so that we log
  // +++ them out of old sessions when they log into a new one. (security thing)

  // +++ do not override this!!!  Instead, override one or more of the do*()'s!
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      pending.incr();
      // set the timeouts
      HttpSession session = req.getSession(true);
      UserSession.setTerminatums(session, terminatums);
      // in case it doesn't know yet, tell the cleaner about the session's context
      SessionCleaner.addContext(session.getSessionContext()); // +++ add check for return value?
      // call the parent!
      incoming.add(req.getContentLength()); // accumulate approximate
      StopWatch sw = new StopWatch();
      super.service(req, resp);
      httptimer.add(sw.millis());
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      pending.decr();
    }
  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      UserSession.init(extractConfigs(config, this), new ServletPrintStream(this));
    } catch (Throwable e) {
      dbg.Caught(e);
      log("SessionedServlet.init(): "+e);
    }
  }

  public void destroy() {
    // +++ this guy has to notify the cleaner to shutdown ?
    // +++ and the cleaner has to kill all of the remaining sessions ?
    UserSession.shUtdOwn(null);
    Thread.yield();
    SessionCleaner.kill();
    Thread.yield();
    LogFile.flushAll();
    Thread.yield();
    super.destroy();
  }

  private static final EasyCursor extractConfigs(ServletConfig config, HttpServlet thiser) {
    EasyCursor cursor = null;
    try {
      cursor = new EasyCursor();
      if(config != null) {
        for(Enumeration enum = config.getInitParameterNames(); enum.hasMoreElements();) {
          String name = (String)enum.nextElement();
          String value = (String)config.getInitParameter(name);
          cursor.setString(name, value);
          dbg.VERBOSE("Loaded parameter: " + name + "=" + value);
        }
      }
    } catch (Exception e) {
      thiser.log("SessionedServlet.extractConfigs: " + e);
    } finally {
      return cursor;
    }
  }

}

class ServletPrintStream extends PrintStream {
  HttpServlet servlet = null;
  public ServletPrintStream(HttpServlet servlet) {
    super(new NullOutputStream(), true);
    this.servlet = servlet;
  }
  public void println(String str) {
    servlet.log(str);
  }
}
