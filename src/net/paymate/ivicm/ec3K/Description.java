package net.paymate.ivicm.ec3K;

import net.paymate.ivicm.SerialDeviceDescription;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/ec3K/Description.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class Description extends SerialDeviceDescription implements isEasy{

  public void save(EasyCursor ezp){
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
  }

  public Description() { }//req'd by super base class

}