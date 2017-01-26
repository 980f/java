package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/Appliance.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.data.sinet.*;
import net.paymate.data.sinet.business.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;

public class Appliance extends EntityBase implements TimeBomb {

  private static final SinetClass myclass = new SinetClass(SinetClass.Appliance);
  public SinetClass getSinetClass() {
    return myclass;
  }

  private static int burpFilter = Integer.MAX_VALUE;
  // sharing a QAgent means that we only have to import np.util, and not np.connection classes
  // it also allows the Alarmer stuff to run faster (I hope)
  private static QAgent agent = null;
  public static synchronized void init(int burpFilter, QAgent agent) {
    Appliance.burpFilter = burpFilter;
    Appliance.agent = agent;
  }

  // appliance tracking stuff ...
  private boolean alerted=false; //we have sent out a stale alarm
  // last java update that was and was not a connection
  public ApplPgmStatus lastPgmConnection = null;
  public ApplPgmStatus lastPgmUpdateOnly = null;
  // last udp network update
  public ApplNetStatus lastUdpUpdate = null;
  // this means of any kind, including connections
  public ApplPgmStatus mostRecentPgmStatus() {
    return ApplPgmStatus.mostRecent(lastPgmConnection, lastPgmUpdateOnly);
  }
  public synchronized void setAlert() {
    alerted = true;
  }
  public synchronized boolean takeAlert() {
    boolean ret = alerted;
    alerted = false;
    return ret;
  }
  public boolean peekAlert() { // for reports
    return alerted;
  }
  public synchronized void logUpdate(ApplPgmStatus latest){
    if(latest != null) {
      Alarmer.Defuse(stale);
      if(latest.wasconnection) {
         // +_+ the order of these in the parameter list may be important
        lastPgmConnection = ApplPgmStatus.mostRecent(latest, lastPgmConnection);
      } else {
         // +_+ the order of these in the parameter list may be important
        lastPgmUpdateOnly = ApplPgmStatus.mostRecent(latest, lastPgmUpdateOnly);
      }
      setupAlarm();
    }
  }
  // no more persistence!
  public synchronized void logUpdate(ApplNetStatus latest){
    if(latest != null) {
      // +_+ set whichever is really the most recent?  (check it)
      lastUdpUpdate = latest;
    }
  }
  private static final long clockskew(ApplPgmStatus aps){
    if(aps == null) {
      return 0;
    } else {
      return aps.appltime.isValid() ?
          aps.srvrtime.getTime() - aps.appltime.getTime() : 0;
    }
  }
  public final long clockskewPgmUPDT(){
    return clockskew(lastPgmUpdateOnly);
  }
  public final long clockskewPgmCNXN(){
    return clockskew(lastPgmConnection);
  }
  Alarmum stale = null;
  public boolean isStale(){
    return !Alarmer.isTicking(stale);
  }
  // make the next one available from the outside
  // so that we can push a button to cause it to refresh?
  private synchronized void setupAlarm() {
    Alarmer.Defuse(stale); // possibly superfluous, but just in case
    stale=Alarmer.New(((int)periodTicks())*burpFilter, this);
  }
  public void onTimeout(){
    if(agent != null) {
      agent.Post(this);
    }
  }

  // deathcode stuff
  private int deathcode = 0; // USE THE ENUMERATION !!! +++
  public synchronized int takeDeathcode() {
    int olddeathcode = deathcode;
    deathcode = 0;
    return olddeathcode;
  }
  public int peekDeathcode() { // for reports
    return deathcode;
  }
  public synchronized boolean setDeathcode(int deathcode) {
    // +++ if deathcode != 0 ...
    this.deathcode = deathcode;
    return true;
  }

  public Applianceid applianceid() {
    return new Applianceid(id().value());
  }

  public long periodTicks() {
    return Ticks.forSeconds(statusival);
  }
  public long txnHoldoffTicks() {
    return Ticks.forSeconds(txnholdoff);
  }

  public void loadFromProps() {
    EasyProperties myprops = getProps();
    applname = myprops.getString(APPLNAME);
    enabled = myprops.getBoolean(ENABLED);
    statusival = myprops.getInt(STATUSIVAL);
    storeid = new Storeid(myprops.getInt(STOREID));
    track = myprops.getBoolean(TRACK);
    txnholdoff = myprops.getInt(TXNHOLDOFF);
    // load the store object
    store = StoreHome.Get(storeid);
    if(store != null) { // at least in the beginning, some appliances will not have stores (until we get the database cleaned up)
      store.linkAppliance(this);
    }
    // get the lasts ...
    // we no longer persist these, so no need to look them up
//    lastPgmConnection = ApplPgmStatusHome.GetLastConnection(applianceid());
//    lastPgmUpdateOnly = ApplPgmStatusHome.GetLastNonConnection(applianceid());
//    lastUdpUpdate     = ApplNetStatusHome.GetLastUpdate(applianceid());
    setupAlarm();
  }
  public synchronized void storeToProps() {
    EasyProperties myprops = getProps();
    myprops.setString(APPLNAME, applname);
    myprops.setBoolean(ENABLED, enabled);
    myprops.setInt(STATUSIVAL, statusival);
    myprops.setInt(STOREID, storeid.value());
    myprops.setBoolean(TRACK, track);
    myprops.setInt(TXNHOLDOFF, txnholdoff);
  }

  // +++ put these into a TrueEnum?
  public static final String APPLIANCEID = "applianceid";
  // fieldname constants - ALL MUST be lower case!
  public static final String APPLNAME = "applname";
  public static final String ENABLED = "enabled";
  public static final String STATUSIVAL = "statusival";
  public static final String STOREID = "storeid";
  public static final String TRACK = "track";
  public static final String TXNHOLDOFF = "txnholdoff";

  public String applname = "";
  public boolean enabled= false;
  public int statusival = 0;
  public Storeid storeid = null;
  public Store store = null;
  public boolean track = false;
  public int txnholdoff = 0;

  public String toString() {
    return ""+id()+":"+applname;
  }

}