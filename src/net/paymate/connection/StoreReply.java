package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/StoreReply.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class StoreReply extends AdminReply {

  public StoreReply() { // needed for ActionReply construction
  }
  public StoreReply(boolean succeeded) {
    super();
    setState(succeeded);
  }
  public ActionType Type(){
    return new ActionType(ActionType.store);
  }
}

// $Id: StoreReply.java,v 1.2 2002/09/04 14:44:07 mattm Exp $

