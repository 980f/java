package net.paymate.data.sinet.hardware;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/hardware/ApplNetStatus.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.data.sinet.*;
import net.paymate.util.*;
import net.paymate.net.IPSpec;
import net.paymate.data.*;

// no more persistence 20040523 !

public class ApplNetStatus /*extends EntityBase*/ {
  private static final SinetClass myclass = new SinetClass(SinetClass.ApplNetStatus);
  public SinetClass getSinetClass() {
    return myclass;
  }
//  public void loadFromProps() {
//    EasyProperties myprops = getProps();
//    applianceid = new Applianceid(myprops.getInt(APPLIANCEID));
//    appltime = UTC.fromSeconds(myprops.getInt(APPLTIME));
//    lanip = IPSpec.New(IPSpec.ipFromInt(myprops.getInt(LANIP)));
//    srvrtime = UTC.fromSeconds(myprops.getInt(SRVRTIME));
//    wanip = IPSpec.New(IPSpec.ipFromInt(myprops.getInt(WANIP)));
//  }
//  public synchronized void storeToProps() {
//    EasyProperties myprops = getProps();
//    myprops.setInt(APPLIANCEID, applianceid.value());
//    myprops.setInt(APPLTIME, appltime.toSeconds());
//    myprops.setInt(LANIP, lanip.ipToInt());
//    myprops.setInt(SRVRTIME, srvrtime.toSeconds());
//    myprops.setInt(WANIP, wanip.ipToInt());
//  }

  public synchronized boolean setAndStore(UTC appltime, IPSpec lanip, IPSpec wanip) {
    this.appltime = appltime;
    this.lanip = lanip;
    this.srvrtime = UTC.Now();
    this.wanip = wanip;
    return true;//store();
  }

  // +++ put these into a TrueEnum?
  public static final String APPLNETSTATUSID = "applnetstatusid";
  // fieldname constants - ALL MUST be lower case!
  public static final String APPLIANCEID     = "applianceid";
  public static final String APPLTIME        = "appltime";
  public static final String SRVRTIME        = "srvrtime";
  public static final String LANIP           = "lanip";
  public static final String WANIP           = "wanip";

  // Applnetstatusid applnetstatusid - UniqueID	serial(int4)	unique ID for this data type
  // see id()
  private Appliance appliance; // reference to Appliance
  private Applianceid applianceid;
  public UTC appltime; //int4 time of appliance when last udp update occurred
  public UTC srvrtime; //int4 time of server when last udp update occurred
  public IPSpec lanip; //int4 Last reported LAN IP
  public IPSpec wanip; //int4 Last reported WAN IP
}
