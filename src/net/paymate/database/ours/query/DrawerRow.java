package net.paymate.database.ours.query;

import  java.sql.*;
import  net.paymate.database.*;

/**
 * Title:        $Source $
 * Description:  Data structure capable of holding the data from the drawer table<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DrawerRow.java,v 1.1 2001/11/16 01:34:30 mattm Exp $
 */

public class DrawerRow extends Query {

  private DrawerRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private DrawerRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a DrawerRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final DrawerRow NewOne(ResultSet rs) {
    DrawerRow tj = new DrawerRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a tranjour that CAN scroll.
   */
  public static final DrawerRow NewSet(Statement stmt) {
    return new DrawerRow(stmt);
  }

  public String transtarttime = "";
  public String drawerid = "";
  public String associateName = "";
  public String storeName = "";
  public String terminalName = "";

}
//$Id: DrawerRow.java,v 1.1 2001/11/16 01:34:30 mattm Exp $
