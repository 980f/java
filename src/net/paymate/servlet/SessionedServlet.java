/**
 * Title:        SessionedServlet<p>
 * Description:  Servlet base class that manages UserSessions<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: SessionedServlet.java,v 1.89 2004/03/13 01:29:31 mattm Exp $
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
import net.paymate.io.LogFile;
import net.paymate.io.NullOutputStream;

public abstract class SessionedServlet extends HttpServlet {

  private static final String STARTUPLOG = OS.TempRoot()+"/startup.log";

  static {
    try {
      PrintStream out = new PrintStream(new FileOutputStream(STARTUPLOG)); // temporary
      if(out != null) {
        System.setOut(out);
      }
      System.out.println("setting logswitches and printforks to verbose");
      LogSwitch.SetAll(new LogLevelEnum(LogLevelEnum.VERBOSE)); // until we can load the settings from disk!
      PrintFork.SetAll(new LogLevelEnum(LogLevelEnum.VERBOSE)); // until we can load the settings from disk!
      System.out.println("done.");
//      System.out.println("SessionedServlet: current logging looks like this:\nLogswitches:\n"+LogSwitch.listLevels()+"\nPrintforks:\n"+PrintFork.asProperties());
    } catch (Exception e) {
      System.out.println("Exception creating '"+STARTUPLOG+"' and setting System.out to it.");
      // uh-oh
    }
  }

  private static ErrorLogStream dbg=ErrorLogStream.getForClass(SessionedServlet.class);//.+_+ no obvious place for construct on first use.

  // +++ maybe create subfunctions that these methods call that then get overridden in subclasses ???

  // override to do stuff
  protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // do nothing.
    hackAttack(req);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // do nothing.
    hackAttack(req);
  }

  // (not really) anti-hacker devices:
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // do nothing.
    hackAttack(req);
  }

  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // do nothing.
    hackAttack(req);
  }

  protected void doPut(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // do nothing.
    hackAttack(req);
  }

  protected void doDelete(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    // do nothing.
    hackAttack(req);
  }

  protected final void hackAttack(HttpServletRequest req) {
    String msg = "Possible hack attack: " + req.getRemoteAddr();
    if(service != null) {
      service.PANIC(msg);
    } else {
      dbg.ERROR(msg);
    }
  }

  public static final UserSession getSession(HttpServletRequest req) {
    return SinetServer.THE().isUp() ? UserSession.extractUserSession(req) : null;
  }

  public static final boolean validSession(HttpServletRequest req) {
    /* +++ or if it has been inactive too long */
    return (req.getSession(true) != null) && req.isRequestedSessionIdValid();
  }

  // these are for logging bytes written and read; put them in the ServiceStub +++.
  public final Accumulator incoming = new Accumulator(); // approximate
  public final Accumulator outgoing = new Accumulator();
  public final Accumulator httptimer = new Accumulator(); // this is for timing txns
  public final Counter pending = new Counter(); // this is for counting them as they come in and leave
  public SessionedServletService service = null;
  public static SessionCleaner cleaner = null;
  protected static int TXNTHREADPRIORITY = Thread.NORM_PRIORITY+1;
  protected static int WEBTHREADPRIORITY = Thread.NORM_PRIORITY;

  /* package */ boolean down = false; // the service manipulates this

  /* package */ static ServiceConfigurator configger = null;

  // +++ going to have to create a public list of UserSessions if we want to
  // +++ keep track of people who login more than one at a time, so that we log
  // +++ them out of old sessions when they log into a new one. (security thing)

  // +++ do not override this!!!  Instead, override one or more of the do*()'s!
  protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
      pending.incr();
      // set the timeouts
      HttpSession session = req.getSession(true);
      if(service != null) {
        UserSession.setTerminatums(session, service.getTerminatums());
      }
      // in case it doesn't know yet, tell the cleaner about the session's context
      if((session != null) && (cleaner != null)) {
        cleaner.addContext(session.getSessionContext()); // +++ add check for return value?
      }
      // call the parent!
      incoming.add(req.getContentLength()); // accumulate approximate
      StopWatch sw = new StopWatch();
      super.service(req, resp);
      httptimer.add(sw.millis());
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      pending.decr();
      System.gc(); // cleanup now.
    }
  }

  // so that we don't accidentally create 2 "service"s
  private static final Monitor initMon = new Monitor("SessionedServletInit");
  private static final SystemStarter starter = new SystemStarter("SessionedServlet.SystemStarter");

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      initMon.getMonitor();
      starter.start(config, this);
    } catch (Throwable e) {
      dbg.Caught(e);
      log("SessionedServlet.init(): "+e); // +++ this log command may not do anything
    } finally {
      initMon.freeMonitor();
    }
  }

  public void destroy() {
    // +++ this guy has to notify the cleaner to shutdown ?
    // +++ and the cleaner has to kill all of the remaining sessions ?
    SessionCleaner localcleaner = cleaner;
    if(localcleaner != null) {
      localcleaner.down();
    }
//    Thread.yield();
    SinetServer.THE().shUtdOwn();
//    Thread.yield();
    LogFile.ExitAll();
//    Thread.yield();
    super.destroy();
  }

  protected static EasyProperties httpServletRequest2EasyProperties(HttpServletRequest req) {
    EasyProperties ezc = new EasyProperties();
    for(Enumeration ennum = req.getParameterNames(); ennum.hasMoreElements();) {
      String name = (String)ennum.nextElement();
      if(name != null) {
        String value = req.getParameter(name);
        ezc.setString(name, value);
      }
    }
    return ezc;
  }
}

