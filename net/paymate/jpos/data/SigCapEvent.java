package net.paymate.jpos.data;

import jpos.events.DataEvent;
import net.paymate.jpos.data.*;
import net.paymate.jpos.Terminal.*;


/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/SigCapEvent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class SigCapEvent extends DataEvent implements Event {
  public EventType Type(){
    return new EventType(EventType.SigAcquired);
  }

  public SigCapEvent(Object obj, int i) {
    super(obj,i);
  }

}
//$Id: SigCapEvent.java,v 1.1 2001/10/25 19:53:38 andyh Exp $