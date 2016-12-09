/* $Id: Jape.java,v 1.5 2000/06/04 20:37:35 alien Exp $ */
package net.paymate.jpos.Terminal;

import jpos.JposException;
import jpos.JposConst;

public class Jape implements Event {
  public EventType Type(){
    return new EventType(EventType.Jape);
  }

  public int jape;
  public int morejape;

  public Jape(int theJape){
    jape= theJape;
    morejape=0;
  }

  public Jape(jpos.JposException thejape){
    jape      = thejape.getErrorCode();
    morejape  = (jape==JposConst.JPOS_E_EXTENDED)? thejape.getErrorCodeExtended():0;
  }

}
//$Id: Jape.java,v 1.5 2000/06/04 20:37:35 alien Exp $
