package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/InstanceNamer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.lang.StringX;

public class InstanceNamer {

  private String prefix;
  private Counter count = new Counter(); // Counter already has mutexing/synchronizing

  public String Next(){
    return prefix+Ascii.bracket(count.incr());
  }

  public InstanceNamer(String prefix) {
    this.prefix=StringX.OnTrivial(prefix,"Instance");
  }
}
//$Id: InstanceNamer.java,v 1.3 2004/01/09 11:46:07 mattm Exp $