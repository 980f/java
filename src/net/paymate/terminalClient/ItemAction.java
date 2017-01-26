package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ItemAction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public class ItemAction extends ClerkCommand {
//  int msgid;
  ClerkItem item;
  public ClerkItem ClerkItem(){
    return item;
  }
  public ItemAction(int msgid,ClerkEvent type) {
    super(type);
    this.item=new ClerkItem(msgid);
  }
  public String toString(){
    return item.Image();
  }

}