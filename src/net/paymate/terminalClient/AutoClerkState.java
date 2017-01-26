package net.paymate.terminalClient;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/AutoClerkState.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.lang.RawEnum;

public class AutoClerkState extends RawEnum {
  public final static int idle=0;
  public final static int awake=1;
  public final static int authing=2;
  public final static int responded=3;

  public static String toString(int value){
    switch (value) {
      case idle:      return "idle";
      case awake:     return "awake";
      case authing:   return "authing";
      case responded: return "responded";
      default: return "Illegal."+value;
    }
  }

  public String toString(){
    return toString(value);
  }

}
//$Id: AutoClerkState.java,v 1.3 2003/07/27 05:35:15 mattm Exp $