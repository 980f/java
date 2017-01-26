/**
* Title:        LineDisplay
* Description:  wrap jpos display device
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LineDisplay.java,v 1.19 2003/07/27 05:35:06 mattm Exp $
*/
package net.paymate.jpos.Terminal;

import net.paymate.lang.StringX;
import net.paymate.util.*;
import net.paymate.ivicm.ec3K.*;
//not a DEService since with no input it has no listeners.

class LineDisplay {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(LineDisplay.class);

  LineDisplayService device;
  public void joins(LineDisplayService device,QReceiver posterm){
    this.device=device;
    if(device!=null){
      device.setReceiver(posterm);//only used for errors.
    }
  }

//  public String toString(){
//    return device.getPhysicalDeviceName();
//  }

//
//  protected void Attach(String id) {
//    fakeit=(Fundamental.Attach(this,this.id=id,DeviceName.LineDisplay)!=null);
//  }
//
//  public JposException  Release() {
//    return Fundamental.Release(this);
//  }
//
  protected String forRefresh="";

  public void refresh(){
    Display(forRefresh);
  }
///////////

  public void Display(String msg){
    if(device!=null){
      //do our own scrolling, don't trigger marquee crap.
      String scrolled= StringX.tail(msg,device.getColumns());
      device.displayText(forRefresh=scrolled);
      dbg.VERBOSE("Should have just displayed: " + scrolled);
    }
  }

  public LineDisplay(LineDisplayService device,QReceiver posterm){
    joins(device,posterm);
  }

}
//$Id: LineDisplay.java,v 1.19 2003/07/27 05:35:06 mattm Exp $
