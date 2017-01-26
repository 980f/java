package net.paymate.database.ours.query;

import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.data.*; // id's
import  java.util.*; // vector
import  net.paymate.util.*; // Safe

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/TerminalPendingRow.java,v $
 * Description:  Data structure capable of holding the data from the terminals pending list<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TerminalPendingRow.java,v 1.12 2003/08/01 02:50:46 mattm Exp $
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
   * Makes a terminal that CAN scroll.
   */
  public static final TerminalPendingRow NewSet(Statement stmt) {
    return new TerminalPendingRow(stmt);
  }

  // the rest is cause I'm lazy, I guess
  private static final int DEFAULTNUM = -1;

  public String applianceid = "";
  public String terminalid = "";
  public String terminalName = "";
  public String modelCode = "";
  String lastCloseTime = "";
  private String lastTxnTime = "";

  int apprCount = DEFAULTNUM;
  long apprAmount = DEFAULTNUM;

  public Terminalid terminalid() {
    return new Terminalid(terminalid);
  }

  public void lastCloseTime(String val) {
    lastCloseTime = val;
  }
  public void lastTxnTime(String val) {
    lastTxnTime = val;
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
  public String lastTxnTime() {
    return lastTxnTime;
  }
  public int apprCount() {
    return apprCount;
  }
  public long apprAmount() {
    return apprAmount;
  }
}
//$Id: TerminalPendingRow.java,v 1.12 2003/08/01 02:50:46 mattm Exp $
