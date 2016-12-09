/**
* Title:        AppliancesFormat<p>
* Description:  The canned query for the Appliances screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: AppliancesFormat.java,v 1.11 2001/11/17 20:06:37 mattm Exp $
* @todo add 'stale' to table display, perhaps as a visual attribute rather than a column.
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // ApplianceRow
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.page.*; // Acct
import  net.paymate.connection.*;
import  java.sql.*; // resultset
import  java.util.*; // ARRAYS for sorting
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements

public class AppliancesFormat extends RecordFormat {
  private static final ErrorLogStream dbg = new ErrorLogStream(AppliancesFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AppliancesFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[AppliancesFormatEnum.storeCol] = new HeaderDef(AlignType.LEFT   , "Store");
    theHeaders[AppliancesFormatEnum.applianceIdCol] = new HeaderDef(AlignType.LEFT   , "ApplianceID");
    theHeaders[AppliancesFormatEnum.locallyUniqueIdCol] = new HeaderDef(AlignType.LEFT   , "Appliance Time");
    theHeaders[AppliancesFormatEnum.lastLclTimeCol] = new HeaderDef(AlignType.LEFT   , "Last Update");
    theHeaders[AppliancesFormatEnum.diffTimeCol] = new HeaderDef(AlignType.RIGHT   , "* Diff");
    theHeaders[AppliancesFormatEnum.backLog] = new HeaderDef(AlignType.CENTER   , "T/R BL");
    theHeaders[AppliancesFormatEnum.freeMemoryCol] = new HeaderDef(AlignType.RIGHT   , "FreeMem");
    theHeaders[AppliancesFormatEnum.fmPercentCol] = new HeaderDef(AlignType.RIGHT   , "%");
    theHeaders[AppliancesFormatEnum.totalMemoryCol] = new HeaderDef(AlignType.RIGHT   , "TtlMem");
    theHeaders[AppliancesFormatEnum.activeCountCol] = new HeaderDef(AlignType.RIGHT   , "Threads");
    theHeaders[AppliancesFormatEnum.activeAlarmsCountCol] = new HeaderDef(AlignType.RIGHT   , "Alarms");
    theHeaders[AppliancesFormatEnum.revisionCol] = new HeaderDef(AlignType.LEFT   , "Revision");
    theHeaders[AppliancesFormatEnum.terminalsCol] = new HeaderDef(AlignType.LEFT   , "Terminals");
  }

  ApplianceRow appliance = null;
  LocalTimeFormat ltf = null;
  // +++ make this next one come from some combination of appliance/terminal or system parameters:
  private static final long MAXTIMEDIFF = Ticks.forMinutes(5);
  private PayMateDB db = null; // this is so we can get the rest of the stuff.  It can't be packed into one nice query.  :(  We can fix it better when we have our own tables.

  public AppliancesFormat(LoginInfo linfo, ApplianceRow appliance, String title, PayMateDB db) {
//    super(title, linfo.colors, theHeaders, null, -1, null);
    super(linfo.colors, title, appliance, null, -1, null, linfo.ltf);
    this.appliance = appliance;
    this.db = db;
    ltf = linfo.ltf;
    // add the server jar info to the revision header ++++ -> IF this user is a gawd?  when to show revisions at all?  filter this stuff for non-gawds?
    String revision = net.paymate.Revision.Version();
    if(Safe.NonTrivial(revision)) {
      HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
      System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
      myHeaders[AppliancesFormatEnum.revisionCol].title = new StringElement("Rev [" + revision + "]");
      headers = myHeaders;
    }
  }

// @@@ +++ implement this ...
//  private static Element styleElement(String value, boolean boldit) {
//    return (boldit ? ((Element)(new B(value))) : ((Element)(new StringElement(value))));
//  }

  int count = 0;

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      if(tgr != null) {
        count++;
        long clockSkew = ApplianceTracker.clockskew(appliance.rptApplTime(), appliance.rptTime());
        boolean outOfPhase = (clockSkew > Ticks.forMinutes(5)); // 5 minutes is max it can be off before it shows up
        boolean stale = ApplianceTracker.isStale(appliance.applname); //%%% add to display.
        setColumn(AppliancesFormatEnum.applianceIdCol, appliance.applname);
        setColumn(AppliancesFormatEnum.locallyUniqueIdCol, ltf.format(new java.util.Date(appliance.rptApplTime())));
        setColumn(AppliancesFormatEnum.lastLclTimeCol, ltf.format(new java.util.Date(appliance.rptTime())));
        setColumn(AppliancesFormatEnum.diffTimeCol, ""+((clockSkew > MAXTIMEDIFF) ? "* " : "") + Safe.millisToTime(clockSkew));
        //return styleElement(str, stale || outOfPhase);
        setColumn(AppliancesFormatEnum.freeMemoryCol, appliance.rptFreeMem);
        setColumn(AppliancesFormatEnum.fmPercentCol, ""+(appliance.rptFreeMem() * 100 / appliance.rptTtlMem()));
        setColumn(AppliancesFormatEnum.totalMemoryCol, appliance.rptTtlMem);
        setColumn(AppliancesFormatEnum.activeCountCol, appliance.rptThreadCount);
        setColumn(AppliancesFormatEnum.activeAlarmsCountCol, appliance.rptAlarmCount);
        setColumn(AppliancesFormatEnum.revisionCol, appliance.rptRevision);
        setColumn(AppliancesFormatEnum.backLog, appliance.rptStoodTxn+"/"+appliance.rptStoodRcpt);
        setColumn(AppliancesFormatEnum.storeCol, appliance.storename);
        setColumn(AppliancesFormatEnum.terminalsCol, db.getTerminalsForAppliance(appliance.applname));
      } else {
        dbg.WARNING("RecordFormat.next() returned null!");
      }
    } catch (Exception t2) {
      dbg.Caught("generating next row content",t2);
    } finally {
      dbg.Exit();
      return (tgr == null) ? null : this;
    }
  }

  protected int footerRows() {
    return 1;
  }

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case AppliancesFormatEnum.applianceIdCol: {
          ret = "TOTALS:" + count;
        } break;
// @@@ +++ add up the standins on all of the systems
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }
}
//$Id: AppliancesFormat.java,v 1.11 2001/11/17 20:06:37 mattm Exp $
