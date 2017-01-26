package net.paymate.jpos.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/StifledException.java,v $
 * Description:  for passing back an exception rather than throwing it.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class StifledException extends Problem {
  Exception caught;

  public Exception getNextException(){//see ErrorLogStream for implied interface.
    return caught;
  }

  public StifledException(String detail,Exception caught) {
    super(detail);
    this.caught=caught;
  }

  public StifledException(Exception caught) {
    this(caught.getLocalizedMessage(),caught);
  }

}
//$Id: StifledException.java,v 1.1 2001/12/04 07:01:42 andyh Exp $