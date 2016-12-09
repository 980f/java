/* $Id: SigCaptured.java,v 1.5 2000/09/07 03:55:58 andyh Exp $ */
package net.paymate.jpos.Terminal;

import net.paymate.jpos.data.*;

/**
 for passing signature data up an event chain
 */

public class SigCaptured implements Event {
  protected SigData sig;

  public SigData Value(){
    return (sig!=null)? sig: new SigData(); //default constructs trivial signature.
  }

  public SigCaptured(SigData newsigdata){
    sig=newsigdata;
  }

  public EventType Type(){
    return new EventType(EventType.SigAcquired);
  }
}
//$Id: SigCaptured.java,v 1.5 2000/09/07 03:55:58 andyh Exp $
