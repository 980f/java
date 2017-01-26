package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.*; // UTC
import net.paymate.data.*;

public abstract class AuthRequest implements Comparable {

  public AuthRequest() {
  }

  public abstract AuthRequest fromRequest(TxnRow tjr, TxnRow original, MerchantInfo merch);
  // for transporting over IP
  public abstract byte [] toBytes();
  protected abstract int maxRequestSize(); // overload to set the size of the request buffer
  public abstract int compareTo(Object o);
    /**
   * test request formatter, somewhat
   */
  public void test(String[] args) {
    TxnRow record= TxnRow.fakeRecord();
    AuthRequest arf=this.fromRequest(record, null, MerchantInfo.fakeOne());
    System.err.println("authrequest.toBytes:"+Ascii.bracket(arf.toBytes()));//#in a main()
  }

}
