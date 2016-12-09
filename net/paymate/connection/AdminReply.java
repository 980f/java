/**
 * Title:        ActionReply<p>
 * Description:  Reply to an ActionRequest<p>
 * Copyright:    2000 PayMate.net<p>
 * Company:      paymate<p>
 * @author       paymate
 * @version      $Id: AdminReply.java,v 1.12 2001/07/06 18:56:36 andyh Exp $
 */

package net.paymate.connection;
import net.paymate.util.*;

public class AdminReply extends ActionReply {
  public ActionType Type(){
    return new ActionType(ActionType.admin);
  }

}
//$Id: AdminReply.java,v 1.12 2001/07/06 18:56:36 andyh Exp $
