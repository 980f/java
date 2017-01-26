package net.paymate.authorizer.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PTTransaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.14 $
 */

import net.paymate.authorizer.*; // AuthTransaction
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*;
import net.paymate.data.sinet.business.*;
import net.paymate.terminalClient.PosSocket.paymentech.*;

public class PTTransaction extends AuthTransaction {

  public PTTransaction(TxnRow record, TxnRow original, Storeid storeid,
                       StandinLimit slim, MerchantInfo merch,
                       boolean hostCaptureOnly, PaymentechAuth handler) {
    super(record, original, storeid, slim, merch);
    request  = new PaymentechRequest(hostCaptureOnly, handler);
    response = new PaymentechResponse(hostCaptureOnly);
  }

}

/*
Status level:
socketConnected?
requestSent?
responseReceived?
aborted?

notifyThread

RequestObject

ResponseObject

standin [T/F]

*/
