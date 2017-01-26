package net.paymate.authorizer.npc;

import net.paymate.authorizer.*; // AuthTransaction
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*;
import net.paymate.data.sinet.business.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCTransaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class NPCTransaction extends AuthTransaction {

  public NPCTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    super(record, original, storeid, slim, merch);
    request = new NPCRequest();
    response = new NPCResponse();
  }
}