package net.paymate.authorizer;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/GatewayTransaction.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.data.Terminalid;

// +++ Make GatewayTransaction and AuthTransaction share a base but not extend each other

public class GatewayTransaction extends AuthTransaction {

  public String potentialGWTID;

  protected GatewayTransaction(String potentialGWTID) {
    super(/*TxnRow*/ null, /*TxnRow*/ null, /*Storeid*/ null, /*StandinLimit*/ null, /*MerchantInfo*/ null);
    this.potentialGWTID = potentialGWTID;
  }

  public boolean isGateway() {
    return true;
  }

  public boolean canStandinAgain() {
    return false;
  }
}