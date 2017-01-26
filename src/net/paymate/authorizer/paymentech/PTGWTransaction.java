package net.paymate.authorizer.paymentech;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/paymentech/PTGWTransaction.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.authorizer.*;
import net.paymate.data.Terminalid;

public class PTGWTransaction extends GatewayTransaction {

  // the terminalid is for PTGW(mostly netconnect)!
  public PTGWTransaction(byte [] bytes, PaymentechAuth auth, String potentialGWTID) {
    super(potentialGWTID);
    request  = new PTGatewayRequest(bytes, auth);
    response = new PTGatewayResponse();
  }
}