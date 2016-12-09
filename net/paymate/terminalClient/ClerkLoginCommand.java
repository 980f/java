/**
* Title:        ClerkLoginCommand
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ClerkLoginCommand.java,v 1.1 2000/08/31 21:48:49 andyh Exp $
*/
package net.paymate.terminalClient;
import net.paymate.connection.ClerkIdInfo;

public class ClerkLoginCommand extends ClerkCommand {
  public ClerkIdInfo cid;
  public ClerkLoginCommand(ClerkIdInfo cid) {
    super(new ClerkEvent(ClerkEvent.Login));
    this.cid=cid;
  }

}
//$Id: ClerkLoginCommand.java,v 1.1 2000/08/31 21:48:49 andyh Exp $
