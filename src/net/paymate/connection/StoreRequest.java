package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/StoreRequest.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.data.Terminalid;
import net.paymate.terminalClient.StoreMenu;

public class StoreRequest extends AdminRequest {
//  StoreMenu op;
  public StoreRequest() {
//    needed by fromProperties
  }

  public ActionType Type(){
    return new ActionType(ActionType.store);
  }

  public StoreRequest Deposit(){
    return new StoreRequest();
  }

}
//$Id: StoreRequest.java,v 1.4 2003/10/01 04:23:44 andyh Exp $