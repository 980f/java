package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/SrvrStandinRecord.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */
import net.paymate.ISO8583.data.*;// MOVE THIS ! +++

public class SrvrStandinRecord {

// go to the table to get things just before you send it off, to be sure nothing has changed.
// but this is here to make calculating totals easier

  public long cents = 0;
  public TransactionID tid = null; // to look it up again later.
  public SrvrStandinRecord(TransactionID tid, long cents) {
    this.tid = tid;
    this.cents = cents;
  }
}
