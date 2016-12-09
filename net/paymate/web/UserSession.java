/**
* Title:        UserSession<p>
* Description:  User session management object<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: UserSession.java,v 1.150 2001/11/17 20:06:37 mattm Exp $
*
* Notes:        In order to use the session, perform this at the beginning of the HttpServlet.doGet() (for example):
*               UserSession user = UserSession.extract
* req, true); // true means to force it to create one if one doesn't exist
*
*/

// ++++++++++++++++++++++++
// eventually break this into pieces.  The servlet-related stuff should go into a ServletEnvironment system,
// which will have parts merged with np.Main.java, or something similar.

package net.paymate.web;

import net.paymate.data.*;

import  net.paymate.web.page.*;
import  net.paymate.web.color.*;
import  net.paymate.web.table.*;
import  net.paymate.web.table.query.*;
import  net.paymate.connection.*;
import  net.paymate.authorizer.*;
import  net.paymate.authorizer.cardSystems.*;
import  net.paymate.util.*;
import  net.paymate.util.timer.*;
import  net.paymate.database.*;
import  net.paymate.database.ours.query.*;
import  net.paymate.terminalClient.Receipt;
import  net.paymate.ISO8583.data.TransactionID;
import  net.paymate.net.*;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  javax.servlet.http.*;
import  java.util.*;
import  java.io.*;
import  net.paymate.servlet.*;
// +++ later move the sql stuff elsewhere
import  java.sql.*;
// for receipt printing:
import  net.paymate.awtx.print.PNGModel;
import  net.paymate.jpos.Terminal.Scribe612;

public class UserSession implements HttpSessionBindingListener, AtExit {

  private static final ErrorLogStream dbg = new ErrorLogStream(UserSession.class.getName(), ErrorLogStream.WARNING);

  private static final String BRLF = "<BR>\n";

  private static String newsFile = null;

  private static String backupPath = "/data/backups"; // +++ get from configs
  private static final BackupAgent backupAgent = BackupAgent.New("UserSessionBackupAgent");
  private static final SendMail mailer = new SendMail();

  private static boolean shutdown = false; // +++ create a UserSessionList class to manage them, then create a single instance of it.
  public static final boolean shutdown() {
    return shutdown;
  }
  private static final SessionTermParams notime = new SessionTermParams(0, 0);
  public void AtExit() {
    if(!isaGod()) {
      // not a god, so kill it
      setTerminatums(getHttpSession(), notime); // give em nothing
    }
  }
  public static final void atExit(HttpSession session) {
    shutdown = true;
    // unbind all that aren't GODS
    HttpSessionContext context = session.getSessionContext();
    for(Enumeration enum = context.getIds(); enum.hasMoreElements(); ) {
      String id = (String)enum.nextElement();
      session = context.getSession(id);
      if(session != null) {
        UserSession user = extractUserSession(session);
        if(user != null) {
          user.AtExit();
        }
      }
    }
    statusClient.shutdown();
    backupAgent.shutdown();
  }
  public boolean IsDown() {
    return true;
  }
  public static final boolean isDown() {
    return shutdown; // +++ don't return true until the only sessions left are for gods !!!
  }

  public static String defaultColorSchemeID = null;

