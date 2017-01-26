package net.paymate.jpos.Terminal;
/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/CardReader.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CardReader.java,v 1.22 2002/07/09 17:51:28 mattm Exp $
*/

import net.paymate.jpos.data.*;
import net.paymate.util.*;

import net.paymate.ivicm.et1K.*;

public class CardReader {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(CardReader.class);

  MSRService device;
  public void joins(MSRService device,QReceiver posterm){
    this.device=device;
    if(device!=null){
      device.setReceiver(posterm);
    }
  }

/**
 * @see MSRService#Flush
 */
  public void Flush() {
    if(device!=null){
      device.Flush();
    }
  }

  /**
 * @see MSRService#Acquire
 */
  public void Acquire() {
    if(device!=null){
      device.Acquire();
    }
  }

  public CardReader(MSRService device,QReceiver posterm) {
    joins(device,posterm);
  }

}

//$Id: CardReader.java,v 1.22 2002/07/09 17:51:28 mattm Exp $
