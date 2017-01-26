package net.paymate.database.ours.query;

import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.data.*; // id's
import  java.util.*; // vector
import  net.paymate.util.*; // Safe

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/TermauthRow.java,v $
 * Description:  Data structure capable of holding the data from the termauths <p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TermauthRow.java,v 1.1 2003/08/01 02:50:46 mattm Exp $
 */

public class TermauthRow extends Query {

  private TermauthRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private TermauthRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a TermauthRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final TermauthRow NewOne(ResultSet rs) {
    TermauthRow tj = new TermauthRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a TermauthRow that CAN scroll.
   */
  public static final TermauthRow NewSet(Statement stmt) {
    return new TermauthRow(stmt);
  }

  public String authid       = "";
  public String authseq      = "";
  public String authtermid   = "";
  public String termauthid   = "";
  public String termbatchnum = "";
  public String terminalid   = "";
}
//$Id: TermauthRow.java,v 1.1 2003/08/01 02:50:46 mattm Exp $
