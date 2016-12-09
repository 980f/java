package net.paymate.database.ours.query;

import net.paymate.database.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/ApplianceRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import  java.sql.*;
//import  net.paymate.database.*;
import  net.paymate.util.*;

public class ApplianceRow extends Query {

  private static final ErrorLogStream dbg = new ErrorLogStream(ApplianceRow.class.getName(), ErrorLogStream.WARNING);

  private ApplianceRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private ApplianceRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes an ApplianceRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final ApplianceRow NewOne(ResultSet rs) {
    ApplianceRow tj = new ApplianceRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes an ApplianceRow that CAN scroll.
   */
  public static final ApplianceRow NewSet(Statement stmt) {
    return new ApplianceRow(stmt);
  }

  public String applianceid = "";
  public String applname = "";
  public String storeid = "";
  public String storename = "";
  public String rptRevision = "";
  public String rptApplTime = "";
  public String rptTime = "";
  public String rptFreeMem = "";
  public String rptTtlMem = "";
  public String rptThreadCount = "";
  public String rptAlarmCount = "";
  public String rptStoodTxn = "";
  public String rptStoodRcpt = "";
  public long rptApplTime() { // CHAR(14) !!! @@@
    return PayMateDB.tranUTC(rptApplTime).getTime();
  }
  public long rptTime() { // CHAR(14) !!! @@@
    return PayMateDB.tranUTC(rptTime).getTime();
  }
  public long rptFreeMem() {
    return Safe.parseInt(rptFreeMem);
  }
  public long rptTtlMem() {
    return Safe.parseInt(rptTtlMem);
  }
  public long rptThreadCount() {
    return Safe.parseInt(rptThreadCount);
  }
  public long rptAlarmCount() {
    return Safe.parseInt(rptAlarmCount);
  }
  public long rptStoodTxn() {
    return Safe.parseInt(rptStoodTxn);
  }
  public long rptStoodRcpt() {
    return Safe.parseInt(rptStoodRcpt);
  }
}
