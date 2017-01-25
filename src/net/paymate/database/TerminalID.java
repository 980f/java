package net.paymate.database;

/**
 * Title:
 * Description:  frequently used pair
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: mattm $
 * @version $Id: TerminalID.java,v 1.6 2001/11/17 06:16:58 mattm Exp $
 */

import net.paymate.util.*; // safe

public class TerminalID {
  public static final char sep = '*';

  public int terminalID=0;
  public int storeid;

  public String fullname(){
    return ""+terminalID+sep+storeid; // +++ NEED A SAFE.SUBSTRING FUNCTION!!!!
  }

  public TerminalID(int terminalID, int storeid) {
    this.terminalID=terminalID;
    this.storeid=storeid;
  }

  public TerminalID(String fullname) {
    parseFullname(fullname);
  }

  public TerminalID parseFullname(String fullname) {
    int i = fullname.indexOf(sep);
    if(i == -1) {
      // +++ dbg message
    } else {
      terminalID = Safe.parseInt(fullname.substring(0, i));
      if(fullname.length() > i+1) {
        storeid = Integer.valueOf(fullname.substring(i+1)).intValue();
      }
    }
    return this;
  }

}
//$Id: TerminalID.java,v 1.6 2001/11/17 06:16:58 mattm Exp $
