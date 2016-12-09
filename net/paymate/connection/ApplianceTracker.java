package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ApplianceTracker.java,v $
 * Description:  pool of applianceTrackers, each of whihc tracks appliance status
 * Synchronizations: functions which manipulate the list as a whole.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.timer.*;
import net.paymate.util.*;
import net.paymate.net.SendMail;
import net.paymate.database.*;

import java.util.*;

public class ApplianceTracker implements TimeBomb {
  private String servicename = "APPLIANCETRACKER";
  private static final ErrorLogStream dbg = new ErrorLogStream(ApplianceTracker.class.getName());

//global tracking mechanism
  static Hashtable appliances = new Hashtable(20);
//these need to be servlet parameters:
  public static int burpFilter=3; //allow two updates to be skipped
  public static String alertListName="staleAppliance";

// now for the per appliance tracking fields
//  private UpdateRequest urq;//we are making a psuedo-extension of this guy
  //the next three were at one time in updateRequest, and will eventually just be in the database:
  public String applianceName = "";
  public Date srvrTime = null;
  public long lastTime = 0;

/**
 * return appliances clock  skew with respec tto server
 */
  public long clockskew(){
    return clockskew(lastTime, srvrTime.getTime());
  }
  public static final long clockskew(long lastTime, long srvrTime){
    return lastTime>0? srvrTime - lastTime : 0;
  }

  public boolean isStale(){
    return !Alarmer.isTicking(stale);
  }

  public static final boolean isStale(String applianceId){
    boolean ret = true;
    ApplianceTracker tracker = getTracker(applianceId);
    if(tracker != null){
      ret = tracker.isStale();
    }
    return ret;
  }

  Alarmum stale;
  public void onTimeout(){
    if(mailsender != null) {
      mailsender.send(db.getServiceParam(servicename, alertListName, "alien@spaceship.com,alheilveil@austin.rr.com"),
        applianceName+" last reported to " + hostname + " at "+ Safe.timeStamp(srvrTime), // +++ use ltf of default for service here
        db.getApplianceTerminalStore(applianceName));
    } else {
      dbg.ERROR("mailsender is null!");
    }
  }

  static ApplianceTracker getTracker(String key){
    return (ApplianceTracker)appliances.get(key);
  }

  private static String hostname = "unknown";
  private static SendMail mailsender = null;
  private static PayMateDB db = null;
  public static final void init(String hostname, SendMail mailsender, PayMateDB db) {
    ApplianceTracker.hostname = hostname;
    ApplianceTracker.mailsender = mailsender;
    ApplianceTracker.db = db;
  }

  /**
   * restart appliance tracking.
   */
// @@@ mak an interface to this
  public static synchronized final void ClearAll() {
    appliances.clear();
  }

  /**
   * remove an applinace tracking record from the global list
   */
// @@@ mak an interface to this
  public static final void remove(String applianceId){
    ApplianceTracker tracker = getTracker(applianceId);
    if(tracker != null){
      Alarmer.Defuse(tracker.stale);
      appliances.remove(tracker);
    }
  }

  /**
   * update or create appliance update tracker
   */
  /* package */ static final void logUpdate(UpdateRequest request){
    ApplianceTracker tracker = getTracker(request.applianceId);
    if(tracker != null){
      Alarmer.Defuse(tracker.stale);
    } else {
      tracker= new ApplianceTracker(request.applianceId);
    }
    tracker.lastTime=request.requestInitiationTime;
    tracker.srvrTime= Safe.Now();
    tracker.stale=Alarmer.New(request.opt.period*burpFilter,tracker);
    appliances.put(request.applianceId, tracker);
    db.logApplianceUpdate(request.applianceId /* really the name */, request.runtimeinfo.revision,
      request.requestInitiationTime, tracker.srvrTime.getTime(), request.runtimeinfo.freeMemory, request.runtimeinfo.totalMemory,
      request.runtimeinfo.activeCount, request.runtimeinfo.activeAlarmsCount, request.txnCount, request.rcptCount);
  }

  private ApplianceTracker(String applianceName) {
    //see update.
    this.applianceName = applianceName;
  }

}