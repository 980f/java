/* $Id: PinCaptured.java,v 1.7 2001/07/22 03:28:57 andyh Exp $ */
package net.paymate.jpos.Terminal;

import net.paymate.jpos.data.*;

public class PinCaptured implements Event {
  //jTermEvent
  public EventType Type(){
    return new EventType(EventType.PinAcquired);
  }

  protected PINData thePIN;

  public PINData Pin(){
    return thePIN;
  }

  public boolean HasData(){
    return thePIN!=null && thePIN.NonTrivial();
  }

  PinCaptured(PINData pd){
    thePIN=pd;
  }

  PinCaptured(){
    thePIN=null;
  }

}
//$Id: PinCaptured.java,v 1.7 2001/07/22 03:28:57 andyh Exp $
