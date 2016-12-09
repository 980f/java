/**
 * Title:        AdminRequest<p>
 * Description:  Intermediate class for all admin requests<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: AdminRequest.java,v 1.12 2000/06/04 20:37:31 alien Exp $
 */

// user login returns user $ threshold & ACL (offline permissions)

package net.paymate.connection;

import net.paymate.connection.ActionRequest;

public class AdminRequest extends ActionRequest {
  public ActionType Type(){
    return new ActionType(ActionType.admin);
  }
}
//$Id: AdminRequest.java,v 1.12 2000/06/04 20:37:31 alien Exp $
