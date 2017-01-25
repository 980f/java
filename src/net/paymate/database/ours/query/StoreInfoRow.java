package net.paymate.database.ours.query;

/**
 * Title:        StoreInfoRow
 * Description:  Data structure capable of holding the data from the tranjour table<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: StoreInfoRow.java,v 1.4 2001/11/17 06:16:58 mattm Exp $
 */

import  net.paymate.ISO8583.factory.Field;
import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.util.*;

public class StoreInfoRow extends Query {

  private static final ErrorLogStream dbg = new ErrorLogStream(StoreInfoRow.class.getName(), ErrorLogStream.WARNING);

  // from db.genStoresQuery(linfo.enterpriseID)

  private StoreInfoRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private StoreInfoRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a StoreInfoRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final StoreInfoRow NewOne(ResultSet rs) {
    StoreInfoRow tj = new StoreInfoRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a StoreInfoRow that CAN scroll.
   */
  public static final StoreInfoRow NewSet(Statement stmt) {
    return new StoreInfoRow(stmt);
  }

  public String storeid = "";
  public String storename = "";
  public String address1 = "";
  public String address2 = "";
  public String city = "";
  public String state = "";
  public String zipcode = "";
  public String country = "";
  public String EnterpriseName = "";
  public String javatz = "";
  public String storehomepage = "";
}
