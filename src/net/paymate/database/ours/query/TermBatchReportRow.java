package net.paymate.database.ours.query;

import net.paymate.database.Query;
import net.paymate.data.*;
import java.sql.*;
import net.paymate.util.*;
//import java.util.*; // Timezone

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/query/TermBatchReportRow.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class TermBatchReportRow extends Query {

  private TermBatchReportRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a TermBatchReportRow that CAN scroll.
   *
   * @param rs - The resultset for the query that gives
   */
  public static final TermBatchReportRow NewSet(Statement stmt) {
    return new TermBatchReportRow(stmt);
  }

  public Terminalid terminalid() {
    return new Terminalid(terminalid);
  }

  public Batchid batchid() {
    return new Batchid(batchid);
  }

  public String batchtime    = "";
  public String termbatchnum = "";
  public String batchid      = "";
  public String terminalid   = "";
  public String authrespmsg  = "";
}
// $Id: TermBatchReportRow.java,v 1.3 2002/04/04 06:46:46 mattm Exp $
