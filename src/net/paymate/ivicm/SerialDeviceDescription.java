package net.paymate.ivicm;

import net.paymate.jpos.DeviceDescription;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/SerialDeviceDescription.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.serial.*;
import net.paymate.util.*;

public class SerialDeviceDescription extends DeviceDescription implements isEasy {
  Parameters serialParams;

  public SerialDeviceDescription(){}//req'd by base class

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.push("portInfo");
    try {
      serialParams.save(ezp);
    }
    finally {
      ezp.pop();
    }
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    try {
      serialParams=new Parameters(name, ezp.push("portInfo"));
    }
    finally {
      ezp.pop();
    }
  }

}
//$Id: SerialDeviceDescription.java,v 1.2 2002/02/11 04:45:23 andyh Exp $