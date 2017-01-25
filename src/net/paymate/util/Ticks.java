package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Ticks.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Ticks.java,v 1.2 2001/07/19 01:06:55 mattm Exp $
 */

public class Ticks {

  public static final long perSecond=1000L;
  public static final long perMinute=60*perSecond;
  public static final long perHour=60*perMinute;
  public static final long perDay=24*perHour;

  public static final long forSeconds(int seconds){
    return seconds*perSecond;
  }

  public static final long forSeconds(double seconds){
    return (long)(seconds*((double)perSecond));
  }

  public static final long forMinutes(int minutes){
    return minutes*perMinute;
  }

  public static final long forHours(int hours){
    return hours*perHour;
  }

  public static final long forDays(int days){
    return days*perDay;
  }

}
//$Id: Ticks.java,v 1.2 2001/07/19 01:06:55 mattm Exp $