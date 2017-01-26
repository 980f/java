package net.paymate.database.ours.query;

import net.paymate.database.Query;
import net.paymate.data.*;
import java.sql.*;
import net.paymate.util.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/AuthorizerRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class AuthorizerRow extends Query {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthorizerRow.class, ErrorLogStream.WARNING);

  private AuthorizerRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private AuthorizerRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a AuthorizerRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final AuthorizerRow NewOne(ResultSet rs) {
    AuthorizerRow tj = new AuthorizerRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a AuthorizerRow that CAN scroll.
   */
  public static final AuthorizerRow NewSet(Statement stmt) {
    return new AuthorizerRow(stmt);
  }

  public Authid authid() {
    return new Authid(authid);
  }

  public String authname = "";
  public String authid = "";
  public String authclass = "";
}

// $Id: AuthorizerRow.java,v 1.3 2002/07/09 17:51:25 mattm Exp $
