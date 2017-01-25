/* $Id: ToLog.java,v 1.4 2000/06/04 20:37:34 alien Exp $ */
package net.paymate.connection;

public class ToLog extends MessageReply {
  public ActionType Type(){
    return new ActionType(ActionType.tolog);
  }

  public ToLog(String body){
    super(body);
  }

}
//$Id: ToLog.java,v 1.4 2000/06/04 20:37:34 alien Exp $
