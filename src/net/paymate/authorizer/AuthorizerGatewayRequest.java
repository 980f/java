package net.paymate.authorizer;

import net.paymate.database.ours.query.*;
import net.paymate.data.*;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/authorizer/AuthorizerGatewayRequest.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class AuthorizerGatewayRequest extends AuthRequest {

  protected byte [] bytes;

  protected AuthorizerGatewayRequest() {
  }

  public AuthorizerGatewayRequest(byte [] bytes) {
    this.bytes = bytes; // default behavior
  }

  protected int maxRequestSize() {
    return bytes.length;
  }

  public AuthRequest fromRequest(TxnRow tjr, TxnRow original, MerchantInfo merch) {
    return this;
  }

  public byte[] toBytes() {
    return bytes;
  }
  public int compareTo(Object o) {
    return 0; // let super handle it
  }
}


