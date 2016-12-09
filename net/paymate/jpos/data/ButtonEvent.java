package net.paymate.jpos.data;

import jpos.events.DataEvent;

/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/data/ButtonEvent.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.jpos.data.*;
import net.paymate.jpos.Terminal.*;

public class ButtonEvent extends DataEvent implements Event {
  public EventType Type(){
    return new EventType(EventType.FormButtonData);
  }

  public ButtonEvent(Object obj, int i) {
    super(obj,  i);
  }

}
//$Id: ButtonEvent.java,v 1.1 2001/10/25 19:53:37 andyh Exp $