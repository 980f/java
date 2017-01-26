package net.paymate.jpos.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/Problem.java,v $
 * Description:  Base class for paymate data exceptions.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class Problem {
  String description;

  protected Problem(String description) {
    this.description=description;
  }

  public static Problem Noted(String description) {
    return new Problem(description);
  }

  public String toString(){
    return description;
  }
}
//$Id: Problem.java,v 1.3 2003/07/24 16:47:33 andyh Exp $