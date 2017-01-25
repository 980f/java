package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ApplianceOptions.java,v $
 * Description: Carrier for appliance temporary options, such as live debug stuff.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ApplianceOptions.java,v 1.3 2001/11/17 00:38:33 andyh Exp $
 */

import net.paymate.util.*;

public class ApplianceOptions implements isEasy {

  public int period=    (int)Ticks.forMinutes(2);   //tix
  public int txnHoldoff=(int)Ticks.forSeconds(61.0);//tix

  final static String periodKey="period";
  final static String txnHoldoffKey="txnHoldoff";

  public EasyCursor saveas(String key,EasyCursor ezc){
    ezc.push(key);
    save(ezc);
    return ezc.pop();
  }

  public void save(EasyCursor ezp){
    if(period!=0) {
      ezp.setInt(periodKey,period);
    }
    ezp.setInt(txnHoldoffKey,txnHoldoff);
  }

  public void load(EasyCursor ezp){
    period= ezp.getInt(periodKey,(int)Ticks.forMinutes(2));
    txnHoldoff=ezp.getInt(txnHoldoffKey,(int)Ticks.forSeconds(61.0));//%%% server must set according to worst authorizer.
  }

  public EasyCursor loadfrom(String key,EasyCursor ezc){
    ezc.push(key);
    load(ezc);
    return ezc.pop();
  }

  public ApplianceOptions() {
  //
  }

}
//$Id: ApplianceOptions.java,v 1.3 2001/11/17 00:38:33 andyh Exp $