/**
* Title:        LineDisplay
* Description:  wrap jpos display device
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LineDisplay.java,v 1.14 2001/10/12 04:11:37 andyh Exp $
*/
package net.paymate.jpos.Terminal;

import jpos.*;
import jpos.events.*;
import jpos.events.DataEvent;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.Safe;

//not a DEService since with no input it has no listeners.
public class LineDisplay extends jpos.LineDisplay  implements jpos.LineDisplayConst {
  private static final ErrorLogStream dbg = new ErrorLogStream(LineDisplay.class.getName());

  boolean fakeit;

  String id;
  public String toString(){
    return id;
  }

  protected void Attach(String id) {
    fakeit=(Fundamental.Attach(this,this.id=id,DeviceName.LineDisplay)!=null);
  }

  public JposException  Release() {
    return Fundamental.Release(this);
  }

  protected String forRefresh="";

  public JposException refresh(){
    return Display(forRefresh);
  }

  public JposException Display(String msg){
    if(fakeit){
      return null;
    }
    try {
      //do our own scrolling, don't trigger marquee crap.
      String scrolled= Safe.tail(msg,getColumns());
      displayTextAt(0,0,forRefresh=scrolled,LineDisplayConst.DISP_DT_NORMAL);
      dbg.VERBOSE("Should have just displayed: " + scrolled);
      return null;
    } catch(jpos.JposException jape){
      dbg.VERBOSE("Exception displaying: " + msg);
      if(jape.getErrorCode()==JposConst.JPOS_E_CLOSED){
        fakeit=true;
        return null;
      }
      return jape;
    }
  }

}
//$Id: LineDisplay.java,v 1.14 2001/10/12 04:11:37 andyh Exp $
