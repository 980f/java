package net.paymate.web;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/SinetServer.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.21 $
 */

import net.paymate.util.*;
import net.paymate.net.*;
import net.paymate.database.*;
import net.paymate.connection.*;
import net.paymate.servlet.*;
import net.paymate.web.page.*;
import net.paymate.web.color.*;
import java.io.*;
import net.paymate.lang.ThreadX;
import net.paymate.io.LogFile;
//import java.lang.reflect.Method;
import net.paymate.lang.StringX;
// SS2
import net.paymate.data.sinet.business.*;
import net.paymate.data.sinet.hardware.*;
import net.paymate.data.sinet.EntityHome;

public class SinetServer extends Service {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SinetServer.class);

  public static final String NAME = "SinetServer";

  // singleton stuff
  public static final SinetServer THE() {
    return sinetServer;
  }
  private static SinetServer sinetServer = null;
  public static final ServiceConfigurator initialize(EasyCursor configs,
      int TXNTHREADPRIORITY, int WEBTHREADPRIORITY) {
    sinetServer = new SinetServer(null);
    return sinetServer.init(configs, TXNTHREADPRIORITY, WEBTHREADPRIORITY);
  }

  public SinetServer(ServiceConfigurator cfg) {
    super(NAME, cfg);
  }

  public String svcLogFile() {
    return paniclogFile.status();
  }
  public boolean isUp() {
    return !isDown() && inited();
  }
  public void down() {
    // stub
  }
  public void up() {
    // stub
  }

  private boolean shutdown = false;

  public final void shUtdOwn() {
    // first, tell the Servlets +++
    shutdown = true;
    storeCronService.down();
    statusClient.down();
    updateService.down();
    statusServer.down();
    apptrkr.down();
//    // +++ do more here, including gc's!
//    ThreadX.sleepFor(100);
    // then the authorizers
    ConnectionServer.THE().authmgr.downAll();
//    ThreadX.sleepFor(3000); // gimme a few secs to do stuff, then ...
//    // then ... ??? +++ %%% @@@
    ThreadX.sleepFor(10.0); // gimme a few secs to do stuff, then ...
  }

  public final boolean isDown() {
    return shutdown; // +++ don't return true until the only sessions left are for gods !!!
  }

  private boolean inited = false;
  public final boolean inited() {
    return inited;
  }
  private final Monitor myInit = new Monitor("UserSessionInit");
  private final String sep = System.getProperty("file.separator");
  private String HOSTNAME = "HOSTNAMEnotSETyet";
  private boolean ISPRODUCTION = true;
  // +++ get from configs:
  private int TXNTHREADPRIORITY = Thread.NORM_PRIORITY+1;
  private int WEBTHREADPRIORITY = Thread.NORM_PRIORITY;

  private final ServiceConfigurator init(EasyCursor configs,
                                        int TXNTHREADPRIORITY,
                                        int WEBTHREADPRIORITY) {
   return init(configs, TXNTHREADPRIORITY, WEBTHREADPRIORITY, false);
  }

  private final ServiceConfigurator init(EasyCursor configs,
                                        int TXNTHREADPRIORITY,
                                        int WEBTHREADPRIORITY,
                                        boolean standalone) {
    try {
      myInit.getMonitor();
      if(!inited) {
        LogFile.defaultBackupStream.println("initializing SinetServer!");
        PrintFork.SetAll(LogSwitch.WARNING);
//        System.out.println("Usersession.initStart: current logging looks like this:\nLogswitches:\n"+LogSwitch.listLevels()+"\nPrintforks:\n"+PrintFork.asProperties());
        // load configs from the servlets config file
        grabbedParams = configs;
        HOSTNAME = configs.getString("computername", "UNKNOWN HOST").toLowerCase();
        EasyCursorServiceConfigurator localconfigger = new EasyCursorServiceConfigurator(grabbedParams);
        String logPath = configs.getString("logPath", LogFile.DEFAULTPATH);
        String logArchivePath = configs.getString("logArchivePath", LogFile.DEFAULTARCHIVEPATH);
        // initialize the logfile paths
        LogFileService.onecfg = localconfigger;
        LogFileService.one(); // makes sure that the service exists
        LogFile.setPath(logPath, logArchivePath) ; // set the default Log Path before creating any logs
        // setup the log file for the system
        // even though ErrorLogStream handles it, UserSession will report on the logfile, since it started it.
        ErrorLogStream.stdLogging(NAME);
        PrintFork elspf = ErrorLogStream.fpf.getPrintFork(); // should already exist, but I'm getting it so that I can set its level
        elspf.myLevel.setLevel(LogLevelEnum.VERBOSE);
        ErrorLogStream.Choke(LogLevelEnum.VERBOSE);
        // setup the mailer; will use defaults until the database is connected.
        String emergemail = configs.getString("emergencyEmail", "");
        dbg.ERROR("emergencyEmail loaded from configs as: '" + emergemail + "'!");
        mailer = SendMail.New(emergemail, localconfigger); // Depends on Service.init().
        ISPRODUCTION = configs.getBoolean("isproduction", true);
        boolean shouldValidate = configs.getBoolean("dbvalidate", true);
        Service.init(mailer, HOSTNAME);
        // validate the database
        dbg.ERROR(SessionedServlet.class.getName()+" validating database ...");
        String dblogin = configs.getString("dblogin", "mainsail");
        String dbpassword = configs.getString("dbpassword", "1strauss");
        String dburl = StringX.TrivialDefault(configs.getString("dburl"), configs.getString("informixAddress"));
        String dbdriver = StringX.TrivialDefault(configs.getString("dbDriver"), configs.getString("dbdriver"));
        int oversize = configs.getInt("pooloversize", DBConnInfo.DEFAULTOVERSIZE);
        int intervalSecs = configs.getInt("intervalsecs", DBConnInfo.DEFAULTINTERVALSECS);
        String keepaliveSQL = configs.getString("keepalivesql", DBConnInfo.DEFAULTKEEPALIVESQL);
        DBConnInfo dbConnInfo = new DBConnInfo(dburl, dblogin, dbpassword, dbdriver, oversize, intervalSecs, keepaliveSQL);
        dbg.ERROR("loaded db configs: " + dbConnInfo);
        this.setConfigger(configger);
        // do NOT validate if we are running standalone!  Takes too long (etc)!
        PayMateDB.init(dbConnInfo, ISPRODUCTION && ! standalone, shouldValidate && ! standalone); // db.init() will only actually work once ...
        // reset these so they will get its configs from the database:
        mailer.setConfigger(configger);
        LogFileService.onecfg = configger;
        if( ! standalone) {
          // start the status client
          statusClient = new StatusClient(configger); // depends on Service.init()
          // create the services
          apptrkr = ApplianceTrackerList.getTrackerList(configger); // depends on Service.init()
        }
        // initialize various other misc items
        PayMatePage.init(ISPRODUCTION);
        String defaultColors = configger.getServiceParam(NAME,"defaultClrscheme", (new ColorSchema(ColorSchema.MONEY)).Image());
        ColorScheme.setDefaultScheme(ColorScheme.schemeForName(defaultColors));
        // for default creation ...
        Associate.DEFAULTCOLORS = defaultColors;
        PayMateDB.setServerDefaultColors(); // passes them into the validator
        if( ! standalone) {
          // status server ...
          statusServer = new StatusServer(configger, apptrkr);
          // update service ...
          updateService = new UpdateService(configger);
        }
        initSS2(); // apptrkr should go before this one!
        LogControlService.setLogControl(configger); // causes it to load and set the levels
        // next service depends on another one...
        // this must come late in the list so that everything else is up by now!
        if( ! standalone) {
          ConnectionServer.init(configger, apptrkr,
                                true
                                /*preloadAllAuths; get from config file+++*/,
                                TXNTHREADPRIORITY, WEBTHREADPRIORITY); // parameter
          storeCronService = new StoreCronService(configger); // depends on ConnectionServer
          PayMateDB.startBackgroundValidator();
        }
        inited = true;
        dbg.ERROR("SinetServer inited");
        LogFile.defaultBackupStream.println("SinetServer Inited!");
      } else {
        LogFile.defaultBackupStream.println("SinetServer was already inited!");
      }
    } catch (Throwable e) {
      LogFile.backupLogException("Could not init SinetServer!", e);
//      dbg.Caught(e);
//      PANIC("COULD NOT INIT USERSESSION!");
    } finally {
      myInit.freeMonitor();
      return configger;
    }
  }
  private final void initSS2() {
    try {
      dbg.ERROR("initSS2() starting ...");
      EntityHome.init(configger);
      // now, load all of the classes up!
      if(EnterpriseHome.GetAllIds().length == 0) {// Check to see if there are any enterprises.  If not, make one.
        // creates an enterprise with one store
        // and with a single gawd login with storeaccess
        Enterprise newent = EnterpriseHome.New(null); // +++ set the enterprise properties!
        Store newstore = newent.newStore(); // +++ set the store properties!
        Associate newass = newent.newAssociate(); // +++ set the associate properties!
        // +++ @SS2 the rest of the associate stuff should be created here!
        PANIC("Completed setup of a new (START) enterprise.  "+
              "Note that you will have to create the storeaccess manually in PG, "+
              "though, as that isn't coded yet. ");
      }
      StoreHome.GetAllIds();
      ApplianceHome.GetAllIds();
      AssociateHome.GetAllIds();
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      dbg.ERROR("initSS2() done");
    }
  }
  // singles services
  private StatusServer statusServer = null;
  private UpdateService updateService = null;
  private StoreCronService storeCronService = null;
  private PayMateDBDispenser configger = new PayMateDBDispenser();
  private ApplianceTrackerList apptrkr = null;
  private SendMail mailer = null;
  private StatusClient statusClient = null; // UDP
  public EasyCursor grabbedParams = new EasyCursor();

  // this will only run in standalone mode
  public void archive(String storeid) {
    net.paymate.web.ReceiptArchiver archiver = new net.paymate.web.ReceiptArchiver();
    archiver.receiptArchiveReport(storeid);
  }

  // can run authbills and archiver from here
  public static final void main(String params[]) {
    try {
      if(params.length < 1) {
        System.out.println("Usage: SinetServer configfilename");
        return;
      }
      // load the properties from the config file
      // clean up the parameters so that we can use them
      // a sample line: servlets.default.initArgs=logPath=/data/logs
      String tag = "servlets.default.initArgs=";
      TextList lines = TextList.CreateFrom(net.paymate.io.IOX.FileToString(params[0]));
      TextList clean = new TextList();
      for(int i = lines.size(); i-->0;) {
        String line = lines.itemAt(i);
        if(line.indexOf(tag) > -1) {
          clean.add(StringX.replace(line, tag, ""));
        }
      }
      {  // do these two lines before initing the database!
        java.sql.DriverManager.setLogStream(System.out);
        org.postgresql.Driver.setLogLevel(3);
      }
      EasyCursor props = new EasyCursor(clean.toString());
      SinetServer sinetServer = new SinetServer(null);
      sinetServer.init(props, Thread.NORM_PRIORITY, Thread.NORM_PRIORITY, true);
      // run whatever it is we want to run!
      // --- for now, assume we want to run the ReceiptArchiver; add the authbills later
      sinetServer.archive(params.length > 1 ? params[1] : "");
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      LogFile.ExitAll();
    }
  }
}
