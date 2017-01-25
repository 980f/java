package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/QActor.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

public interface QActor {
  public void runone(Object fromq);
  public void Stop();
}
//$Id: QActor.java,v 1.2 2001/11/03 00:19:56 andyh Exp $