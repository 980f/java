package net.paymate.ivicm.et1K;

import net.paymate.ivicm.SerialDeviceDescription;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/et1K/Description.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class Description extends SerialDeviceDescription implements isEasy{
//+++ move signature compression here
//+++ add a forms manager. (not yet extracted from PosTerminal)

  public void save(EasyCursor ezp){
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
  }

  public Description() { }//req'd by super base class

}
//$Id: Description.java,v 1.1 2001/07/12 17:05:31 andyh Exp $