/**
* Title:        DebugCommand
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DebugCommand.java,v 1.1 2000/08/31 21:48:49 andyh Exp $
*/
package net.paymate.terminalClient;

public class DebugCommand extends ClerkCommand {
  public DebugOp debop;

  public DebugCommand(int dbgop) {
    super(new ClerkEvent(ClerkEvent.Debug));
    debop=new DebugOp(dbgop);
  }

}
//$Id: DebugCommand.java,v 1.1 2000/08/31 21:48:49 andyh Exp $
