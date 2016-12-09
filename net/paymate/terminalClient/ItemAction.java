package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ItemAction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class ItemAction extends ClerkCommand {
  int msgid;
  public ClerkItem ClerkItem(){
    return new ClerkItem(msgid);
  }
  public ItemAction(int msgid,ClerkEvent type) {
    super(type);
    this.msgid=msgid;
  }

}