  private static boolean inited = false;
  public static final boolean inited() {
    return inited;
  }
  private static final Monitor myInit = new Monitor("UserSessionInit");
  private static final String sep = System.getProperty("file.separator");
  public static final void init(EasyCursor configs, PrintStream sps) {
    try {
      myInit.getMonitor();
      if(!inited) {
        // load configs from the servlets config file
        UserSession.grabbedParams = configs;
        String dblogin = configs.getString("dblogin", "mainsail");
        String dbpassword = configs.getString("dbpassword", "1strauss");
        String dbinformixAddress = configs.getString("informixAddress");
        String dbdriver = configs.getString("dbDriver");
        String logPath = configs.getString("logPath", LogFile.DEFAULTPATH);
        // initialize the logfile paths
        LogFile.setPath(logPath) ; // set the default Log Path before creating any logs
        // setup the log file for the system
        // even though ErrorLogStream handles it, UserSession will report on the logfile, since it started it.
        ErrorLogStream.stdLogging("sinet");
        ErrorLogStream.Console(LogLevelEnum.OFF);  // since we have no console to log to ...
        //ErrorLogStream.globalLeveller.setLevel(LogLevelEnum.VERBOSE);
        // validate the database
        dbg.ERROR(SessionedServlet.class.getName()+" validating database ...");
        dbConnInfo = new DBConnInfo(dbinformixAddress, dblogin, dbpassword, dbdriver);
        PayMateDB.init(dbConnInfo, sps); // db.init() will only actually work once ...
        // load the rest of the parameters from the database
        String servicename = "USERSESSION";
        PayMateDB db = new PayMateDB(dbConnInfo);
        EasyCursor dbcfgs = db.getServiceParams(servicename);
        hostname = dbcfgs.getString("computername", "").toLowerCase();
        backupPath = dbcfgs.getString("backupPath", backupPath);
        UserSession.newsFile = dbcfgs.getString("newsFile", sep + "data" + sep + "config" + sep + "news");
        // start the status client
        String macid = dbcfgs.getString("statusMacid", "unknownSrvr");
        String statusServer = dbcfgs.getString("statusServer", "64.92.151.10"/*monster*/);
        String statusNtrvlMs = dbcfgs.getString("statusIntervalMs", "" + StatusClient.POLLRATEDEFAULT);
        if(statusClient == null) {
          statusClient = new StatusClient(statusServer, "0.0.0.0", macid, Safe.parseLong(statusNtrvlMs));
        }
        // initialize the connectionServer
        connectionServer = new ConnectionServer();
        connectionServer.init(hostname, new PayMateDB(dbConnInfo), mailer, true /*preloadAllAuths; get from config file+++*/); // parameter
        // initialize various other misc items
        StatusPage.init(hostname);
        defaultColorSchemeID = dbcfgs.getString("defaultColorscheme", "TRANQUILITY");
        PayMatePage.serverDefaultColors = ColorScheme.schemeForName(defaultColorSchemeID);
        inited = true;
        dbg.ERROR("UserSession inited");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      myInit.freeMonitor();
    }
  }

  private static DBConnInfo dbConnInfo = null;
  public static ConnectionServer connectionServer = null;

  public static String hostname = "";

  public boolean loggedIn = false;
  public LoginInfo linfo = new LoginInfo(); // the default; to be replaced when the login occurs

  public PayMateDB db = null; // set in constructor

  /////////////////////////
  // session binding stuff

  private boolean bound = false;

  public void valueBound(HttpSessionBindingEvent event) {
    dbg.VERBOSE("BOUND as " + event.getName() + " to " + event.getSession().getId());
    bound = true;
  }

  public void valueUnbound(HttpSessionBindingEvent event) {
    dbg.VERBOSE("UNBOUND as " + event.getName() + " from " + event.getSession().getId());
    loggedIn = false;
    bound = false;
  }

  public boolean getBondage() {
    return bound;
  }

  String cachedSessionID = "";
  public String sessionid() {
    if(!Safe.NonTrivial(cachedSessionID) & (hSession != null)) {
      cachedSessionID = getHttpSession().getId();
    }
    return cachedSessionID;
  }

  private HttpSession hSession = null;
  public HttpSession getHttpSession() {
    return hSession;
  }

  public static final Monitor httpSessionMonitor = new Monitor("HttpSession");

  public static final void setTerminatums(HttpSession session, SessionTermParams newOnes) {
    try {
      httpSessionMonitor.getMonitor();
      // get or create the cleanup parameters
      SessionTermParams sessionTerms = (SessionTermParams)session.getValue(SessionTermParams.sessionTermParamsKey);
      if(sessionTerms == null) { // create one
        session.putValue(SessionTermParams.sessionTermParamsKey, new SessionTermParams(newOnes));
      } else { // modify one
        sessionTerms.mergeFrom(newOnes);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      UserSession.httpSessionMonitor.freeMonitor();
    }
  }

  public static final String userSessionKey = "UserSession";
  public void bind(HttpServletRequest req) {
    HttpSession session = req.getSession(true);
    try {
      httpSessionMonitor.getMonitor();
      // add the user login stuff to the list
      session.putValue(userSessionKey, this);
      this.hSession = session;
    } finally {
      httpSessionMonitor.freeMonitor();
    }
  }
  public void unbind() {
    // +++ maybe also log the logging in and out somewhere
    try {
      httpSessionMonitor.getMonitor();
      // +++ log the user out
      // then, kill the link
      hSession.removeValue(userSessionKey);
      hSession = null;
    } finally {
      httpSessionMonitor.freeMonitor();
    }
  }

  /////////////////////////////
  // session creation stuff

  public static final UserSession create(HttpServletRequest req) {
    UserSession user = null;
    try {
      httpSessionMonitor.getMonitor();
      // first check it one more time (we are multi-threaded, you know)
      user = extractUserSession(req.getSession(true));
      // should we be disconnecting it once we find it ??? ---
      if(user == null) {
        user = new UserSession(req); // +++ strip these from the request
      }
    } finally {
      httpSessionMonitor.freeMonitor();
      return user;
    }
  }

  public static final UserSession extractUserSession(HttpSession session) {
    UserSession user = null;
    try {
      httpSessionMonitor.getMonitor(); // --- is this necessary?
      if(session != null) {
        user = (UserSession)session.getValue(userSessionKey);
      }
    } catch (ClassCastException cce) {
      dbg.ERROR("ClassCastException caught while trying to extract the session!");
    } finally {
      httpSessionMonitor.freeMonitor();
      return user;
    }
  }

  public static final UserSession extractUserSession(HttpServletRequest req) {
    return (req != null) ? extractUserSession(req.getSession(true)) : null; // --- this could be an infinite loop!
  }

  public static final UserSession extractUserSession(HttpServletRequest req, boolean force) {
    UserSession user = UserSession.extractUserSession(req);
    if((user == null) && force && !shutdown) {
      dbg.VERBOSE("User does not exist; creating");
      user = UserSession.create(req);
    }
    return user;
  }

  /////////////////////
  // some misc stuff

  public boolean isNew() {
    return hSession.isNew();
  }

  //////////////////////
  // logins and logouts

  /**
  * Returns an ActionReplyStatus representing the login error.

  Login Algorithm:
  Is terminalID valid and enabled? (most stuff from here down can be done in one or two DB queries)
  NO  - "BadTerminal" reply
  YES - is terminal+user valid? (is user enabled, is store enabled, etc.)
  NO  - "invalid user"
  YES - is password correct?
  NO  - "invalid user"?
  YES - does user have permissions for transtype?
  NO  - "not permitted"

  */
  public ActionReplyStatus login(HttpServletRequest req, ActionRequest request) {
    return local_login(request, new ClerkIdInfo(req.getParameter(Login.USERID),
    req.getParameter(Login.PASSWORD)),
    Safe.parseInt(req.getParameter(Login.ENTID)),
    0);
  }

  public ActionReplyStatus login(ActionRequest request) {
    return local_login(request, request.clerk, 0, Safe.parseInt(request.terminalId));
  }

  Monitor sessionMon = new Monitor("UserSession");

  // either terminalID or enterpriseID must be present! (probably never will have both)
  private ActionReplyStatus local_login(ActionRequest request, ClerkIdInfo clerk, int enterpriseID, int terminalID) { // maybe shouldn't be synchronized (monitored)?
    loggedIn = false;
    // start with generic 'error' in case something blows
    ActionReplyStatus stat = new ActionReplyStatus(ActionReplyStatus.ServerError);
    try {
      sessionMon.getMonitor();
      sessionMon.name = "UserSession." + enterpriseID + "." + clerk.Name();
      int count = db.getLoginInfo(clerk, enterpriseID, terminalID, linfo);
      // count = 1: found
      //         0: none found
      //         2: too many; please specify your enterpriseid so that we may narrow it down
      if(count != 1) {
        stat.setto(ActionReplyStatus.InvalidTerminal);
      } else {
        if(!permissionsValid(linfo.enterpriseID, linfo.clerk, request, linfo.permissions)) {
          dbg.WARNING("Action type " + request.Type().Image() + " is not valid for user " + clerk.Name());
          stat.setto(Safe.NonTrivial(linfo.permissions)?ActionReplyStatus.InsufficientPriveleges:ActionReplyStatus.InvalidLogin);
        } else {
          // EVERYTHING IS GOOD!  YOU ARE LOGGED IN!
          dbg.VERBOSE("logged in userid=" + linfo.clerk.toSpam());
          loggedIn = true;
          linfo.colors = ColorScheme.schemeForName(linfo.colorschemeid);
          stat.setto(ActionReplyStatus.Success); // default for the case where nothing goes wrong
        }
      }
      if(loggedIn) {
        linfo.loginError = "logged in ";
      } else {
        switch(count) {
          case 0:
          case 1: {
            linfo.loginError = "FAILED LOGIN ATTEMPT!  Please try again. ";
          } break;
          default: {
            linfo.loginError = "Please enter your enterpriseID and try again.  Call our offices if you don't know your EnterpriseID: 512-418-0340.";
          } break;
        }
      }
      dbg.VERBOSE(linfo.loginError +
      linfo.clerk.toSpam() +
      ", termid=" + linfo.terminalID +
      ", permissions=" + linfo.permissions +
      ", ar.status=" + stat.Image());
      // returning an ActionReplyStatus makes it easy, but there is probably a better way.
      // like what about the loginError?
    } catch (Exception e2) {
      dbg.Caught(e2);
    } finally {
      sessionMon.freeMonitor();
    }
    return stat;
  }

  public static final boolean goodLogin(ActionReplyStatus stat) {
    return stat.is(ActionReplyStatus.Success);//+_+ allow fakers as well?
  }

  public boolean logout(HttpSession session) {
    try {
      sessionMon.getMonitor();
      loggedIn = false;
      // cleanup the UserSession
      unbind();
      // now, make this user NOT a god
      linfo = null;
      // and go ahead and clean up now (don't want to wait on the finalizer)
      TableGen.cleanup(sessionid());
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      sessionMon.freeMonitor();
    }
    return false;
  }

  public String loginInfo() {
    return loggedIn ? linfo.forDisplay() : null;
  }

  // +++ maybe break all of this security (user login, etc.) stuff out into extended class?
  // +++ we really should be using a 2-string-per-value enum to handle this,
  // +++ then the second enum (UserPermissions instead of just ActionType)
  // +++ would not be necessary

  // no point in recreating one of these every time we pass through this function
  // static ones take up more ram, but require less time to process (maybe)
  //+++ make TruEnum instantiate static members of each enum?
  private static final UserPermissions PwebAdmin = new UserPermissions(UserPermissions.E);
  private static final UserPermissions Preturn   = new UserPermissions(UserPermissions.R);
  private static final UserPermissions Pvoid     = new UserPermissions(UserPermissions.V);
  private static final UserPermissions Psale     = new UserPermissions(UserPermissions.S);
  //add one for drawer closing
  //add one for terminal re-entry sales (and MOTO?)
  private static final UserPermissions PdbAdmin  = new UserPermissions(UserPermissions.D);

  public boolean permissionsValid(int enterpriseID, ClerkIdInfo clerk, ActionRequest request, String permissions) {
    boolean finRet = ((request.isFinancial()) ? ((FinancialRequest)request).isReturn() : false);
    return permissionsValid(enterpriseID, clerk, request.Type(), finRet, permissions);
  }

  boolean permitted(String permissions,UserPermissions level){
    return permissions!=null&& permissions.indexOf(level.Image())>=0;
  }

  public boolean permissionsValid(int enterpriseID, ClerkIdInfo clerk, ActionType type, boolean isFinancialReturn, String permissions) {
    permissions = Safe.OnTrivial(permissions, " ");
    switch(type.Value()) {
      //internal admin requests, never initiatied by a human
      case ActionType.update:
      case ActionType.receiptStore:
      case ActionType.receiptGet://+_+ should have some privelege
      case ActionType.message:
      case ActionType.tolog:
      case ActionType.toclerk:
        case ActionType.toprinter:
        return true; //{}
        case ActionType.batch:
      case ActionType.clerkLogin: return permitted (permissions,Psale);
      // these next few are all extensions of the financial request, more or less
      case ActionType.check:
      case ActionType.credit:
      case ActionType.debit: return permitted(permissions, isFinancialReturn ? Preturn : Psale);
      case ActionType.adminWeb: return permitted(permissions,PwebAdmin);
      //      case ActionType.adminDB: return permitted(permissions,PdbAdmin);
      case ActionType.reversal: return permitted(permissions,Pvoid);
      //invalid actions, this function should have not been called for these
      case ActionType.card:
      case ActionType.financial:
      case ActionType.admin:
      case ActionType.unknown:
        default:
        return false; //{}// never let these pass (should never get here ???)
      }
  }

  ///////////////////////////////////////////
  // page generation code (move this ???)

  // +++ temporary; add a member for kinds of access (permissions), etc.
  private static final AdminOp NewsAdminOp        = new AdminOp("News"       ,new AdminOpCode(AdminOpCode.n));
  private static final AdminOp DrawersAdminOp     = new AdminOp("Drawers"    ,new AdminOpCode(AdminOpCode.c));
  private static final AdminOp SearchAdminOp      = new AdminOp("Search"     ,new AdminOpCode(AdminOpCode.f));
  private static final AdminOp AppliancesAdminOp  = new AdminOp("Appliances" ,new AdminOpCode(AdminOpCode.ap));
  private static final AdminOp StoresAdminOp      = new AdminOp("Stores"     ,new AdminOpCode(AdminOpCode.s));
  private static final AdminOp AssociatesAdminOp  = new AdminOp("Associates" ,new AdminOpCode(AdminOpCode.e));
  private static final AdminOp TerminalsAdminOp   = new AdminOp("Terminals"  ,new AdminOpCode(AdminOpCode.m));
  private static final AdminOp AdvertisingAdminOp = new AdminOp("Advertising",null/*new AdminOpCode(AdminOpCode.a)*/);
  private static final AdminOp ProfileAdminOp     = new AdminOp("Profile"    ,null/*new AdminOpCode(AdminOpCode.o)*/);
  private static final AdminOp PreferencesAdminOp = new AdminOp("Preferences",null/*new AdminOpCode(AdminOpCode.p)*/);
  private static final AdminOp DetailsAdminOp     = new AdminOp(""    ,new AdminOpCode(AdminOpCode.d)); // just used to get to the url
  private static final AdminOp ops[] = {
    TerminalsAdminOp,
    DrawersAdminOp,
    SearchAdminOp,
    StoresAdminOp,
    AssociatesAdminOp,
    AdvertisingAdminOp,
    ProfileAdminOp,
    PreferencesAdminOp,
    NewsAdminOp,
  };
  private static final AdminOp appspg = new AdminOp("Appliances",new AdminOpCode(AdminOpCode.ap));
  private static final AdminOp gw = new AdminOp("DATABASE",new AdminOpCode(AdminOpCode.g));
  private static final AdminOp debugpg = new AdminOp("STATUS",new AdminOpCode(AdminOpCode.b));
  private static final AdminOp threadspg = new AdminOp("THREADS",new AdminOpCode(AdminOpCode.bt));
  private static final AdminOp logpg = new AdminOp("LOGS",new AdminOpCode(AdminOpCode.bl));
  private static final AdminOp backuppg = new AdminOp("BACKUPS",new AdminOpCode(AdminOpCode.bu));
  private static final AdminOp paramspg = new AdminOp("PARAMS",new AdminOpCode(AdminOpCode.bp));
  private static final AdminOp gawdops[] = {
    appspg,
    debugpg,
    gw,
    backuppg,
    threadspg,
    logpg,
    paramspg,
  };
  private static final Element [] forDbPage = {
    new Input(Input.HIDDEN, AdminOp.adminPrefix, gw.codeImage())
  };

  void addViewTranScript(ElementContainer ec){
    /*pmp.getHead()*/
    ec.addElement(new Script().setLanguage("javascript").addElement(
    "function viewtransaction(txnid, label) {\n"+
      "winStats = 'toolbar=1,location=1,directories=1,status=1,menubar=1';"+
      "winStats+= ',scrollbars=1,hotkeys=1,resizable=1';"+//,width=700,height=400,left=10,top=25';"+
      (isaGod()?"":"winStats+= ',width=750,height=450';")+
      "  urlString = \""/*+"./"*/+Acct.key()+"?"+DetailsAdminOp.url()+"&tid=\" + txnid + \"&" + UnsettledTransactionFormat.NOVOIDMARKER + "=1#title\";\n"+
      "  window.open(urlString ,\"Transaction_\"+label,winStats);\n"+
    "}\n"
    ));
  }

  private TR makeMenu(AdminOp menuops[]) {
    TR tr = new TR();
    for(int imenu = 0; imenu < menuops.length; imenu++) {
      String url = menuops[imenu].url();
      if(Safe.NonTrivial(url)) {
        tr.addElement(new TD(new Center(new A(Acct.key()+"?"+menuops[imenu].url(), menuops[imenu].name())))); // +++ need to say to use this page!
      } else {
        tr.addElement(new TD(new Center(menuops[imenu].name()))); // +++ need to say to use this page!
      }
      tr.addElement(PayMatePage.LF);
    }
    return tr;
  }

  public final String replyTo(HttpServletRequest req, String theObject, EasyCursor props) {
    // +++ check for down?
    return connectionServer.ReplyTo(req, theObject, this, props);
  }

  // this is synchronized so that the user only gets one page generated at a time (for our programming ease)
  public PayMatePage generatePage(HttpServletRequest req) {
    String loginInfo = loginInfo();
    PayMatePage pmp = new Acct(loginInfo);
    try {
      sessionMon.getMonitor();
      String query = Safe.TrivialDefault(req.getQueryString(), "");
      ElementContainer ec = new ElementContainer();
      pmp.colors = linfo.colors;
      // barf the parameters you got
      dbg.VERBOSE("Query: " + query);
      // +++ the processing...
      // +++ split out the parameters!
      String adminOpStr = Safe.TrivialDefault(req.getParameter(AdminOp.adminPrefix), "");

      AdminOpCode op = new AdminOpCode(adminOpStr);

      // here, we need a menu
      TR tr = makeMenu(ops);
      Table t1 = new Table();
      t1.setBgColor(linfo.colors.MEDIUM.BG)
      .setBorder(0)
      .setWidth(PayMatePage.PERCENT100)
      .setCellSpacing(0)
      .setCellPadding(0)
      .addElement(tr);
      ec.addElement(t1)
      .addElement(PayMatePage.BRLF);
      if(isaGod()) {
        tr = makeMenu(gawdops);
        t1 = new Table();
        t1.setBgColor(linfo.colors.MEDIUM.BG)
        .setBorder(0)
        .setWidth(PayMatePage.PERCENT100)
        .setCellSpacing(0)
        .setCellPadding(0)
        .addElement(tr);
        ec.addElement(t1)
        .addElement(PayMatePage.BRLF);
      }
      // if it was log in ...
      if(!Safe.NonTrivial(query)) {
        dbg.VERBOSE("Login: " + loginInfo()/*linfo.clerk.Name() + ":" + linfo.longName + " of " + linfo.companyName*/);
//        ec.addElement("Welcome, " + loginInfo() /*linfo.longName + " of " + linfo.companyName + " on terminal " + linfo.terminalName*/ + " !")
//        .addElement(PayMatePage.BRLF);/*
//        .addElement("[news or messages go here]");*/ // +++ put news or messages here (like network status info, eg).
      }

      String prefix = "This is where the manager will ";

      // if it is a god, and the god didn't specify, give them the status page
      if(!op.isLegal() && isaGod()) {
        op = new AdminOpCode(AdminOpCode.b);
      }

//      if(want to see debug) {
      String paramnames = "";
      for(Enumeration enum = req.getParameterNames(); enum.hasMoreElements();) {
        String paramname = (String)enum.nextElement();
        if(paramname != null) {
          paramnames += paramname + "=";
          String [] values = req.getParameterValues(paramname);
          for(int pvi = values.length; pvi-->0;) {
            paramnames += values[pvi]+",";
          }
          paramnames += ";";
        }
      }
      dbg.ERROR("Request parameters: " + paramnames);
//      }

      switch(op.Value()) {
        case AdminOpCode.gc: {
          if(isaGod()) {
            System.gc();
          }
        } break;
        case AdminOpCode.bu: { // backup the system to disk, so that we can then archive it all.
          // unloads the database to file
          if(isaGod()) { // only database administrators have this pOwEr
            String title = hostname + " Backups";
            ec.addElement(new Center(new H2(title)))
              .addElement(PayMatePage.BRLF).addElement(new HR());
            if(Safe.equalStrings("1", req.getParameter("clear"))) {
              ec.addElement(new Center().addElement(new H3(backupAgent.Clear() ? "History cleared." : "History clear attempted, but had an error (busy).")))
                .addElement(BRLF);
            }
            if(Safe.equalStrings("1", req.getParameter("run"))) {
              // wait 30 ms between rows.  Will take a long time, but will probably not interrupt the system too much
              // drop our thread to a lower priority instead? ---
              BackupJob [] job = backupAgent.backup(db, backupPath, Thread.currentThread().getPriority()-1, /*30*/0);
              ec.addElement(new Center().addElement(new H3((job.length > 0) ? "" + job.length + " backups in progress.<BR>Click on the BACKUP link [not button] for status." : "Unable to enqueue backups.")))
                .addElement(BRLF);
            }
            TR tr2 = new TR();
            Table t = new Table().setCellSpacing(4).setCellPadding(0).setBorder(0).addElement(tr2);
            Form f = new Form(req.getServletPath() + "/" + Acct.key() + "?" + backuppg.url(), Form.POST)
              .addElement(new Input().setType(Input.SUBMIT).setValue("Backup All Now"))
              .addElement(new Input(Input.HIDDEN, "run", "1")).addElement(BRLF);
            tr2.addElement(new TD().addElement(f));
            f = new Form(req.getServletPath() + "/" + Acct.key() + "?" + backuppg.url(), Form.POST)
              .addElement(new Input().setType(Input.SUBMIT).setValue("Clear History"))
              .addElement(new Input(Input.HIDDEN, "clear", "1")).addElement(BRLF);
            tr2.addElement(new TD().addElement(f));
            ec.addElement(BRLF)
              .addElement(BRLF)
              .addElement(t)
              .addElement(BRLF);
            ec.addElement(new BackupJobFormat(linfo, backupAgent, "Backups"));
          } else {
            ec.addElement(News.contents(getNews(newsFile)));
          }
        } break;
        case AdminOpCode.ss: { // store stats (in progress)
          // code it here for now, and make it into a class later +++
          int colCount = 40; //20; // how many buckets to use
          // +++ divide the max approvedtxns() / #buckets.  Round up to the nearest integer * 10^n (5, 20, 400, etc)
          // +++ then use the result of that calculation as your item width below (instead of hardcoding 5)
/*
select Max(transactionamount)/40 as max
from tranjour
where storeid = '000000001004001'
and ( (ACTIONCODE='A')
       or
      (standin ....
     )
and not MESSAGETYPE='0400'
and not voidtransaction='Y'
==> 4.325
Math.ceil(max) = 5.0
+++ Now, do ALL of the buckets (outer join?)!
*/
          // headers
          HeaderDef [] headers = {
            new HeaderDef(AlignType.RIGHT, "Amount"),
            new HeaderDef(AlignType.RIGHT, "Count"),
            new HeaderDef(AlignType.LEFT,  "--->"),
          };
          // --- cheap method, but I am in a hurry
          TextList amounts = new TextList();
          TextList counts = new TextList();
          long maxCounts = 0;
          Statement stmt = db.query(QueryString.Select("").
          append(" round(transactionamount/5)*5 as Amount ").
          comma(" count(transactionamount) as Count ").
          from("tranjour").
          where().nvPairQuoted("storeid", req.getParameter(StoreFormat.CAID)).
          and().append(PayMateDB.ApprovedTxns()).
          groupby("1").
          orderbyasc("1"));
          if(stmt != null) {
            try {
              ResultSet rs = db.getResultSet(stmt);
              while(db.next(rs)) {
                amounts.add(db.getStringFromRS("Amount", rs));
                String count = db.getStringFromRS("Count", rs);
                counts.add(count);
                maxCounts = Math.max(maxCounts, Safe.parseLong(count));
              }
            } catch (Exception e) {
              dbg.Caught(e);
            }
            db.closeStmt(stmt);
            double divisor = maxCounts / (colCount * 1.0);
            String [][] data = new String[amounts.size()][3];
            for(int i = 0; i < amounts.size(); i++) {
              String countStr = counts.itemAt(i);
              long count = Safe.parseLong(countStr);
              long columns = (long)Math.ceil(count / divisor);
              data[i][0] = amounts.itemAt(i);
              data[i][1] = countStr;
              data[i][2] = Safe.fill("", '-', (int)columns, true);
            }
            ec.addElement(ArrayTableGen.output("Store Statistics / $5", linfo.colors, data, headers, sessionid()));
          } else {
            ec.addElement("Error stats running query.");
          }
        } break;
        case AdminOpCode.shUtdOwn: { // shutdown the system !!!
          if(isaGod()) { // only database administrators have this pOwEr
            shUtdOwn(req.getSession(true));
            ec.addElement(statusPage(req.getSession(true)));
          }
        } break;
        case AdminOpCode.cArdsYstEms: { // disconnect the SPP->CS socket
          // +++ convert this to use links on the page and authid's for references
          if(isaGod()) { // only database administrators have this pOwEr
            //long waitMillis = Ticks.forSeconds(Long.parseLong(Safe.TrivialDefault(req.getParameter("duration"), "0")));
            boolean up = (req.getParameter("up") != null);
            ec.addElement(new StringElement("CardSystems authorizer " +(
                (up ?
                  connectionServer.authmgr.findAuthByName("MAVERICK").bringup() :
                  connectionServer.authmgr.findAuthByName("MAVERICK").shutdown())
              ? "UP" : "DOWN") + "!")); // try to get the secure connection to card systems to restart
            ec.addElement(statusPage(req.getSession(true)));
          }
        } break;
        case AdminOpCode.b: { //status
          if(isaGod()) { // only database administrators have this pOwEr
            ec.addElement(statusPage(req.getSession(true)));
          }
        } break;
        case AdminOpCode.bl: { //debug logs & settings
          // add the ability to change them !!!
          if(isaGod()) { // only database administrators have this pOwEr
            // setto
            // first, check to see if we are supposed to change a loglevel
            String set = req.getParameter("set");
            String to  = req.getParameter("to");
            if(Safe.NonTrivial(set) && Safe.NonTrivial(to)) {
              String message = "";
              // which one, and to what level?
              if(Safe.equalStrings(set, "ALL")) {
                LogSwitch.SetAll(to);
                message = "ALL LogSwitches set to " + to;
              } else {
                LogSwitch ls = LogSwitch.find(set);
                message = (ls == null) ? "Log switch not found: " + set : ls.Name() + " set to " + ls.setLevel(to).Level();
              }
              ec.addElement(new Center().addElement(new H3(message))).addElement(BRLF);
            }
            TR tr2 = new TR();
            Form f = new Form(req.getServletPath() + "/" + Acct.key() + "?" + logpg.url(), Form.POST);
            f.addElement(new Table().setCellSpacing(4).setCellPadding(0).setBorder(0).addElement(tr2));
            Vector debuggers = LogSwitch.Sorted();
            Option [] logswitches = new Option[debuggers.size()+1];
            logswitches[0] = new Option("ALL").addElement("ALL");
            for(int i = 0; i < debuggers.size(); i++) {
              LogSwitch ls = (LogSwitch)debuggers.elementAt(i);
              String toset = (ls != null) ? ls.Name() : "[not found]";
              logswitches[i+1] = new Option(toset).addElement(toset);
            }
            LogLevelEnum llenum = new LogLevelEnum();
            Option [] loglevels = new Option[llenum.numValues()];
            for(int i = 0; i < llenum.numValues(); i++) {
              llenum.setto(i);
              loglevels[i] = new Option(llenum.Image()).addElement(llenum.Image());
            }
            tr2.addElement(new TD().addElement("Debug or Fork: ").addElement(new Select("set", logswitches)));
            tr2.addElement(new TD().addElement("Log Level: ").addElement(new Select("to", loglevels)));
            tr2.addElement(new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Change")));
            String title = hostname + " Logs";
            LogSwitchTableGen.output(title, linfo.colors);  // +++ so that it makes checkboxes and inserts code for it.
            ec.addElement(new Center(new H2(title)))
              .addElement(PayMatePage.BRLF).addElement(new HR())
              .addElement(new LogFileFormat(linfo.colors, "Log Files"))
              .addElement(PayMatePage.BRLF).addElement(new HR())
                      .addElement(BRLF)
                      .addElement(BRLF)
                      .addElement(f)
                      .addElement(BRLF)
              .addElement(LogSwitchTableGen.output("Log Levels", linfo.colors))  // again to make it all work (--- I forgot why, but I think it has to do with the Log stuff)
              .addElement(new HR());
          }
        } break;
        case AdminOpCode.bp: { //parameters
          if(isaGod()) { // only database administrators have this pOwEr
            ec.addElement(StatusPage.printParams(linfo.colors, sessionid(), db));
          }
        } break;
        case AdminOpCode.bt: { // threads
          if(isaGod()) { // only database administrators have this pOwEr
            ec.addElement(StatusPage.printThreads(linfo.colors, sessionid()));
          }
        } break;
        case AdminOpCode.g: { //database admin
          if(isaGod()) { // only database administrators have this pOwEr
            ec.addElement(DBPage.generatePage(req, this, "/" + Acct.key() + "?" + gw.url(), linfo.colors, forDbPage));
          }
        } break;
        case AdminOpCode.f: { //Txn Search (find)
          TranjourFilter tf=new TranjourFilter();
          tf.card=new CardRange(req.getParameter(SearchPage.card1tag),req.getParameter(SearchPage.card2tag));
          //String store = req.getParameter(SearchPage.storeTag);
          tf.amount=new MoneyRange(req.getParameter(SearchPage.amount1tag),req.getParameter(SearchPage.amount2tag));
          tf.stan =new StanRange(req.getParameter(SearchPage.stan1tag),req.getParameter(SearchPage.stan2tag));
          tf.appr =new AuthRange(req.getParameter(SearchPage.appr1tag),req.getParameter(SearchPage.appr2tag));
          // +++ possibly optimize
          String date1 = formatDate(
          req.getParameter(SearchPage.date1year),
          req.getParameter(SearchPage.date1month),
          req.getParameter(SearchPage.date1day),
          req.getParameter(SearchPage.date1hour),
          req.getParameter(SearchPage.date1minute), false);
          String date2 = formatDate(
          req.getParameter(SearchPage.date2year),
          req.getParameter(SearchPage.date2month),
          req.getParameter(SearchPage.date2day),
          req.getParameter(SearchPage.date2hour),
          req.getParameter(SearchPage.date2minute), true);
          LocalTimeFormat ltf = LocalTimeFormat.New(linfo.ltf.getZone(), "yyyyMMddHHmmss" /* this is what we set it to in formatDate(), below */);
          tf.time = db.TimeRange();
          if(Safe.NonTrivial(date1)) {
            tf.time.include(ltf.parse("20"+date1));
            if(!Safe.NonTrivial(date2)) {
              date2 = date1.substring(0,6) + "235959"; // quick and dirty
            }
          }
          if(Safe.NonTrivial(date2)) {
            tf.time.include(ltf.parse("20"+date2));
          }
          // now that we have built the filter, run the query
          TableGen utf = null;
          String key = req.getParameter("k");
          ec.addElement(PayMatePage.BRLF)
          .addElement(new Center(new Font().setSize("+2").addElement("Search")))
          .addElement(PayMatePage.BRLF);
          if( !Safe.NonTrivial(key) && !tf.NonTrivial()) {
            // if no parameters passed, give the parameters page
            // +++ and print the instructions, too
            ec.addElement(SearchPage.defaultPage(null/* for now --- */, Acct.key() + "?" + SearchAdminOp.url()));
          } else {
            if(Safe.NonTrivial(key)) {
              // retreived an existing search, so use it
              utf = TableGen.getFromKey(key);
            }
            TxnRow stmt = null;
            if(utf == null) {
              // if parameters passed, do the search
              try {
                stmt = db.findTransactionBy(linfo.enterpriseID, tf);
                utf = new UnsettledTransactionFormat(linfo, stmt, false, "Search Results" /* +++ add more text to say what the search was for */, leftRightNextTable(SearchAdminOp.url()), 15, sessionid());
              } catch (Exception t) {
                dbg.Caught("generatePage: Exception performing pending transaction query!",t);
              }
            }
            if(utf != null) {
              addViewTranScript(ec);
              // return the results as a table
              ec.addElement(utf);
            }
          }
        } break;
        case AdminOpCode.c: { // drawer closings list
          TableGen utf = null;
          String key = req.getParameter("k");
          if(Safe.NonTrivial(key)) {
            utf = TableGen.getFromKey(key);
          }
          if(utf == null) {
            utf = new DrawerClosingFormat(linfo, DrawerRow.NewSet(db.runEnterpriseDrawerQuery(linfo.enterpriseID)), "Drawer Closings", leftRightNextTable(DrawersAdminOp.url()), 15, sessionid());
          }
          ec.addElement(utf);
        } break;
        case AdminOpCode.c1: { // display a single drawer closing
          // +++ give the title something meaningful?
          TableGen utf = null;
          TableGen utf2 = null;
          int bmid = Safe.parseInt(req.getParameter(DrawerClosingFormat.BMID));
          // +++ How do we check to see that we have access to this bookmark?  Using this method, other people can access this drawer closing, can't they?
          if(bmid!=0) {
            TxnRow stmt = db.getDrawerClosingCursor(bmid, linfo.ltf);
            utf = new UnsettledTransactionFormat(linfo, stmt, false, stmt.title(), null, -1, null);
            utf2 = new CardSubtotalsFormat(linfo, db.getDrawerCardSubtotals(bmid), "Subtotals by Card Type");
          }
          if(utf != null) {
            addViewTranScript(ec);
            ec.addElement(utf);
            ec.addElement(PayMatePage.BRLF);
            ec.addElement(utf2);
          } else {
            dbg.ERROR("Bookmark is trivial.");
            ec.addElement("Unable to locate bookmark.  Please notify webmaster.");
          }
        } break;
        case AdminOpCode.s: { //Stores
          ec.addElement(new StoreFormat(linfo, db.storesInfoQuery(linfo.enterpriseID), "Stores", null, -1, null));
        } break;
        case AdminOpCode.e: { //Associates
          //+++ enterprise-level can add/edit/delete; store-level can choose (maybe).
          ec.addElement(new AssociatesFormat(linfo, db.associateQuery(linfo.enterpriseID), "Associates", null, -1, null));
        } break;
        case AdminOpCode.ap: { //Appliances
          //+++ for gawd-level users only
          ec.addElement(new AppliancesFormat(linfo, db.getApplianceRowQuery(), "Appliances", db));
        } break;
        case AdminOpCode.a: { //Advertising
          //+++ what need for this?
        } break;
        case AdminOpCode.o: { //Profile
          //+++ what need for this?
        } break;
        case AdminOpCode.p: { //Preferences
          //+++ need enterprise-level for this?
          //+++ need enterpriseid for this
        } break;
        case AdminOpCode.d: {
          // +++ turn this into a formal PayMatePage !!MUST  ADD BUTTON TO PRINT CLEANLY if you do(alh)
          // +++ add amount change feature? (void or refund the txn.  if that works, do a new one?  ask how to do this)
          // here, a transaction id is passed in so that the details can be displayed
          // then, a full report of the transaction is displayed,
          // including a link to the voiding or voided transaction, if appropriate
          // and the receipt is displayed, or a link to it is, along with a reprint option.
          String txnid = req.getParameter("tid");
          if(txnid == null) {
            ec.addElement("<BR><CENTER><B>Error in page format.  No transactions selected.</B></CENTER><BR>");
          } else {
            boolean voided = false;
            String voidpw = req.getParameter("vpw");
            boolean voidable = (req.getParameter(UnsettledTransactionFormat.NOVOIDMARKER) != null); // +++ until we have a better way to do this
            if(voidpw != null) {
              // this means they wanted to void the txn!
              if(linfo.clerk.Password().equals(voidpw)) {
                // try to void the record
                TransactionID transid = TransactionID.New(txnid);
                ReversalRequest request = new ReversalRequest(transid); // DO MORE HERE ???
                request.terminalId = ""+linfo.terminalID;
                ActionReply reply = connectionServer.generateReply(request, this, false, null, null);//web voids
                voided = reply.Response.isApproved();
                if(!voided) {
                  ec.addElement("<BR><CENTER><B>Unable to void transaction [" + reply.Response + ": " + reply.Errors.asParagraph() + "].</B></CENTER><BR>");
                } else {
                  ec.addElement("<BR><CENTER><B>Transaction " + transid.stan() + " voided.</B></CENTER><BR>");
                }
              } else {
                ec.addElement("<BR><CENTER><B>Unable to void transaction due to incorrect password.</B></CENTER><BR>");
                // log as a security "exception"
                dbg.ERROR("Txn void attempt via web by user " + linfo.clerk.toSpam() + " unsuccessful to do incorrect password " + voidpw);
              }
            }
            // output the page with the transaction info on it
            TransactionID tid = TransactionID.New(txnid); // parses for us
            TxnRow rec = db.getTranjourRecordfromTID(tid, null/*linfo.terminalID*/);
            ec.addElement(PayMatePage.BRLF)
            .addElement(new Center(new Font().setSize("+2").addElement("<a name=\"title\"/>Transaction #" + rec.stan + " Details")))
            .addElement(PayMatePage.BRLF);
            if(rec == null) {
              ec.addElement("<BR><CENTER><B>Error locating transaction # " + tid.stan() + ".</B></CENTER><BR>");
            } else {
              String rcptFilename = db.getReceipt(tid, linfo.terminalID);
              //+++ can we get standin info?
              int stanvalue= rec.siStan();
              if(stanvalue>0){//was a standin, 0 is not a legal stan
                if(!Safe.NonTrivial(rcptFilename)){//try to find an orphaned receipt
                  rcptFilename = connectionServer.getOrphanReceipt(db, rec);
                  dbg.WARNING("Looking for orphaned receipt:"+rcptFilename+ " for:"+tid.image());
                }
                //other si related flash goes here.
              }
              EasyCursor ezp = rec.toProperties();
              HeaderDef headers[] = null; // fix !!! +++
              Element tgr = EasyCursorTableGen.output("Transaction Detail for # " +
              tid.stan() + " [" + tid.image() +"]:",
              linfo.colors, ezp, headers, sessionid());
              ec.addElement(PayMatePage.BRLF)
              .addElement((rcptFilename != null) ?
              (Element) new IMG(ReceiptRequestor + "?" + TransID + "=" + tid.image()).setAlt("Receipt Image:"+rcptFilename) :
              (Element) new StringElement("No receipt stored."))
              .addElement(PayMatePage.BRLF);
              // don't give a void option if it is already voided
              // +++ eventually, add a REDO option when not voidable so that txn can be redone.
              // +++ if the record is not pending, do a refund instead?  (otherwise their old drawer will get changed!)
              if(!"Y".equalsIgnoreCase(rec.voidtransaction) && voidable) {
                TD td1 = new TD().addElement("Enter Password to void: ");
                TD td2 = new TD().addElement(new Input(Input.PASSWORD, "vpw", ""));
                TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("VOID Txn"));
                TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
                Table t = new Table().addElement(tr1);
                Form f = new Form("./"+Acct.key()+"?"+DetailsAdminOp.url(), Form.POST).
                setName("form_void").
                addElement(new Input(Input.HIDDEN, "tid", txnid)).
                addElement(t);
                ec.addElement(f);
              }
              if(isaGod()) {
                ec.addElement(tgr) // +++ add more to this?
                .addElement(PayMatePage.BRLF);
              }
            }
          }
        } break;
        default:
        case AdminOpCode.n: { // if they didn't specify ... home/root/news
          ec.addElement(News.contents(getNews(newsFile)));
        } break;
        case AdminOpCode.m: { //Terminals
          // will need to use different queries to come up with a list of stores that we can use here
          // that list depends on the priveleges of the user logged in +++
          ec.addElement(new TerminalsFormat(linfo, db.getTerminalsForStore(linfo.storeid),
            "Terminals", null, -1 /* No pagination! */, null, db));
        } break;
        case AdminOpCode.t: { // Pending Txns
          String terminalidfullname = req.getParameter("t");
          dbg.WARNING("pending pages for terminal t="+terminalidfullname);
          TerminalID termid = new TerminalID(terminalidfullname);
          dbg.WARNING("terminal = "+termid.fullname());
          addViewTranScript(ec); // add the javascript to handle displaying a single txn's details
          // get the terminalid's for all of the terminals for this store:
          TerminalPendingRow tpr = db.getTerminalPendingRow(termid.terminalID);
          java.util.Date starttime = db.getPendingStartTime(termid); // get this separately?
          String title = "Pending Transactions for terminal " + tpr.enterpriseName+"/"+tpr.storeName+"/"+tpr.terminalName + " from " + linfo.ltf.format(starttime) + " to "+linfo.ltf.format(Safe.Now());
          TxnRow trow = db.unsettledTxnsQuery(tpr.terminalName, starttime, Safe.parseInt(tpr.storeid));
          Element el = new UnsettledTransactionFormat(linfo, trow, true, title, null, -1 /* No pagination! */, null);
          ec.addElement(el);
          ec.addElement(PayMatePage.BRLF);
          CardSubtotalsFormat utf2 = new CardSubtotalsFormat(linfo, db.getUnsettledCardSubtotals(Safe.parseInt(tpr.storeid), tpr.terminalName, starttime), "Subtotals by Card Type");
          ec.addElement(utf2);
        } break;
        case AdminOpCode.duptemp: { // take this out later ---
          /* eg: https://64.92.151.4:8443/servlets/admin/Acct?adm=duptemp&since=20010909 */
          ec.addElement(AnyDBTableGen.output("Possible Duplicates", linfo.colors, db.getResultSet(db.getPossibleMSDups(req.getParameter("since")+"000000")) , null, null));
        } break;
      }
      pmp.fillBody(ec);
      // send the page back
    } catch (Exception emon2) {
      dbg.Caught(emon2);
    } finally {
      sessionMon.freeMonitor();
    }
    return pmp;
  }

  ////////////////////////
  // the doings

  public boolean isaGod() {
    return (linfo != null) && linfo.permits(PdbAdmin);
  }

  public static final void shUtdOwn(HttpSession session) {
    // +++ put all this stuff on another thread, and immediately return a page saying that we are doing it !!!
    // first, tell the UserSessions
    if(session != null) {
      atExit(session); // first, handle incoming stuff for this class (don't make any new connections & disconnect the ones that are not GODS
      ThreadX.sleepFor(100);
    }
    // then the authorizers
    connectionServer.authmgr.shutdown();
    ThreadX.sleepFor(3000); // gimme a few secs to do stuff, then ...
  }

  public static final StopWatch uptime= new StopWatch();

  private Element statusPage(HttpSession session) {
    return StatusPage.printStatus(session, linfo.ltf.getZone(), linfo.colors, sessionid(), connectionServer, backupAgent, mailer, statusClient);
  }

  ////////////////////////
  // constructor (private)

  private UserSession(HttpServletRequest req) {
    db = new PayMateDB(dbConnInfo);
    dbg.VERBOSE("about to bind ...");
    bind(req);
    dbg.VERBOSE("... bound.");
  }

  ///////////////////////
  // other (just receipt right now)

  public static final String ReceiptRequestor = "rcpt";
  public static final String TransID          = "tid";
  // eventually make this use the security, too
  public boolean printReceipt(HttpServletRequest req, OutputStream os) {
    String query = Safe.TrivialDefault(req.getQueryString(), "");
    // barf the parameters you got
    dbg.VERBOSE("Query: " + query);
    String transIdStr = Safe.TrivialDefault(req.getParameter(TransID), "");
    // first, query for the Receipt
    TransactionID tid = TransactionID.New(transIdStr);
    ReceiptGetRequest request = new ReceiptGetRequest(tid);
    ActionReply ar = connectionServer.generateReply(request, this, false, null, null);//anyone or anything can get a receipt
    Receipt rcpt = null;
    if((ar != null) && (ar instanceof ReceiptGetReply) && ar.Succeeded()) {
      rcpt = ((ReceiptGetReply)ar).receipt();
    }
    TextList someStrings = new TextList();
    if(rcpt == null) {
      someStrings.add("Receipt not found.");
      someStrings.add("Please notify PayMate.net staff.");
    }
    // create the formatter
    PNGModel png = null;
    boolean sig = (rcpt != null);//means "show signature"
    Receipt.setShowSignature(sig);
    try {
//  public PNGModel(PrinterModel lp, OutputStream os, int linesOfText, boolean signature) {
      net.paymate.awtx.print.PrinterModel lp = new Scribe612(null);
      int linesOfText = (sig) ? rcpt.totalLines() : someStrings.size();
      png = new PNGModel(lp, os, linesOfText, sig);
    } catch (Exception t) {
      dbg.Caught("No AWT available?", t);
    }
    if(png == null) {
      dbg.ERROR("RED ALERT!  Unable to generate image!  Can't create PNGModel!");
      return false;
    } else {
      if(sig) {
        // print it to the output stream
        rcpt.print(png,0);//always ask for prime receipt when printed from web, never "duplicate"
        dbg.WARNING("Printed receipt.  was: " + rcpt.toTransport());
      } else {
        png.print(someStrings);
        png.formfeed(); // REQUIRED !!!!
        dbg.WARNING("Receipt not found: " + transIdStr);
      }
      return true;
    }
  }

  private String leftRightNextTable(String url) {
    return Acct.key() + "?" + url;
  }

  public void finalize() {
    TableGen.cleanup(sessionid());
  }

  private static final String formatDate(String year, String month, String day, String hour, String minute, boolean wholeDay) {
    if(!Safe.NonTrivial(year) && !Safe.NonTrivial(month) && !Safe.NonTrivial(day) && !Safe.NonTrivial(hour) && !Safe.NonTrivial(minute)) {
      return null;
    }
    String second = "00";
    // if the wholeDay is desired, and they didn't put a time, but they did put a date, make it use 23:59:59 !!!!!
    if(wholeDay && !Safe.NonTrivial(hour) && !Safe.NonTrivial(minute) && (Safe.NonTrivial(year) || Safe.NonTrivial(month) || Safe.NonTrivial(day))) {
      hour = "23";
      minute = "59";
      second = "59";
    }
    String date =
    Safe.twoDigitFixed(year)+
    Safe.twoDigitFixed(month)+
    Safe.twoDigitFixed(day)+
    Safe.twoDigitFixed(hour)+
    Safe.twoDigitFixed(minute)+
    Safe.twoDigitFixed(second);
    if(Safe.parseLong(date) == 0) {
      date = null;
    }
    return date;
  }

  public static net.paymate.net.StatusClient statusClient = null;

  public static EasyCursor grabbedParams = new EasyCursor();

  /**
   * Eventually, manage the file from data entered over the web,
   * then just load it when the program loads,
   * and write it when it is changed over the web.
   *
   * +++ create a util function for loading a file from disk into a string.  use it here.
   */
  protected static String getNews(String filename) {
    // get from somewhere external to the jar (so it can be updated without recompiling) !!!
    String ret = "";
    // first, get the filename from the configuration, with a default of /data/config/news
    try {
      if(Safe.NonTrivial(filename)) {
        File file = new File(filename);
        if(file != null) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          Streamer.swapStreams(new FileInputStream(file), baos); // +++ check return value
          ret = baos.toString(); // +++ will this all work correctly?
        } else {
          dbg.WARNING("Unable to locate news file on disk.");
        }
      } else {
        dbg.WARNING("Unable to locate news file on disk.");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    }
    return ret;
  }

}
//$Id: UserSession.java,v 1.150 2001/11/17 20:06:37 mattm Exp $
