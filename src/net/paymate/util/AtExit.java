package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/AtExit.java,v $
 * Description:  +++ possibly replace with <a href="http://java.sun.com/j2se/1.4/docs/api/java/lang/Runtime.html#addShutdownHook(java.lang.Thread)">Runtime.addShutdownHook(java.lang.Thread)</a>
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: AtExit.java,v 1.2 2002/07/09 17:51:34 mattm Exp $
 */

public interface AtExit {

  public void AtExit();
  public boolean IsDown();

}