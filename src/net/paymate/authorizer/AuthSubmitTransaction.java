package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthSubmitTransaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.lang.ReflectX;

public class AuthSubmitTransaction extends AuthorizerTransaction {

  public AuthSubmitRequest request = null;
  public AuthSubmitResponse response = null;
  public TxnRow records = null;
  public boolean auto = false;

  public AuthSubmitTransaction() {
  }

  public String toString() {
    return ReflectX.shortClassName(this)+": batch# "+request.batchid() +
        "\nrequest=" + request + "\nresponse=" + response +
        "\nsocketOpenAttemptCount = " + socketOpenAttempts.value()+"]";
  }
}
