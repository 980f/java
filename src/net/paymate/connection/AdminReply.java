/**
 * Title:        ActionReply<p>
 * Description:  Reply to an ActionRequest<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: AdminReply.java,v 1.13 2003/04/03 00:06:07 andyh Exp $
 */

package net.paymate.connection;
import net.paymate.util.*;

public class AdminReply extends ActionReply {
  public ActionType Type(){
    return new ActionType(ActionType.admin);
  }
  protected boolean offline=false; //done while in standin, always false from server
  public boolean isOffline(){ //done while in standin, always false from server
    return offline;
  }
  public boolean setOffline(boolean isoffline){
    try {
      return offline;//return current state
    }
    finally {
      offline=isoffline;//set new state.
    }
  }
}
//$Id: AdminReply.java,v 1.13 2003/04/03 00:06:07 andyh Exp $
