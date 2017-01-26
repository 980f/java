package net.paymate.database.ours.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/AuthAttemptRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.util.*;
import  net.paymate.data.*;

public class AuthAttemptRow extends Query {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthAttemptRow.class, ErrorLogStream.WARNING);

  private AuthAttemptRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes an AuthAttemptRow that CAN scroll.
   */
  public static final AuthAttemptRow NewSet(Statement stmt) {
    return new AuthAttemptRow(stmt);
  }

  public String authattemptid = "";
  public String authendtime = "";
  public String authname = "";
  public String authrequest = "";
  public String authresponse = "";
  public String authstarttime = "";
  public String terminalid = "";
  public String txnid = "";

}
