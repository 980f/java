package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ItemEntry.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class ItemEntry extends ItemAction {//will fix hierarchy names later...
  String image;

  public ItemEntry(int msgid,String image) {
    super(msgid,new ClerkEvent(ClerkEvent.Enter));
    this.image=image;
  }

  public String toString(){
    return "["+msgid+"]= "+image;
  }

}