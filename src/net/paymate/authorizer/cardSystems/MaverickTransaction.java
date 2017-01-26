package net.paymate.authorizer.cardSystems;

import net.paymate.authorizer.*; // AuthTransaction, Authorizer
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*;
import net.paymate.data.sinet.business.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/MaverickTransaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import net.paymate.awtx.RealMoney;
public class MaverickTransaction extends AuthTransaction {

  public MaverickTransaction(TxnRow tjr, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch, boolean authNoSettle, Authorizer auth) {
    super(tjr, original, storeid, slim, merch);
    request = new MaverickRequest(authNoSettle);
    response = new MaverickResponse(auth);
  }

}

