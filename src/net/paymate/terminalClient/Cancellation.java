package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/Cancellation.java,v $
 * Description:  used to send posterm an 'escape' response to the indicated question
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class Cancellation extends ItemAction {
  public Cancellation(int msgid) {
    super(msgid, new ClerkEvent(ClerkEvent.Cancel));
  }

  public static Cancellation forClerkItem(int clerkitem) {
    return new Cancellation(clerkitem);
  }
}
//$Id: Cancellation.java,v 1.3 2003/02/13 02:30:39 andyh Exp $
