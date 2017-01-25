/* $Id: ToClerk.java,v 1.4 2000/06/04 20:37:34 alien Exp $ */
package net.paymate.connection;

public class ToClerk extends MessageReply {
  public ActionType Type(){
    return new ActionType(ActionType.toclerk);
  }

  public ToClerk(String body){
    super(body);
  }

}
//$Id: ToClerk.java,v 1.4 2000/06/04 20:37:34 alien Exp $
