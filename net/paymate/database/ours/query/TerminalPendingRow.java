package net.paymate.database.ours.query;

import  java.sql.*;
import  net.paymate.database.*;
import  java.util.*; // vector

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/TerminalPendingRow.java,v $
 * Description:  Data structure capable of holding the data from the terminals pending list<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TerminalPendingRow.java,v 1.4 2001/11/17 06:16:59 mattm Exp $
 */

public class TerminalPendingRow extends Query {

  private TerminalPendingRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private TerminalPendingRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a TerminalPendingRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final TerminalPendingRow NewOne(ResultSet rs) {
    TerminalPendingRow tj = new TerminalPendingRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a tranjour that CAN scroll.
   */
  public static final TerminalPendingRow NewSet(Statement stmt) {
    return new TerminalPendingRow(stmt);
  }

  // the rest is cause I'm lazy, I guess
  private static final int DEFAULTNUM = -1;

  public String enterpriseid = "";
  public String storeid = "";
  public String terminalid = "";
  public String terminalName = "";
  public String storeName = "";
  public String enterpriseName = "";
  public String modelCode = "";
  String lastCloseTime = "";

  int apprCount = DEFAULTNUM;
  long apprAmount = DEFAULTNUM;

  public void lastCloseTime(String val) {
    lastCloseTime = val;
  }
  public void apprCount(int  val) {
    apprCount = val;
  }
  public void apprAmount(long  val) {
    apprAmount = val;
  }
  public String lastCloseTime() {
    return lastCloseTime;
  }
  public int apprCount() {
    return apprCount;
  }
  public long apprAmount() {
    return apprAmount;
  }
}
//$Id: TerminalPendingRow.java,v 1.4 2001/11/17 06:16:59 mattm Exp $
