package net.paymate.authorizer.linkpoint;

import net.paymate.authorizer.*; // AuthTransaction
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.data.*;
import net.paymate.data.sinet.business.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/linkpoint/LPTransaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

public class LPTransaction extends AuthTransaction {

  /* package */ boolean useXML = true;

  public LPTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch, LinkpointAuthorizer auth) {
    super(record, original, storeid, slim, merch);
    request  = /*useXML ? new LinkpointXMLAuthRequest(auth) :*/ new LinkpointAuthRequest(auth.getLive(), auth.getPath2files(), auth.getKeyfilebase(), auth.getCertfilebase());
    response = /*useXML ? new LinkpointXMLAuthResponse()    :*/ new LinkpointAuthResponse();
  }
}