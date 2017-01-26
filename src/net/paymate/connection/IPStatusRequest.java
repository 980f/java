package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/IPStatusRequest.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.UTC;

public class IPStatusRequest extends AdminRequest {
  String lanip    = "";
  String wanip    = "";
  UTC applianceTime = null;

  // +++ need load and save

  public IPStatusRequest() { // leave for reflective reconstructor
  }
  public ActionType Type(){
    return new ActionType(ActionType.ipstatupdate);
  }
}