package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ApplianceOptions.java,v $
 * Description: Carrier for appliance temporary options, such as live debug stuff.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ApplianceOptions.java,v 1.7 2004/02/10 01:10:41 andyh Exp $
 */

import net.paymate.util.*;

public class ApplianceOptions implements isEasy {

  private int period;   //tix
  private int txnHoldoff;//tix

  public ApplianceOptions setPeriod(int period) { // check for valid value in ApplianceTrackerList
    this.period = period;
    return this;
  }

  public int period() {
    return period;
  }
  /**
   * return true if it was okay
   */
  public boolean validatePeriod(int minTicks) {
    if(period() < minTicks) {
      setPeriod(minTicks);
      return false;
    }
    return true;
  }
  public ApplianceOptions setHoldoff(int tix) {//check for valid value in ApplianceTrackerList
     this.txnHoldoff = tix;
     return this;
   }

  public int txnHoldoff(){//tix
    return txnHoldoff;
  }

  final static String periodKey="period";
  final static String txnHoldoffKey="txnHoldoff";

  public void save(EasyCursor ezp){
    if(period!=0) {//if zero let the loader provide a default value by omitting this field.
      ezp.setInt(periodKey,period);
    }
    ezp.setInt(txnHoldoffKey,txnHoldoff);
  }

  public void load(EasyCursor ezp){//retain existing setting for any missing field...
    period= ezp.getInt(periodKey,period);
    txnHoldoff=ezp.getInt(txnHoldoffKey,txnHoldoff);
  }

  /**
   * as of 20040209 the values herein are what are used when we bootup into standin.
   */
  public ApplianceOptions() {
    period=    (int)Ticks.forMinutes(2);
    txnHoldoff=(int)Ticks.forSeconds(61.0);
  }

}
//$Id: ApplianceOptions.java,v 1.7 2004/02/10 01:10:41 andyh Exp $