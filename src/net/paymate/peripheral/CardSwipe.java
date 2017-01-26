package net.paymate.peripheral;

/**
 * Title:        $Source: /cvs/src/net/paymate/peripheral/CardSwipe.java,v $
 * Description:  abstract card reader, sends
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.jpos.data.MSRData;
import net.paymate.util.*;

public abstract class CardSwipe extends PosPeripheral {
  //no special cardswipe features (yet)
  public CardSwipe(QReceiver posterm) {
    super(posterm);
  }
}
//$Id: CardSwipe.java,v 1.2 2003/01/21 16:16:51 andyh Exp $