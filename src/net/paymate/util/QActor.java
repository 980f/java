package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/QActor.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public interface QActor {
  public void runone(Object fromq);//called each time an object is pulled from a q for processing.
  public void Stop();//called when QAgent has stopped.
}
//$Id: QActor.java,v 1.3 2003/10/01 04:23:45 andyh Exp $