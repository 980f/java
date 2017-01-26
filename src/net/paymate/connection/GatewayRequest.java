package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/GatewayRequest.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class GatewayRequest extends ActionRequest {

  public GatewayRequest() {
  }
  public ActionType Type(){
    return new ActionType(ActionType.gateway);
  }
}