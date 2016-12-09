/* $Id: CheckScanned.java,v 1.6 2000/08/02 01:36:59 andyh Exp $ */
package net.paymate.jpos.Terminal;

import net.paymate.jpos.data.*;

public class CheckScanned implements Event{

  public EventType Type(){
    return new EventType(EventType.CheckAcquired);
  }

  protected MICRData theScan;
  public MICRData Value(){
    return new MICRData(theScan);
  }

  CheckScanned(MICRData ascan){
    theScan= ascan;
  }

}
//$Id: CheckScanned.java,v 1.6 2000/08/02 01:36:59 andyh Exp $
