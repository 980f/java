package net.paymate.peripheral;

/**
 * Title:        $Source: /cvs/src/net/paymate/peripheral/KeyPad.java,v $
 * Description:  replacement for horridly twisted DisplayPad classes
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.awtx.Question;
import net.paymate.util.QReceiver;

abstract public class KeyPad extends PosPeripheral {

  abstract public void ask(Question q);

/**
 * @param posterm will often be a clerkui rather than a posterm
 */
  public KeyPad(QReceiver posterm) {
    super(posterm);
  }
}