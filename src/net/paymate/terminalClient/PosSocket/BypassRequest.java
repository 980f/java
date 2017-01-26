package net.paymate.terminalClient.PosSocket;
import net.paymate.connection.ActionRequest;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/BypassRequest.java,v $
 * Description:  get a response bypassing payamte server
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

public class BypassRequest extends ActionRequest {
  public byte [] response;

  public BypassRequest(byte [] response) {
    this.response= response!=null? response: Formatter.NullResponse;
  }

  public static BypassRequest New(byte [] response) {
    return new BypassRequest(response);
  }

  public static BypassRequest New(String bytes) {
    return new BypassRequest(bytes!=null? bytes.getBytes(): null);
  }

}
//$Id: BypassRequest.java,v 1.4 2002/11/18 15:17:06 andyh Exp $