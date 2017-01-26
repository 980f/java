package net.paymate.database.ours.query;

import net.paymate.database.Query;
import net.paymate.data.*;
import java.sql.*;
import net.paymate.util.*;
import java.util.*; // Timezone

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/StoreauthRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class StoreauthRow extends Query {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreauthRow.class, ErrorLogStream.WARNING);

  private StoreauthRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private StoreauthRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a AuthStoreFullRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final StoreauthRow NewOne(ResultSet rs) {
    StoreauthRow tj = new StoreauthRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a AuthStoreFullRow that CAN scroll.
   */
  public static final StoreauthRow NewSet(Statement stmt) {
    return new StoreauthRow(stmt);
  }

  public String authid = "";
  public String storeid = "";
  public String authmerchid = "";
  public String institution = "";
  public String maxtxnlimit = "";
  public String paytype = "";
  public String settleid = "";
  public String settlemerchid = "";
  public String storeauthid = "";
}

// $Id: StoreauthRow.java,v 1.1 2003/08/01 02:50:46 mattm Exp $
