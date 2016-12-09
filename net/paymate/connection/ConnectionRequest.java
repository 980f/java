package net.paymate.connection;

/**
 * Title:        ConnectionRequest
 * Description:  Appliance login request (gets configuration for terminals and appliance)
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: ConnectionRequest.java,v 1.6 2001/10/02 17:06:35 mattm Exp $
 */

import net.paymate.util.*;

public class ConnectionRequest extends UpdateRequest {
  public ActionType Type(){
    return new ActionType(ActionType.connection);
  }

  public ConnectionRequest() {
//
  }

}
//$Id: ConnectionRequest.java,v 1.6 2001/10/02 17:06:35 mattm Exp $