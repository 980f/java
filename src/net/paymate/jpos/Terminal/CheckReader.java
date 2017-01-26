/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/CheckReader.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.15 $
*/
package net.paymate.jpos.Terminal;
import net.paymate.jpos.data.MICRData;
import net.paymate.util.*;

import net.paymate.ivicm.ec3K.*;

public class CheckReader {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(CheckReader.class);

  MICRService device;
  public void joins(MICRService device,QReceiver posterm){
    this.device=device;
    if(device!=null){
      device.setReceiver(posterm);
    }
  }

  public void Acquire() {
    if(device!=null){
      device.Acquire();
    }
  }

  public CheckReader(MICRService device,QReceiver posterm){
    joins(device,posterm);
  }

}
//$Id: CheckReader.java,v 1.15 2002/07/09 17:51:28 mattm Exp $
