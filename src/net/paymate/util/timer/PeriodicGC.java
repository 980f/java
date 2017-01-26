package net.paymate.util.timer;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/timer/PeriodicGC.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.util.*;
import net.paymate.*;

public class PeriodicGC implements TimeBomb{
  Alarmum myperiod;
  static ErrorLogStream dbg;

  public boolean enabled(boolean beenabled){
    try {
      return dbg.levelIs(LogSwitch.ERROR);
    }
    finally {
      dbg.setLevel(beenabled?LogSwitch.VERBOSE:LogSwitch.ERROR);
    }
  }

  public void onTimeout(){
    Main.gc(dbg);//only does it if level is verbose
    Alarmer.reset(myperiod.ticks,myperiod);
  }

  public static PeriodicGC Every(long ticks){
    return new PeriodicGC((int)ticks);
  }

  private PeriodicGC(int periodinticks) {
    if(dbg==null) dbg=ErrorLogStream.getForClass(PeriodicGC.class);
    myperiod=Alarmer.New(periodinticks,this);
  }

}
//$Id: PeriodicGC.java,v 1.7 2003/07/24 17:43:53 andyh Exp $