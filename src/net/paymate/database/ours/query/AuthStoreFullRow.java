package net.paymate.database.ours.query;

import net.paymate.database.Query;
import net.paymate.data.*;
import java.sql.*;
import net.paymate.util.*;
import java.util.*; // Timezone
import net.paymate.data.sinet.business.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/AuthStoreFullRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

public class AuthStoreFullRow extends Query {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthStoreFullRow.class, ErrorLogStream.WARNING);

  private AuthStoreFullRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private AuthStoreFullRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a AuthStoreFullRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final AuthStoreFullRow NewOne(ResultSet rs) {
    AuthStoreFullRow tj = new AuthStoreFullRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a AuthStoreFullRow that CAN scroll.
   */
  public static final AuthStoreFullRow NewSet(Statement stmt) {
    return new AuthStoreFullRow(stmt);
  }

  public Authid authid() {
    return new Authid(authid);
  }

  public Storeid storeid() {
    return new Storeid(storeid);
  }

  public String authname = "";
  public String authid = "";
  public String storeid = "";
}

// $Id: AuthStoreFullRow.java,v 1.5 2003/10/29 08:33:17 mattm Exp $
