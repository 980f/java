package net.paymate.net;

/**
 * Title:        $Source: /cvs/src/net/paymate/net/ModemInitPair.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;
public class ModemInitPair implements isEasy {
  public String command;
  public String expect;
  final static String commandKey="command";
  final static String expectKey="expect";
  public void save(EasyCursor ezc){
    ezc.setString(commandKey,command);
    ezc.setString(expectKey,expect);
  }
  public void load(EasyCursor ezc){
    expect=ezc.getString(expectKey,"");
    command=ezc.getString(commandKey,"");
  }
  public ModemInitPair() {
    String command="";
    String expect="";
  }
}
//$Id: ModemInitPair.java,v 1.1 2002/07/03 21:51:46 andyh Exp $