package net.paymate.database.ours.query;

/**
 * Title:        CardSubtotalsRow
 * Description:  Data structure capable of holding the data from the card subtotals query<p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: CardSubtotalsRow.java,v 1.2 2001/10/17 23:59:00 mattm Exp $
 */

import  java.sql.*;
import  net.paymate.database.*;
import  net.paymate.util.*;
import  net.paymate.awtx.*; // realmoney

public class CardSubtotalsRow extends Query {

  private static final ErrorLogStream dbg = new ErrorLogStream(CardSubtotalsRow.class.getName(), ErrorLogStream.WARNING);

  public RealMoney rawamount(){
    return new RealMoney(sumer);

  }

  private CardSubtotalsRow() {
    this(null);
    //all fields are init'ed to ""
  }

  private CardSubtotalsRow(Statement stmt) {
    super(stmt);
  }

  /**
   * Makes a CardSubtotalsRow that can NOT scroll (just a snapshot of a single record).
   */
  public static final CardSubtotalsRow NewOne(ResultSet rs) {
    CardSubtotalsRow tj = new CardSubtotalsRow();
    tj.fromResultSet(rs);
    return tj;
  }

  /**
   * Makes a CardSubtotalsRow that CAN scroll.
   */
  public static final CardSubtotalsRow NewSet(Statement stmt) {
    return new CardSubtotalsRow(stmt);
  }

  public String paymenttypename = "";
  public String counter = "";
  public String sumer = "";
}
