/**
* Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/PinEntry.java,v $
* Description:  connection to entuch pinpad
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PinEntry.java,v 1.20 2002/07/09 17:51:29 mattm Exp $
*/
package net.paymate.jpos.Terminal;
import net.paymate.ivicm.et1K.*;

import net.paymate.jpos.data.*;
import net.paymate.awtx.RealMoney;
import net.paymate.util.*;

public class PinEntry {
  static final ErrorLogStream dbg=ErrorLogStream.getForClass(PinEntry.class);
  boolean fakeit;

  PINPadService device;
  public void joins(PINPadService device, QReceiver posterm){
    this.device=device;
    if(device!=null){
      device.setReceiver(posterm);
    }
  }

  public void Release() {
//    device.Release();
  }

  public void Flush(){
//    device.Flush();
  }

  /**
  @param accountNumber required by some pinpads
  @param amt amount of transaction ditto.
  @param isRefund true for refund, false for sale or query
  */
  public boolean Acquire(CardNumber accountNumber, RealMoney amt, boolean isRefund) { //start DUKPT acquisition
    dbg.ERROR("Acquire:"+!fakeit);
    if(!fakeit){
      try {
        device.enablePINEntry(accountNumber.Image(),amt,isRefund);
      }
      catch (Exception ex) {
        dbg.Caught(ex);
        return false;
      }
    }
    return true;
  }

  public PinEntry(PINPadService device, QReceiver posterm) {
    joins(device, posterm);
  }

}
//$Id: PinEntry.java,v 1.20 2002/07/09 17:51:29 mattm Exp $
