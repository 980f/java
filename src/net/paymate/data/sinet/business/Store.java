package net.paymate.data.sinet.business;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/data/sinet/business/Store.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @todo: add function to extract a storeInfo MMM: NO! Don't pervert this
 *        class/package by pulling the connection package in.
 *        Do it in connectionserver instead.
 * @todo: create classes to contain subsets of the current members:
 *        postal address
 *        PointofSaleOptions (enable debit ...)
 *        enable+time of day for auto stuff.
 *        MerchantOptions
 * @version $Revision: 1.21 $
 */

import net.paymate.data.sinet.EntityBase;
import net.paymate.data.sinet.SinetClass;
import net.paymate.data.sinet.hardware.Appliance;
import net.paymate.data.sinet.hardware.ApplianceList;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.EasyProperties;
import net.paymate.data.MerchantType;
import net.paymate.util.UTC;
import net.paymate.util.timer.CronComputer;
import net.paymate.util.Service;
import net.paymate.lang.StringX;
import java.util.TimeZone;//for cron and others.

public class Store extends EntityBase {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(Store.class);
  private static final SinetClass mySinetClass = new SinetClass(SinetClass.Store);

  public SinetClass getSinetClass() {
    return mySinetClass;
  }

  private UTC lastAutoDrawer = null;
  public UTC lastAutoDrawer() {
    return UTC.New(lastAutoDrawer.getTime());
  }
  private UTC lastAutoDeposit = null;
  public UTC lastAutoDeposit() {
    return UTC.New(lastAutoDeposit.getTime());
  }
  private CronComputer cronic;
  public Store setTimeZone(String javatz){
    this.tz=TimeZone.getTimeZone(javatz);
    this.cronic=CronComputer.New(this.tz);
    return this;
  }
  public String timeZoneStr() {
    return tz.getID();
  }
  public TimeZone getTimeZone() {
    return (TimeZone)tz.clone();
  }