// This is the background system starter that allows JServ to come up in the
// foreground and us to come up in the background (preventing jserv getting
// shutdown and restarted by apache if it takes too long to come up) and giving
// us those nice "down for system maintenenance" screens/replies
class SystemStarter implements Runnable {
  Thread thread = null;
  String name = "UNNAMED";
  EasyCursor props = null;
  ServletPrintStream sps = null;

  public SystemStarter(String name) {
    this.name = name;
  }

  public boolean started() {
    return started;
  }
  private boolean started = false;
  private SessionedServlet servlet = null;
  public synchronized void start(ServletConfig config, SessionedServlet servlet) {
    if(!started) {
      this.props = extractConfigs(config);
      this.sps = new ServletPrintStream(servlet);
      LogFile.defaultBackupStream = sps;
      this.servlet = servlet;
      thread = new Thread(this, name);
      // don't set the priority of this thread; it causes problems in threads that that thread creates!
      thread.start();
      started = true;
    }
  }

  public void run() {
    try {
      LogFile.defaultBackupStream = sps;
      servlet.TXNTHREADPRIORITY = props.getInt("TXNTHREADPRIORITY", servlet.TXNTHREADPRIORITY);
      servlet.WEBTHREADPRIORITY = props.getInt("WEBTHREADPRIORITY", servlet.WEBTHREADPRIORITY);
      SessionedServlet.configger = SinetServer.initialize(props, servlet.TXNTHREADPRIORITY, servlet.WEBTHREADPRIORITY);
      SessionedServlet.cleaner = new SessionCleaner(servlet.configger, props.getInt(SessionCleaner.INTERVALSECS));
    } catch (Exception ex) {
      sps.println("Exception initing UserSession!  VERY BAD!  " + ex);
      sps.println(ex);
      ex.printStackTrace(sps);
    }
  }

  private final EasyCursor extractConfigs(ServletConfig config) {
    EasyCursor cursor = null;
    try {
      cursor = new EasyCursor();
      if(config != null) {
        for(Enumeration ennum = config.getInitParameterNames(); ennum.hasMoreElements();) {
          String name = (String)ennum.nextElement();
          String value = (String)config.getInitParameter(name);
          cursor.setString(name, value);
        }
      }
    } catch (Exception e) {
      sps.println("SessionedServlet.extractConfigs: " + e);
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
