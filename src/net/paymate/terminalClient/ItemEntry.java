package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/ItemEntry.java,v $
 * Description:  enumeration of what something is, plus a string image of its value.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

public class ItemEntry extends ItemAction {//will fix hierarchy names later...
  public String image;

  public ItemEntry(int msgid,String image) {
    super(msgid,new ClerkEvent(ClerkEvent.Enter));
    this.image=image;
  }

  public String toString(){
    return super.toString()+'.'+image;
  }

}
//$Id: ItemEntry.java,v 1.5 2004/02/25 18:39:55 andyh Exp $