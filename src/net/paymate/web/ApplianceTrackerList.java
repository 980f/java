package net.paymate.web;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/ApplianceTrackerList.java,v $
 * Description:  Contains & manages the list of ApplianceTrackers (one for each Appliance).
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */
import net.paymate.util.*; // ErrorLogStream, Service,etc.
import net.paymate.util.timer.*; // Alarmer
import net.paymate.net.*; // StatusPacket
import net.paymate.database.*; // PayMateDB
import net.paymate.data.*; // Applianceid
import java.util.*; // hashtable
import net.paymate.lang.StringX;
import net.paymate.data.sinet.hardware.*;
import net.paymate.connection.*;

// +++ could some of this go on appliancehome?

public class ApplianceTrackerList extends Service implements QActor {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ApplianceTrackerList.class);
// singleton
  private static ApplianceTrackerList THE = null;
  public synchronized static final ApplianceTrackerList getTrackerList(PayMateDBDispenser dbd) {
    if((THE == null) && (dbd != null)) { // only works the first time
      THE = new ApplianceTrackerList(dbd);
    }
    return THE;
  }

  QAgent agent = null;

  private int burpFilter=3; //allow N updates to be skipped; set from configs

  /* package */ void sendAlert(String alarming, Appliance theone){
    // list eventually needs to be on a per-store basis, perhaps
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    PANIC(alarming+
          ":A="+theone.toString()+
          ",S="+((theone.store != null) ? theone.store.toString() : "NOSTORE")+
          "\nTerminals: "+db.getTerminalsForAppliance(new Applianceid(theone.id().value()), true /*withids*/).toString()
          );
  }

  public static int MINUPDATEPERIOD = (int)Ticks.forMinutes(2); // up() gets this from configs

  /**
   * IP Status tracker
   * update or create appliance update tracker, log to the database
   */
  public void logUpdate(StatusPacket packet) {
    StopWatch sw = new StopWatch();
    try {
      attempted.incr();
      Appliance apptracker = ApplianceHome.GetByName(packet.ethernetAddress);
      if(apptracker != null) {
        ApplNetStatus newone = ApplNetStatusHome.New(apptracker.applianceid());
        newone.setAndStore(UTC.New(packet.time), IPSpec.New(packet.ipAddress), IPSpec.New(packet.wanIpAddress));  // check return value? +++
        apptracker.logUpdate(newone);
        // determine if we need to send an email & do, if so +++ @@@ %%%
      } else {
        PANIC("Rcvd updateRequest fr:Appliance "+packet.ethernetAddress+"; NOT in DB!");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      completed.incr();
      times.add(sw.Stop());
    }
  }

  /**
   * Appliance status tracker
   * update or create appliance update tracker, log to the database
   * ConnectionRequests are just put through here to update the times ...
   */
  public UpdateReply logUpdate(UpdateRequest request, IPSpec fromip){
    UpdateReply newone = new UpdateReply();
    StopWatch sw = new StopWatch();
    try {
      boolean isConnectionRequest = (request instanceof ConnectionRequest);
      attempted.incr();
      Appliance tracker = ApplianceHome.GetByName(request.applname);
      if(tracker != null) {
        ApplPgmStatus newstatus = ApplPgmStatusHome.New(tracker.applianceid());
        if(newstatus == null) {
          PANIC("EXCEPTION calling ApplPgmStatusHome.New("+tracker.applianceid()+")!");
        } else {
          try {
            newstatus.setAndStore(request.runtimeinfo.activeAlarmsCount,
                                  request.requestInitiationTime,
                                  (int) request.runtimeinfo.freeMemory,
                                  request.runtimeinfo.revision,
                                  request.rcptCount,
                                  request.txnCount,
                                  request.runtimeinfo.activeCount,
                                  (int) request.runtimeinfo.totalMemory,
                                  (fromip != null) ? fromip : new IPSpec(),
                                  isConnectionRequest
                                  ); // check return value? +++
          } catch (Exception ex) {
            dbg.Caught(ex);
          }
        }
        if(tracker.takeAlert()){
          sendAlert(" back online ",tracker);
        }
        tracker.logUpdate(newstatus);
        // deathcode: only do if update, not connection
        if(!isConnectionRequest) {
          // +++ we need to db-log the sending of these deathcodes somewhere!
          newone.deathCode = tracker.takeDeathcode(); // gets and resets it
          if (newone.deathCode != 0) {
            sendAlert(" SENT deathcode " + newone.deathCode + " to ", tracker);
          }
        }
        // prepare return
        newone.setState(ActionReplyStatus.Success);
        // NOTE that ApplianceOptions uses ticks whereas Appliance object uses seconds!
        newone.opt.setPeriod( (int) tracker.periodTicks());
        newone.opt.setHoldoff( (int) tracker.txnHoldoffTicks());
        // I'm not sure what this is for ...
        int wasPeriod = newone.opt.period();
        if(!newone.opt.validatePeriod(MINUPDATEPERIOD)) {
          dbg.WARNING("The update interval (period) was less than " + MINUPDATEPERIOD + "ticks [was"+wasPeriod+"]! - " + newone);
        }
      } else {
        PANIC("Rcvd updateRequest fr:Appliance "+request.applname+"; NOT in DB!");
      }
    } catch(Exception e) {
      dbg.Caught(e);
    } finally {
      completed.incr();
      times.add(sw.Stop());
      return newone;
    }
  }

  //////////////////////////
  // SERVICE stuff
  public String svcCnxns() {
    return ""+ApplianceHome.Size(); // for reporting only!
  }

  private static final Counter completed = new Counter();
  private static final Counter attempted = new Counter();
  private static final Accumulator times = new Accumulator();

  public String svcTxns() {
    return of(completed.value(), attempted.value());
  }
  public String svcPend() {
    return "" + (attempted.value() - completed.value());
  }
  public String svcAvgTime() {
    return DateX.millisToSecsPlus(times.getAverage());
  }
  boolean up = false;
  public boolean isUp() {
    return up;
  }
  public boolean isReallyUp() {
    // to prevent NPE's, get a snapshot
    QAgent localagent = agent;
    return (localagent != null) &&
        !localagent.isPaused() &&
        !localagent.isStopped();
  }
  public void down() {
    agent.Stop();
    up = false;
    markStateChange();
  }
  public void up() {
    // load the configs in here ...
    burpFilter = configger.getIntServiceParam(serviceName(), "burpFilter", burpFilter);
    MINUPDATEPERIOD = (int)Ticks.forMinutes(configger.getIntServiceParam(serviceName(), "MINUPDATEPERIOD", burpFilter));
    agent = QAgent.New("APPLIANCETRACKERlist", this, PriorityComparator.Reversed(), Thread.NORM_PRIORITY);
    agent.Start();
    up = true;
    // now setup the Appliance class
    Appliance.init(burpFilter, agent);
    // and send the email
    markStateChange();
  }
  //end service interface
  //////////////////////

  public void Stop() {
    // called whenever the QAgent stops
  }
  public void runone(Object o) {
    if(isUp()) {
      // this means an alarm requires a panic!
      Appliance theone = (Appliance) o;
      if (theone.track) { // don't send alerts unless we are tracking
        theone.setAlert();
        ApplPgmStatus apm = theone.mostRecentPgmStatus();
        sendAlert("Stale last:" + ((apm != null) ? DateX.timeStamp(apm.srvrtime) : "NEVER"), theone);
      }
    }
  }

  // singleton, so private so we can manage it
  private ApplianceTrackerList(PayMateDBDispenser dbd) {
    super("APPLIANCETRACKER", dbd, true);
    up();
  }

}

/* $Id: ApplianceTrackerList.java,v 1.2 2004/02/10 01:10:41 andyh Exp $ */
