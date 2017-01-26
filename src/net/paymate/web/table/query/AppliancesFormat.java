/**
* Title:        AppliancesFormat<p>
* Description:  The canned query for the Appliances screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: AppliancesFormat.java,v 1.57 2004/01/29 00:21:29 mattm Exp $
* @todo add 'stale' to table display, perhaps as a visual attribute rather than a column.
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.page.*; // Acct
import  net.paymate.net.IPSpec;
import  java.sql.*; // resultset
import  java.util.*; // ARRAYS for sorting
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;
import net.paymate.data.sinet.hardware.*;
import net.paymate.data.sinet.business.*;
import net.paymate.Revision;

public class AppliancesFormat extends UniqueIdArrayFormat implements TableGenRow {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AppliancesFormat.class, ErrorLogStream.WARNING);

  private static HeaderDef[] godheaders;
  private static HeaderDef[] nongodheaders;
  private static boolean[]   onlygodcolumns;

  // gawds get more info than non-gawds.  make the non-gawd headers & content = ""
  static {
    int count = (new AppliancesFormatEnum()).numValues();
    godheaders = godheaders = new HeaderDef[count];
    nongodheaders = new HeaderDef[count];
    onlygodcolumns = new boolean[count];
    setGodColumns(onlygodcolumns);
    setHeaders(godheaders, true);
    setHeaders(nongodheaders, false);
  }

  private static final void setGodColumns(boolean [] onlygodcolumns) {
    // all are false unless set to true
    // true means that a GOD status is required.
    onlygodcolumns[AppliancesFormatEnum.backLog]              = true;
    onlygodcolumns[AppliancesFormatEnum.freeMemoryCol]        = true;
    onlygodcolumns[AppliancesFormatEnum.fmPercentCol]         = true;
    onlygodcolumns[AppliancesFormatEnum.totalMemoryCol]       = true;
    onlygodcolumns[AppliancesFormatEnum.activeCountCol]       = true;
    onlygodcolumns[AppliancesFormatEnum.activeAlarmsCountCol] = true;
    onlygodcolumns[AppliancesFormatEnum.ipLan]                = true;
    onlygodcolumns[AppliancesFormatEnum.ipWan]                = true;
    onlygodcolumns[AppliancesFormatEnum.ipAppTime]            = true;
    onlygodcolumns[AppliancesFormatEnum.ipSrvTime]            = true;
  }

  private static final void setHeaders(HeaderDef [] theHeaders, boolean isagod) {
    theHeaders[AppliancesFormatEnum.storeCol]             =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("Store", isagod, AppliancesFormatEnum.storeCol));
    theHeaders[AppliancesFormatEnum.applianceIdCol]       =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("ApplName", isagod, AppliancesFormatEnum.applianceIdCol));
    theHeaders[AppliancesFormatEnum.srvrCnxnTimeCol]      =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("Last Connect", isagod, AppliancesFormatEnum.srvrCnxnTimeCol));
    theHeaders[AppliancesFormatEnum.applClockDriftCol]    =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("Drift", isagod, AppliancesFormatEnum.applClockDriftCol));
    theHeaders[AppliancesFormatEnum.lastLclTimeCol]       =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("Last Update", isagod, AppliancesFormatEnum.lastLclTimeCol));
    theHeaders[AppliancesFormatEnum.diffTimeCol]          =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("Latency", isagod, AppliancesFormatEnum.diffTimeCol));
    theHeaders[AppliancesFormatEnum.backLog]              =
        new HeaderDef(AlignType.CENTER, onlyIfIsaGod("T/R BL", isagod, AppliancesFormatEnum.backLog));
    theHeaders[AppliancesFormatEnum.freeMemoryCol]        =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("Free", isagod, AppliancesFormatEnum.freeMemoryCol));
    theHeaders[AppliancesFormatEnum.fmPercentCol]         =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("%", isagod, AppliancesFormatEnum.fmPercentCol));
    theHeaders[AppliancesFormatEnum.totalMemoryCol]       =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("TtlMem", isagod, AppliancesFormatEnum.totalMemoryCol));
    theHeaders[AppliancesFormatEnum.activeCountCol]       =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("Threads", isagod, AppliancesFormatEnum.activeCountCol));
    theHeaders[AppliancesFormatEnum.activeAlarmsCountCol] =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("Timers", isagod, AppliancesFormatEnum.activeAlarmsCountCol));
    theHeaders[AppliancesFormatEnum.terminalsCol]         =
        new HeaderDef(AlignType.RIGHT , onlyIfIsaGod("Terminals", isagod, AppliancesFormatEnum.terminalsCol));
    theHeaders[AppliancesFormatEnum.ipLan]                =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("LAN IP", isagod, AppliancesFormatEnum.ipLan));
    theHeaders[AppliancesFormatEnum.ipWan]                =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("WAN IP", isagod, AppliancesFormatEnum.ipWan));
    theHeaders[AppliancesFormatEnum.ipAppTime]            =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("IP ApplTime", isagod, AppliancesFormatEnum.ipAppTime));
    theHeaders[AppliancesFormatEnum.ipSrvTime]            =
        new HeaderDef(AlignType.LEFT  , onlyIfIsaGod("IP Srvr Time", isagod, AppliancesFormatEnum.ipSrvTime));
    String revision = Revision.Version();
    String revisionheader = StringX.NonTrivial(revision) && isagod ? "Rev [" + revision + "]" : "Revision";
    theHeaders[AppliancesFormatEnum.revisionCol]          =
        new HeaderDef(AlignType.LEFT  , revisionheader);
  }

  private static final String onlyIfIsaGod(String before, boolean isagod, int index) {
   return okay(isagod, index) ? before : "";
  }

  private static final boolean okay(boolean isagod, int index) {
    boolean okay = true;
    boolean requires = onlygodcolumns[index];
    return isagod || !requires;
  }

  LocalTimeFormat ltf = null;
  // +++ make this next one come from some combination of appliance/terminal or system parameters:
  private static final long MAXDRIFTDIFF = Ticks.forMinutes(5);
  private static final long MAXLATENCY   = Ticks.forSeconds(7);
  public static final String APPLID = "applid";
  private PayMateDB db = PayMateDBDispenser.getPayMateDB();
  private boolean isagawd = false;
  private String mystoreidstr = "";
  private Applianceid myapplianceid = null;
  public AppliancesFormat(LoginInfo linfo, Applianceid [ ] appliances, String title) {
    super(appliances, title, linfo, null, linfo.isaGod() ? godheaders : nongodheaders);
    ltf = linfo.ltf();
    isagawd = linfo.isaGod();
    mystoreidstr = linfo.store.storeId().toString();
  }

  private static Element styleElement(String value, boolean stale, boolean wayOff) {
    Element common=styleElement(value, stale);
    return wayOff ? (new I(common)) : common;
  }

  private static Element styleElement(String value, boolean stale) {
    return stale ? ((Element)new B(value)) : ((Element)new StringElement(value));
  }

  private static Element styleLink(String value, boolean stale, String url) {
    return new A(url, stale ? ((Element)new B(value)) : ((Element)new StringElement(value)));
  }

  Counter standins = new Counter();
  Counter receipts = new Counter();
  Accumulator latencies = new Accumulator();
  Accumulator clockdrifts = new Accumulator();

  public TableGenRow nextRow() {
    currentRow++;
    applid = (Applianceid)ids[currentRow];
    tl = db.getTerminalsForAppliance(applid, isagawd /* withids */);
    ismine = applid.equals(myapplianceid);
    appliance = ApplianceHome.Get(applid);
    if(appliance != null) {
      net = appliance.lastUdpUpdate;
      cnxn = appliance.lastPgmConnection;
      upd = appliance.mostRecentPgmStatus();
      justupd = appliance.lastPgmUpdateOnly;
      if(upd != null) { // only add these if they are greater than 0
        if(upd.stoodtxn > 0) {
          standins.chg(upd.stoodtxn);
        }
        if(upd.stoodrcpt > 0) {
          receipts.chg(upd.stoodrcpt);
        }
      }
      latency = appliance.clockskewPgmUPDT();
      lags= latency<0;
      absLatency = Math.abs(latency);
      // this is clock drift, not latency
      clockDrift = appliance.clockskewPgmCNXN();
      absdrift = Math.abs(clockDrift);
      stale = appliance.isStale() && appliance.track; //+++ add to display.
    } else {
      net = null;
      cnxn = null;
      upd = null;
      latency = 0;
      lags= false;
      absLatency = 0;
      clockDrift = 0;
      absdrift = 0;
      stale = false;
    }
    latencies.add(absLatency);
    wayOff = absLatency > MAXLATENCY; // max it can be off before it gets emphasized
    driftlags= clockDrift<0;
    clockdrifts.add(absdrift);
    driftwayOff = absdrift > MAXDRIFTDIFF; // max it can be off before it gets emphasized
    return this;
  }

  Appliance appliance = null;
  boolean ismine = false;
  Applianceid applid = null;
  TextList tl = null; // terminals
  ApplNetStatus net = null;
  ApplPgmStatus cnxn = null;
  ApplPgmStatus upd = null;
  ApplPgmStatus justupd = null;
  long latency = 0;
  boolean lags= false;
  long absLatency = 0;
  boolean wayOff = false;
  long clockDrift = 0;
  boolean driftlags= false;
  long absdrift = 0;
  boolean driftwayOff = false;
  boolean stale = false;

  public Element column(int col) {
    switch(col) {
      case AppliancesFormatEnum.applianceIdCol: {
        String url = Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.appliance)).Image() + "&"+APPLID+"=" + applid;
        String applname = (isagawd ? appliance.applname+":" : "") + applid;
        return StringX.NonTrivial(mystoreidstr) ? styleLink(applname, stale, url) : styleElement(applname, stale);
      } // break;
      case AppliancesFormatEnum.lastLclTimeCol: {
        return styleElement((justupd != null) ? ltf.format(justupd.srvrtime) : "", stale);
      } // break;
      case AppliancesFormatEnum.diffTimeCol: {
        return styleElement((lags?'-':'+') +DateX.millisToTime(absLatency), stale, wayOff);
      } // break;
      case AppliancesFormatEnum.freeMemoryCol: {
        return styleElement(detail(AppliancesFormatEnum.freeMemoryCol, (upd == null) ? "" : Formatter.sizeLong(upd.freemem)), stale);
      } // break;
      case AppliancesFormatEnum.fmPercentCol: {
        return styleElement(detail(AppliancesFormatEnum.fmPercentCol, (upd == null) ? "" : ""+(int)MathX.ratio(upd.freemem * 100, upd.ttlmem)), stale);
      } // break;
      case AppliancesFormatEnum.totalMemoryCol: {
        return styleElement(detail(AppliancesFormatEnum.totalMemoryCol, (upd == null) ? "" : Formatter.sizeLong(upd.ttlmem)), stale);
      } // break;
      case AppliancesFormatEnum.activeCountCol: {
        return styleElement(detail(AppliancesFormatEnum.activeCountCol, (upd == null) ? "" : ""+upd.threadcount), stale);
      } // break;
      case AppliancesFormatEnum.activeAlarmsCountCol: {
        return styleElement(detail(AppliancesFormatEnum.activeAlarmsCountCol, (upd == null) ? "" : ""+upd.alarmcount), stale);
      } // break;
      case AppliancesFormatEnum.revisionCol: {
        return styleElement((upd != null) ? upd.revision : "", stale);
      } // break;
      case AppliancesFormatEnum.backLog: {
        return styleElement(detail(AppliancesFormatEnum.backLog, (upd != null) ? Formatter.ratioText(" ",upd.stoodtxn, upd.stoodrcpt) : ""), stale);
      } // break;
      case AppliancesFormatEnum.storeCol: {
        return styleElement(((appliance.store != null) ? appliance.store.storename : "")+ (isagawd ? ":"+appliance.storeid : ""), stale);
      } // break;
      case AppliancesFormatEnum.terminalsCol: {
        return styleElement(isagawd ? tl.asParagraph(BRLF.toString()) : tl.asParagraph(", "), stale);
      } // break;
      case AppliancesFormatEnum.ipLan: {
        return styleElement(detail(AppliancesFormatEnum.ipLan, (net != null) ? net.lanip.toString() : ""), stale);
      } // break;
      case AppliancesFormatEnum.ipWan: {
        return styleElement(detail(AppliancesFormatEnum.ipWan, (net == null) ? "" : net.wanip.toString()), stale);
      } // break;
      case AppliancesFormatEnum.ipAppTime: {
        return styleElement(detail(AppliancesFormatEnum.ipAppTime, (net == null) ? "" : ltf.format(net.appltime)), stale);
      } // break;
      case AppliancesFormatEnum.ipSrvTime: {
        return styleElement(detail(AppliancesFormatEnum.ipSrvTime, (net == null) ? "" : ltf.format(net.srvrtime)), stale);
      } // break;
      case AppliancesFormatEnum.srvrCnxnTimeCol: {
        return styleElement((cnxn == null) ? "" : ltf.format(cnxn.srvrtime), stale);
      } // break;
      case AppliancesFormatEnum.applClockDriftCol: {
        return styleElement((driftlags?'-':'+') +DateX.millisToTime(absdrift), stale, driftwayOff);
      } // break;
      default: {
        return new StringElement();
      }
    }
  }

  // if a header is trivial, the detail() function will make
  // the details trivial, which will fold to no space used,
  // despite the fact that the column is really there.
  private final String detail(int col, String it) {
    return onlyIfIsaGod(it, isagawd, col); //StringX.NonTrivial(headers[col]) ? it : "";
  }

  protected int footerRows() {
    return 1;
  }

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case AppliancesFormatEnum.applianceIdCol: {
          ret = "TOTALS:" + ids.length;
        } break;
        case AppliancesFormatEnum.backLog: {
          ret = detail(AppliancesFormatEnum.backLog, Formatter.ratioText(" ",standins.value(),receipts.value()));
        } break;
        case AppliancesFormatEnum.diffTimeCol: {
          ret = "~"+latencies.getAverage();
        } break;
        case AppliancesFormatEnum.applClockDriftCol: {
          ret = "~"+clockdrifts.getAverage();
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }
}
//$Id: AppliancesFormat.java,v 1.57 2004/01/29 00:21:29 mattm Exp $
