package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/AtExit.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: AtExit.java,v 1.1 2001/05/30 02:43:24 mattm Exp $
 */

public interface AtExit {

  public void AtExit();
  public boolean IsDown();

}