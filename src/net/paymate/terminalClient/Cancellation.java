package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/Cancellation.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class Cancellation extends ItemAction {
  public Cancellation(int msgid) {
    super(msgid,new ClerkEvent(ClerkEvent.Cancel));
  }
}