  public Storeid storeId(){
    return Storeid.isValid(id()) ? new Storeid(id().value()) : new Storeid();
  }
  // this is where we put the code that checks to see if autodrawer and
  // autodeposit are supposed to run, and if so, does them ...
  public synchronized boolean autoDrawerAndDeposit(Service service, StoreCronCallback callback /*same as other parameter, really!*/) {
    boolean didSomething = false;
    try {
      if(enautodrawer || enautodeposit) {
        Storeid storeid = storeId();
        UTC now = UTC.Now();
        if(enautodrawer && shouldCloseDrawers(now)) {
          String msg = "Store " + toString() + " closing drawers since now["+now+"] is *later* than lastran["+lastAutoDrawer+"] + when["+autodrawermins+"].";
          dbg.WARNING(msg);
          service.PANIC(msg);
          // until we can find a better solution for cronning the drawer closes ...
          /*MultiReply mr = */ callback.autoCloseAllDrawers(storeid);
          // if mr.status = failed, +_+ PANIC !!! ???
          didSomething = true;
          lastAutoDrawer = now;
        }
        if(enautodeposit && shouldIssueDeposit(now)) {
          String msg = "Store " + toString() + " issuing deposit since now["+now+"] is *later* than lastran["+lastAutoDeposit+"] + when["+autodepositmins+"].";
          dbg.WARNING(msg);
          service.PANIC(msg);
          /*boolean did = */
          // until we can find a better solution for cronning the issue deposits ...
          if (! callback.autoIssueDeposit(storeid)) {
            // +_+ PANIC !!!
          }
          didSomething = true;
          lastAutoDeposit = now;
        }
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
      service.PANIC("Exception calling autoDrawerAndDeposit() for store " + toString(), ex);
//    ???  didSomething = false; // don't make it keep doing it over and over again if there is a problem!
    } finally {
      return didSomething;
    }
  }
  // whether to do a drawer close
  private boolean shouldCloseDrawers(UTC now) {
    return cronic.dailyDue(autodrawermins,lastAutoDrawer,now);
  }
  // whether to do the deposit
  private boolean shouldIssueDeposit(UTC now) {
    return cronic.dailyDue(autodepositmins,lastAutoDeposit,now);
  }
  // +++ this eventually will happen elsewhere.  Deal with it for now ...
  private synchronized void loadLastTimes() {
    // load the last times
    net.paymate.database.PayMateDB db = net.paymate.database.PayMateDBDispenser.getPayMateDB();
    Storeid storeid = new Storeid(id().value());
    lastAutoDrawer  = db.getLastAutoDrawerTime(storeid);
    lastAutoDeposit = db.getLastAutoDepositTime(storeid);
  }

  public static final String DEFAULTRCPTTIMEFORMAT = "MM/dd/yy hh:mma";
  public static final String merchantTypeDefault = String.valueOf((new MerchantType()).CharFor(MerchantType.Retail));

  // +++ put these into a TrueEnum?
  public static final String STOREID           = "storeid";
  // fieldname constants - ALL MUST be lower case!
  public static final String ADDRESS1          = "address1";
  public static final String ADDRESS2          = "address2";
  public static final String ALWAYSID          = "alwaysid";
  public static final String AUTODEPOSITMINS   = "autodepositmins";
  public static final String AUTODRAWERMINS    = "autodrawermins";
  public static final String AUTOAPPROVE       = "autoapprove";
  public static final String AUTOCLOSE         = "autoclose";
  public static final String AUTOCOMPLETE      = "autocomplete";
  public static final String AUTOQUERY         = "autoquery";
  public static final String CHECKSALLOWED     = "checksallowed";
  public static final String CITY              = "city";
  public static final String COUNTRY           = "country";
  public static final String ENABLED           = "enabled";
  public static final String PHONE             = "phone";
  public static final String STATE             = "state";
  public static final String ZIPCODE           = "zipcode";
  public static final String CREDITALLOWED     = "creditallowed";
  public static final String DEBITALLOWED      = "debitallowed";
  public static final String DEBITPUSHTHRESHOLD= "debitpushthreshold";
  public static final String ENAUTHONLY        = "enauthonly";
  public static final String ENAUTODEPOSIT     = "enautodeposit";
  public static final String ENAUTODRAWER      = "enautodrawer";
  public static final String ENAUTOLOGOUT      = "enautologout";
  public static final String ENLISTSUMMARY     = "enlistsummary";
  public static final String ENMERCHREF        = "enmerchref";
  public static final String ENMODIFY          = "enmodify";
  public static final String ENTERPRISEID      = "enterpriseid";
  public static final String FREEPASS          = "freepass";
  public static final String JAVATZ            = "javatz";
  public static final String MERCHANTTYPE      = "merchanttype";
  public static final String MERCHREFLABEL     = "merchreflabel";
  public static final String PUSHDEBIT         = "pushdebit";
  public static final String RECEIPTABIDE      = "receiptabide";
  public static final String RECEIPTHEADER     = "receiptheader";
  public static final String RECEIPTSHOWSIG    = "receiptshowsig";
  public static final String RECEIPTTAGLINE    = "receipttagline";
  public static final String RECEIPTTIMEFORMAT = "receipttimeformat";
  public static final String SIGCAPTHRESH      = "sigcapthresh";
  public static final String SILIMIT           = "silimit";
  public static final String SITOTAL           = "sitotal";
  public static final String STORENAME         = "storename";
  public static final String STORENUMBER       = "storenumber";

  public synchronized void loadFromProps() {
    EasyProperties myprops = getProps();
    address1          =myprops.getString(ADDRESS1);
    address2          =myprops.getString(ADDRESS2);
    alwaysid          =myprops.getBoolean(ALWAYSID);
    autoapprove       =myprops.getBoolean(AUTOAPPROVE);
    autoclose         =myprops.getBoolean(AUTOCLOSE);
    autocomplete      =myprops.getBoolean(AUTOCOMPLETE);
    autodepositmins   =myprops.getInt(AUTODEPOSITMINS);
    autodrawermins    =myprops.getInt(AUTODRAWERMINS);
    autoquery         =myprops.getBoolean(AUTOQUERY);
    checksallowed     =myprops.getBoolean(CHECKSALLOWED);
    city              =myprops.getString(CITY);
    country           =myprops.getString(COUNTRY);
    creditallowed     =myprops.getBoolean(CREDITALLOWED);
    debitallowed      =myprops.getBoolean(DEBITALLOWED);
    debitpushthreshold=myprops.getInt(DEBITPUSHTHRESHOLD);
    enabled=myprops.getBoolean(ENABLED);
    enauthonly        =myprops.getBoolean(ENAUTHONLY);
    enautodeposit     =myprops.getBoolean(ENAUTODEPOSIT);
    enautodrawer      =myprops.getBoolean(ENAUTODRAWER);
    enautologout      =myprops.getBoolean(ENAUTOLOGOUT);
    enlistsummary     =myprops.getBoolean(ENLISTSUMMARY);
    enmerchref        =myprops.getBoolean(ENMERCHREF);
    enmodify          =myprops.getBoolean(ENMODIFY);
    enterpriseid      = new Enterpriseid(myprops.getInt(ENTERPRISEID));
    freepass          =myprops.getBoolean(FREEPASS);
    setTimeZone(myprops.getString(JAVATZ));
    merchanttype      =new MerchantType(myprops.getChar(MERCHANTTYPE));
    merchreflabel     =myprops.getString(MERCHREFLABEL);
    phone             =myprops.getString(PHONE);
    pushdebit         =myprops.getBoolean(PUSHDEBIT);
    receiptabide      =myprops.getString(RECEIPTABIDE);
    receiptheader     =myprops.getString(RECEIPTHEADER);
    receiptshowsig    =myprops.getBoolean(RECEIPTSHOWSIG);
    receipttagline    =myprops.getString(RECEIPTTAGLINE);
    receipttimeformat =myprops.getString(RECEIPTTIMEFORMAT);
    sigcapthresh      =myprops.getInt(SIGCAPTHRESH);
    silimit           =myprops.getInt(SILIMIT);
    sitotal           =myprops.getInt(SITOTAL);
    state             =myprops.getString(STATE);
    storename         =myprops.getString(STORENAME);
    storenumber       =myprops.getInt(STORENUMBER);
    zipcode           =myprops.getString(ZIPCODE);
    // load the enterprise
    enterprise = EnterpriseHome.Get(enterpriseid);
    if(enterprise != null) {
      enterprise.linkStore(this);
    } else {
      // +++ we have a big problem if you get here!
    }
    // this causes the auto stuff to load when it is needed ...
    loadLastTimes();
  }

  public void linkAppliance(Appliance appliance) {
    appliances.addAppliance(appliance);
  }

  public void storeToProps() {
    EasyProperties myprops = getProps();
    myprops.setString(ADDRESS1, address1);
    myprops.setString(ADDRESS2, address2);
    myprops.setBoolean(ALWAYSID, alwaysid);
    myprops.setBoolean(AUTOAPPROVE, autoapprove);
    myprops.setBoolean(AUTOCLOSE, autoclose);
    myprops.setBoolean(AUTOCOMPLETE, autocomplete);
    myprops.setInt(AUTODEPOSITMINS, autodepositmins);
    myprops.setInt(AUTODRAWERMINS, autodrawermins);
    myprops.setBoolean(AUTOQUERY, autoquery);
    myprops.setBoolean(CHECKSALLOWED, checksallowed);
    myprops.setString(CITY, city);
    myprops.setString(COUNTRY, country);
    myprops.setBoolean(CREDITALLOWED, creditallowed);
    myprops.setBoolean(DEBITALLOWED, debitallowed);
    myprops.setInt(DEBITPUSHTHRESHOLD, debitpushthreshold);
    myprops.setBoolean(ENABLED, enabled);
    myprops.setBoolean(ENAUTHONLY, enauthonly);
    myprops.setBoolean(ENAUTODEPOSIT, enautodeposit);
    myprops.setBoolean(ENAUTODRAWER, enautodrawer);
    myprops.setBoolean(ENAUTOLOGOUT, enautologout);
    myprops.setBoolean(ENLISTSUMMARY, enlistsummary);
    myprops.setBoolean(ENMERCHREF, enmerchref);
    myprops.setBoolean(ENMODIFY, enmodify);
    myprops.setInt(ENTERPRISEID, enterpriseid.value());
    myprops.setBoolean(FREEPASS, freepass);
    myprops.setString(JAVATZ, tz.getID());
    myprops.setChar(MERCHANTTYPE, merchanttype.Char());
    myprops.setString(MERCHREFLABEL, merchreflabel);
    myprops.setString(PHONE, phone);
    myprops.setBoolean(PUSHDEBIT, pushdebit);
    myprops.setString(RECEIPTABIDE, receiptabide);
    myprops.setString(RECEIPTHEADER, receiptheader);
    myprops.setBoolean(RECEIPTSHOWSIG, receiptshowsig);
    myprops.setString(RECEIPTTAGLINE, receipttagline);
    myprops.setString(RECEIPTTIMEFORMAT, receipttimeformat);
    myprops.setInt(SIGCAPTHRESH, sigcapthresh);
    myprops.setInt(SILIMIT, silimit);
    myprops.setInt(SITOTAL, sitotal);
    myprops.setString(STATE, state);
    myprops.setString(STORENAME, storename);
    myprops.setInt(STORENUMBER, storenumber);
    myprops.setString(ZIPCODE, zipcode);
  }

  // +++ need lists of terminals, storeauths, and storeaccesses
  public ApplianceList appliances = new ApplianceList();

  public String address1          ="";
  public String address2          ="";
  public boolean alwaysid          =false;
  public boolean autoapprove       =false;
  public boolean autoclose         =false;
  public boolean autocomplete      =false;
  public int     autodepositmins   = 0; // minutes since midnight
  public int     autodrawermins    = 0; // minutes since midnight
  public boolean autoquery         =false;
  public boolean checksallowed     =false;
  public String city              ="";
  public String country           ="";
  public boolean creditallowed     =false;
  public boolean debitallowed      =false;
  public int debitpushthreshold=0;
  public boolean enabled           =false;
  public boolean enauthonly        =false;
  public boolean enautodeposit     =false;
  public boolean enautodrawer      =false;
  public boolean enautologout      =false;
  public boolean enlistsummary     =false;
  public boolean enmerchref        =false;
  public boolean enmodify          =false;
  public Enterpriseid enterpriseid      = null;
  public Enterprise enterprise = null;
  public boolean freepass          =false;
  private TimeZone tz = TimeZone.getTimeZone("UTC");
  public MerchantType merchanttype      =new MerchantType();
  public String merchreflabel     ="";
  public String phone             ="";
  public boolean pushdebit         =false;
  public String receiptabide      ="";
  public String receiptheader     ="";
  public boolean receiptshowsig    =false;
  public String receipttagline    ="";
  public String receipttimeformat ="";
  public int sigcapthresh      =0;
  public int silimit           =0;
  public int sitotal           =0;
  public String state             ="";
  public String storename         ="";
  public int storenumber       =0;
  public String zipcode           ="";

  public String toString() {
    return ""+id()+":"+storename;
  }
}

//$Id: Store.java,v 1.21 2004/03/26 19:58:45 mattm Exp $