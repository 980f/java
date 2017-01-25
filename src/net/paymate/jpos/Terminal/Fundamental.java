/**
* Title:        Fundamental
* Description:  common usage of baseControls
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Fundamental.java,v 1.3 2001/07/19 01:06:50 mattm Exp $
*/
package net.paymate.jpos.Terminal;

import jpos.*;
import jpos.events.*;

public class Fundamental {
  public static final JposException Attach(BaseControl jposdev, String namer) {
    try {
      jposdev.open(namer);
      //we are a single application system, all devices should always
      //be ready to be claimed.
      jposdev.claim(0);
      //if not claimed+++
      jposdev.setDeviceEnabled(true);
      return null;
    } catch (JposException jape){
      return jape;
    }
  }

  public static final JposException Attach(BaseControl jposdev, String id, String namer) {
    return Attach(jposdev, id+'.'+ namer) ;
  }

  public static final JposException Release(BaseControl jposdev) {
    try {
      jposdev.release();
      jposdev.close();
      return null;
    } catch (JposException jape){
      return jape;
    }
  }
}
//$Id: Fundamental.java,v 1.3 2001/07/19 01:06:50 mattm Exp $
