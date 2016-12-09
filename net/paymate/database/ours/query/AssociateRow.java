package net.paymate.database.ours.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/AssociateRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import  net.paymate.ISO8583.factory.Field;
import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.util.*;

public class AssociateRow extends Query {

  private static final ErrorLogStream dbg = new ErrorLogStream(AssociateRow.class.getName(), ErrorLogStream.WARNING);

  private AssociateRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private AssociateRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a StoreInfoRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final AssociateRow NewOne(ResultSet rs) {
    AssociateRow tj = new AssociateRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a StoreInfoRow that CAN scroll.
   */
  public static final AssociateRow NewSet(Statement stmt) {
    return new AssociateRow(stmt);
  }

  public String associateid = "";
  public String loginname = "";
  public String lastname = "";
  public String firstname = "";
  public String middleinitial = "";
  public String enterpriseacl = "";
  public String colorschemeid = "";
}
