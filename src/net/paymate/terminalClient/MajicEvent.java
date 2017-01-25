package net.paymate.terminalClient;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: MajicEvent.java,v 1.1 2001/02/21 00:11:54 andyh Exp $
 */

public class MajicEvent extends TerminalCommand {
  protected long key;

  public MajicEvent(long key,int tccode){
      super(tccode);
      this.key=key;
  }

  public boolean isEventFor(long key){
    return this.key==key;
  }

}
//$Id: MajicEvent.java,v 1.1 2001/02/21 00:11:54 andyh Exp $