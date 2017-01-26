package net.paymate.io;

import java.util.*;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/io/StreamEventListener.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2001</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public abstract interface StreamEventListener extends EventListener {

  public abstract void notify(EventObject object);

}