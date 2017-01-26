package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplPgmStatus.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

// only keep one of these in ram, the last one

import net.paymate.net.IPSpec;
import net.paymate.data.sinet.*;
import net.paymate.util.EasyProperties;
import net.paymate.util.UTC;

// no more persistence 20040523 !

public class ApplPgmStatus /*extends EntityBase*/ {
  private static final SinetClass myclass = new SinetClass(SinetClass.ApplPgmStatus);
  public SinetClass getSinetClass() {
    return myclass;
  }
//  public void loadFromProps() {
//    EasyProperties myprops = getProps();
//    applianceid = new Applianceid(myprops.getInt(APPLIANCEID));
//    revision = myprops.getString(REVISION);
//    appltime = UTC.fromSeconds(myprops.getInt(APPLTIME));
//    srvrtime = UTC.fromSeconds(myprops.getInt(SRVRTIME));
//    freemem = myprops.getInt(FREEMEM);
//    ttlmem = myprops.getInt(TTLMEM);
//    threadcount = myprops.getInt(THREADCOUNT);
//    alarmcount = myprops.getInt(ALARMCOUNT);
//    wanip = IPSpec.New(IPSpec.ipFromInt(myprops.getInt(WANIP)));
//    stoodrcpt = myprops.getInt(STOODRCPT);
//    stoodtxn = myprops.getInt(STOODTXN);
//    wasconnection = myprops.getBoolean(WASCONNECTION);
//  }
//  public synchronized void storeToProps() {
//    EasyProperties myprops = getProps();
//    myprops.setInt(APPLIANCEID, applianceid.value());
//    myprops.setString(REVISION, revision);
//    myprops.setInt(APPLTIME, appltime.toSeconds());
//    myprops.setInt(SRVRTIME, srvrtime.toSeconds());
//    myprops.setInt(FREEMEM, freemem);
//    myprops.setInt(TTLMEM, ttlmem);
//    myprops.setInt(THREADCOUNT, threadcount);
//    myprops.setInt(ALARMCOUNT, alarmcount);
//    myprops.setInt(WANIP, wanip.ipToInt());
//    myprops.setInt(STOODRCPT, stoodrcpt);
//    myprops.setInt(STOODTXN, stoodtxn);
//    myprops.setBoolean(WASCONNECTION, wasconnection);
//  }

  public synchronized boolean setAndStore(
      int alarmcount, UTC appltime, int freemem, String revision,
      int stoodrcpt, int stoodtxn, int threadcount, int ttlmem,
      IPSpec wanip, boolean wasconnection) {
    this.alarmcount = alarmcount;
    this.appltime = appltime;
    this.freemem = freemem;
    this.revision = revision;
    this.srvrtime = UTC.Now();
    this.stoodrcpt = stoodrcpt;
    this.stoodtxn = stoodtxn;
    this.threadcount = threadcount;
    this.ttlmem = ttlmem;
    this.wanip = wanip;
    this.wasconnection = wasconnection;
    return true;//store();
  }

  public final static String APPLPGMSTATUSID = "applpgmstatusid";
  public final static String APPLIANCEID = "applianceid";
  public final static String REVISION = "revision";
  public final static String APPLTIME = "appltime";
  public final static String SRVRTIME = "srvrtime";
  public final static String FREEMEM = "freemem";
  public final static String TTLMEM = "ttlmem";
  public final static String THREADCOUNT = "threadcount";
  public final static String ALARMCOUNT = "alarmcount";
  public final static String STOODRCPT = "stoodrcpt";
  public final static String STOODTXN = "stoodtxn";
  public final static String WANIP = "wanip";
  public final static String WASCONNECTION = "wasconnection";

  // applpgmstatusid UniqueID	serial(int4)	unique ID for this data type
  Applianceid applianceid = null;//reference to Appliance 	int4 Appliance
  public String revision = null; // reference to Revision+++	int4 Software revision it is reporting.
  public UTC appltime = null; //int4 time of appliance when last update occurred
  public UTC srvrtime = null; // int4 time of system when last update occurred
  public int freemem = 0; //int4 bytes of memory free on appliance when last update occurred
  public int ttlmem = 0; //int4 bytes of memory available to JVM on appliance when last update occurred
  public int threadcount = 0; //int4 number of running threads on appliance when last update occurred
  public int alarmcount = 0; //int4 number of running alarms on appliance when last update occurred
  public IPSpec wanip = null; //int4 Last reported WAN IP
  public int stoodrcpt = 0; //int4 How many receipts were stoodin when the last update occurred
  public int stoodtxn = 0; //int4 How many txns were stoodin when the last update occurred
  public boolean wasconnection = false; //boolean true for connectionrequest, false for updaterequest

  public boolean after(ApplPgmStatus other) {
    if((other != null) &&
       (other.srvrtime != null) &&
       (this.srvrtime!=null) &&
       this.srvrtime.after(other.srvrtime)) {
      return true;
    } else {
      return false;
    }
  }

  public static ApplPgmStatus mostRecent(ApplPgmStatus first, ApplPgmStatus second) {
    if(first == null) {
      return second;
    } else if(second == null) {
      return first;
    } else {
      return first.after(second) ? first : second;
    }
  }
}
