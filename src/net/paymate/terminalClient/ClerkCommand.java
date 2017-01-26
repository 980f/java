/**
* Title:        ClerkCommand
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ClerkCommand.java,v 1.4 2001/10/12 04:11:38 andyh Exp $
*/
package net.paymate.terminalClient;

import net.paymate.connection.ClerkIdInfo; //for clerkid

public class ClerkCommand {
  public ClerkEvent kind;//which kind of command

  public ClerkCommand(ClerkEvent ce){
    kind=ce;
  }
  /**
   * illadvised but frequently used:
   */
  public ClerkCommand(int ce){
    kind=new ClerkEvent(ce);
  }


}
//$Id: ClerkCommand.java,v 1.4 2001/10/12 04:11:38 andyh Exp $
