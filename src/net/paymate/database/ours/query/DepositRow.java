package net.paymate.database.ours.query;

import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.util.*;
import  net.paymate.data.*;

/**
 * Title:        $Source $
 * Description:  Data structure capable of holding the data from the drawer table<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DepositRow.java,v 1.3 2002/12/16 22:19:25 mattm Exp $
 */

public class DepositRow extends Query {

  private DepositRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private DepositRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a DrawerRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final DepositRow NewOne(ResultSet rs) {
    DepositRow tj = new DepositRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a drawer that CAN scroll.
   */
  public static final DepositRow NewSet(Statement stmt) {
    return new DepositRow(stmt);
  }

  private String lastBatchTime = null;

  public void setLastBatchtime(String to) {
    lastBatchTime = to;
  }
  public String getLastBatchtime() {
    return lastBatchTime;
  }

  private static final int DEFAULTNUM = -1;

  int apprCount = DEFAULTNUM;
  long apprAmount = DEFAULTNUM;
  private String lastTxnTime = "";

  public Terminalid terminalid() {
    return new Terminalid(terminalid);
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
  public String lastTxnTime() {
    return lastTxnTime;
  }
  public int apprCount() {
    return apprCount;
  }
  public long apprAmount() {
    return apprAmount;
  }


  public String terminalname = "";
  public String terminalid = "";
  public String authid = "";
  public String authname = "";
  public String termauthid = "";
  public String authtermid = "";
}
//$Id: DepositRow.java,v 1.3 2002/12/16 22:19:25 mattm Exp $
