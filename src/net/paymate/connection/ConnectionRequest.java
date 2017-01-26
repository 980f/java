package net.paymate.connection;

/**
 * Title:        ConnectionRequest
 * Description:  Appliance login request (gets configuration for terminals and appliance)
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: ConnectionRequest.java,v 1.7 2004/02/11 00:23:15 andyh Exp $
 */

import net.paymate.util.*;

public class ConnectionRequest extends UpdateRequest {
  public ActionType Type(){
    return new ActionType(ActionType.connection);
  }

  public ConnectionRequest() {//public only for reflective load.

  }

  public static ConnectionRequest Generate() {
    ConnectionRequest newone=new ConnectionRequest();
    newone.seq=seqgenerator=0;//resetting generator indicates program restart.
    return newone;
  }

}
//$Id: ConnectionRequest.java,v 1.7 2004/02/11 00:23:15 andyh Exp $