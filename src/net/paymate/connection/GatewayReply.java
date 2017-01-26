package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/GatewayReply.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.EasyUrlString;

public class GatewayReply extends ActionReply {

  public GatewayReply() {
  }
  public GatewayReply(EasyUrlString origMessage) {
    this.origMessage = origMessage;
  }
  public ActionType Type(){
    return new ActionType(ActionType.gateway);
  }
}