package net.paymate.database.ours.query;

import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.util.*;
import  net.paymate.data.*;
import net.paymate.lang.StringX;

/**
 * Title:        $Source $
 * Description:  Data structure capable of holding the data from the drawer table<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: BatchesRow.java,v 1.5 2003/10/29 08:33:17 mattm Exp $
 */

public class BatchesRow extends Query {

  private BatchesRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private BatchesRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a DrawerRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final BatchesRow NewOne(ResultSet rs) {
    BatchesRow tj = new BatchesRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a drawer that CAN scroll.
   */
  public static final BatchesRow NewSet(Statement stmt) {
    return new BatchesRow(stmt);
  }

  public boolean failed() {
    return StringX.equalStrings(ActionCode.Declined, actioncode) || StringX.equalStrings(ActionCode.Failed, actioncode);
  }

  public String batchid = "";
  public String batchtime = "";
  public String terminalname = "";
  public String terminalid = "";
  public String txncount = "";
  public String txntotal = "";
  public String authid = "";
  public String authname = "";
  public String actioncode = "";
  public String authrespmsg = "";
  public String batchseq = "";
  public String termbatchnum = "";

}
//$Id: BatchesRow.java,v 1.5 2003/10/29 08:33:17 mattm Exp $
