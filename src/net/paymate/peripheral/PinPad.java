package net.paymate.peripheral;

/**
 * Title:        $Source: /cvs/src/net/paymate/peripheral/PinPad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.awtx.RealMoney;//misplaced class
import net.paymate.util.QReceiver;

import net.paymate.terminalClient.*;//to get pieces for onCancel

public abstract class PinPad extends PosPeripheral{

  /**
   * FYI: @return what we think cardholder gets prompted
   */
//  abstract public PinPad prompt(String s);

  /**
   * @return this, after starting acquisition.
   */
  abstract public PinPad Acquire(PinRequest pinreq);

  protected void postCancel(){
    if(posterm!=null){
      posterm.Post(Cancellation.forClerkItem(ClerkItem.NeedPIN));
    }
  }

  public PinPad(QReceiver posterm) {
    super(posterm);
  }
}
//$Id: PinPad.java,v 1.3 2003/02/13 02:30:39 andyh Exp $