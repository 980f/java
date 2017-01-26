package net.paymate.web;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/StoreCronService.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.*;
import net.paymate.util.timer.*;
import net.paymate.lang.ThreadX;
import net.paymate.data.sinet.business.Store;
import net.paymate.data.sinet.business.StoreCronCallback;
import net.paymate.data.sinet.business.StoreHome;
import net.paymate.data.sinet.business.Storeid;

// +++ make a base class out of this (CronService), and then let all cron-like services use it

public class StoreCronService extends Service implements Runnable, StoreCronCallback {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreCronService.class);

  public static final int POLLRATEDEFAULT = 0;
  private int pollRateMins = POLLRATEDEFAULT; // <= 0 means not to.
  private Thread thread = null;
  private boolean on = false;
  private boolean cont = false;

  public StoreCronService(ServiceConfigurator cfg) {
    super("StoreCron", cfg);
    initLog();
    up(); // loads the parameters & starts the listening thread
  }

  public String svcPend() {
    return isUp() ? "1" : "0";
  }
  public String svcTxns() {
    return of(timesDidSomething.value(), overallTime.getCount());
  }
  public String svcAvgTime() {
    return DateX.millisToSecsPlus(overallTime.getAverage());
  }
  public String svcReads() {
    return printStats(reads);
  }
  public String svcWrites() {
    return printStats(writes);
  }
  public String svcNotes() {
    return "" + pollRateMins + " min interval";
  }
  public boolean isUp() {
    dbg.ERROR("thread = " + thread + ", thread.isAlive()=" + ((thread != null) && thread.isAlive()) + ", on="+on+", cont="+cont);
    return (thread != null) && thread.isAlive() && on && cont;
  }

  public void up() {
//    if(!on || !thread.isAlive()){
    if(!isUp()) {
      loadConfigs();
      // start the thread
      if((thread != null) && !thread.isAlive()) {
        thread = null;
      }
      if (thread == null) {
        thread = new Thread(this, StoreCronService.class.getName());
      }
      cont = true;
      if (!thread.isAlive()) {
        thread.setDaemon(true); // or pass a parameter into here +++
        thread.start();
      }
      markStateChange();
    }
  }
  public void down() {
    //stop all background activity, dumping anything queued.
    cont = false;
    Thread theThread = thread;
    if(theThread != null) {
      theThread.interrupt();
    }
    markStateChange();
  }

  protected void loadConfigs() {
    pollRateMins = configger.getIntServiceParam(serviceName(),"pollRateMins", POLLRATEDEFAULT);
  }

  private Accumulator reads = new Accumulator();
  private Accumulator writes = new Accumulator();
  private StopWatch onetimer = new StopWatch(false);

  public boolean runone() {
    boolean didSomething = false;
    try {
      Store [ ] stores = StoreHome.GetAll();
      for(int i = stores.length; i-->0;) {
        Store s = stores[i];
        if(s != null) {
          onetimer.Start();
          boolean storeDidSomething = s.autoDrawerAndDeposit(this,this);
          if(storeDidSomething) {
            writes.add(onetimer.Stop());
            didSomething = true;
          } else {
            reads.add(onetimer.Stop());
          }
        } else {
          PANIC("Store object was null!");
        }
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return didSomething;
    }
  }

  private Accumulator overallTime = new Accumulator();
  private Counter timesDidSomething = new Counter();
  private ConnectionServer theCnxnSrvr = null;
  public void run() {
    dbg.ERROR("$Id: StoreCronService.java,v 1.8 2004/01/27 20:34:00 mattm Exp $ pollRateMins="+pollRateMins+"; running ...");
    on = true;
    try {
      if(pollRateMins > 0) {
        theCnxnSrvr = ConnectionServer.THE();
        StopWatch sw = new StopWatch(false);
        while(cont) { // +++++++++ add the onexit stuff !!!!
          sw.Start();
          boolean didSomething = runone();
          overallTime.add(sw.Stop());
          if(!didSomething) { // only sleep if we didn't do anything
            ThreadX.sleepFor(Ticks.forMinutes(pollRateMins)); // every N minutes
          } else {
            timesDidSomething.incr();
            // the following is important since the storeCron stuff happens on minute intervals
            // in other words, the schedule is set to the minute
            // this is regardless of the amount of sleeptime between checks (although that should be no less than 1 minute)
            // need to sleep out the rest of a minute!
            long sleepleft = Ticks.forMinutes(1) - sw.millis();
            if(sleepleft > 0) {
              ThreadX.sleepFor(sleepleft); // at least one minute
            }
          }
        }
      } else {
        dbg.WARNING("StoreCronService not running since pollRateMins is <= 0!");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      on = false;
    }
    dbg.ERROR("$Id: StoreCronService.java,v 1.8 2004/01/27 20:34:00 mattm Exp $ ... stopped.");
  }

  // callback stuff
  public /*MultiReply mr = */ void autoCloseAllDrawers(Storeid storeid) {
    if(theCnxnSrvr == null) {
      // +++ ???
    } else {
      theCnxnSrvr.closeAllDrawers(storeid, true /*auto*/);
    }
  }
  public boolean autoIssueDeposit(Storeid storeid) {
    if(theCnxnSrvr == null) {
      // +++ ???
      return false;
    } else {
      return theCnxnSrvr.issueDeposit(storeid, true /*auto*/);
    }
  }

}