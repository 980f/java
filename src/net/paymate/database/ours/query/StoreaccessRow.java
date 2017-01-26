package net.paymate.database.ours.query;

import net.paymate.database.Query;
import net.paymate.data.*;
import java.sql.*;
import net.paymate.util.*;
import java.util.*; // Timezone

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/StoreaccessRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class StoreaccessRow extends Query {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreaccessRow.class, ErrorLogStream.WARNING);

  private StoreaccessRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private StoreaccessRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a AuthStoreFullRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final StoreaccessRow NewOne(ResultSet rs) {
    StoreaccessRow tj = new StoreaccessRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a AuthStoreFullRow that CAN scroll.
   */
  public static final StoreaccessRow NewSet(Statement stmt) {
    return new StoreaccessRow(stmt);
  }

  public String associateid = "";
  public String enclosedrawer = "";
  public String enreturn = "";
  public String ensale = "";
  public String envoid = "";
  public String storeaccessid = "";
  public String storeid = "";

}

// $Id: StoreaccessRow.java,v 1.1 2003/08/01 02:50:46 mattm Exp $
