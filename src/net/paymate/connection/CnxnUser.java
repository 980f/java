package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/CnxnUser.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface CnxnUser {
//  public int fgtimeout();
  public void processReply(Action action,boolean inBackground);
}
//$Id: CnxnUser.java,v 1.2 2001/11/17 00:38:33 andyh Exp $