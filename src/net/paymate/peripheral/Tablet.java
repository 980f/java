package net.paymate.peripheral;

/**
 * Title:        $Source: /cvs/src/net/paymate/peripheral/Tablet.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */


import net.paymate.util.*;

abstract public class Tablet extends PosPeripheral {

  public Tablet(QReceiver posterm) {
    super(posterm);
  }

  /**
   * @return whether tablet is in usable condition
   */
  public boolean isFunctional(){
    return false;
  }

  public abstract PosPeripheral setEnable(boolean beon) ;

}
//$Id: Tablet.java,v 1.1 2003/01/21 16:16:51 andyh Exp $