package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/IPStatusReply.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class IPStatusReply extends AdminReply {

  public IPStatusReply() {
  }
  public ActionType Type(){
    return new ActionType(ActionType.ipstatupdate);
  }
}