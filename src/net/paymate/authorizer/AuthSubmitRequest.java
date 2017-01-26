package net.paymate.authorizer;

import net.paymate.data.*;
import net.paymate.database.ours.query.*;
import net.paymate.util.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthSubmitRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

public abstract class AuthSubmitRequest {

  // used for getting info out of the database
  public TxnRow records = null;
  public Terminalid terminalid = null;
  public Authid authid = null;
  // used to get stuff and for formatting the message
  public TermAuthid termauthid = null;
  public UTC batchtime = UTC.Now();
  public Accumulator amounts = new Accumulator();
  public int batchseq = 0; // set when batchid is set, is [1-998] % batchseq
  public int termbatchnum = 0;
  public MerchantInfo merch = null;
  protected Batchid batchid = null; // DO NOT unprotect this!  Needed to force simultaneous setting of batchid and batch seq number
  // set and get
  public void setBatchid(Batchid batchid, LongRange range) {
    this.batchid = batchid;
    this.batchseq = (int)((batchid.value() % range.high()) + range.low()); // [1-998, eg]
  }
  public Batchid batchid() {
    return batchid;
  }

  public AuthSubmitRequest(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    this.terminalid = terminalid;
    this.authid = authid;
    this.merch = merch;
  }

  protected abstract int maxRequestSize(); // overload to set the size of the request buffer

  public int txncount() {
    return (int)amounts.getCount();
  }
  public int txntotal() {
    return (int)amounts.getTotal();
  }

  public String toString() {
    return "terminalid="+terminalid+", authid="+authid+", termauthid="+termauthid+
      ", batchtime="+batchtime+", amounts="+amounts+", batchseq="+batchseq+
      ", termbatchnum="+termbatchnum+", merch="+merch+", batchid="+batchid;
  }

}
