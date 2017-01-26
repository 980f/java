/**
 * Title:        $Source: /cvs/src/net/paymate/connection/AdminRequest.java,v $
 * Description:  Intermediate class for all admin requests<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Revision: 1.14 $
 */

// user login returns user $ threshold & ACL (offline permissions)

package net.paymate.connection;

import net.paymate.connection.ActionRequest;

// possibly used for pinging

public class AdminRequest extends ActionRequest {
  public ActionType Type(){
    return new ActionType(ActionType.admin);
  }
}
//$Id: AdminRequest.java,v 1.14 2002/11/14 00:32:03 mattm Exp $
