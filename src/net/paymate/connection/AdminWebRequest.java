/**
 * Title:        AdminWebRequest<p>
 * Description:  Generated from the admin webpages<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: AdminWebRequest.java,v 1.1 2000/10/01 03:21:34 mattm Exp $
 */
package net.paymate.connection;

public class AdminWebRequest extends AdminRequest {

  public ActionType Type(){
    return new ActionType(ActionType.adminWeb);
  }
  //but doesn't add any fields beyond the base

}