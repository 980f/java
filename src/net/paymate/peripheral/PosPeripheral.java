package net.paymate.peripheral;

/**
 * Title:        $Source: /cvs/src/net/paymate/peripheral/PosPeripheral.java,v $
 * Description:  a peripheral that talks to a PosTerminal
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.QReceiver;

public abstract class PosPeripheral {
  protected QReceiver posterm;

  /**
   * public for debug purpose, for faking input from device!
   */
  public boolean Post(Object arf){
    return posterm!=null && posterm.Post(arf);
  }
/**
 * @param beon if true enables device.
 * this is advisory, device doesn't have to support enabling
 * @return this.
 */
  abstract public PosPeripheral setEnable(boolean beon);
  /**
   * @param posterm is who to send swipes to
   * @return this
   */
  protected PosPeripheral sendTo(QReceiver posterm){
    this.posterm=posterm;
    return this;
  }

  protected PosPeripheral(QReceiver posterm) {
    sendTo(posterm);
  }
}
//$Id: PosPeripheral.java,v 1.2 2003/06/17 16:06:30 andyh Exp $