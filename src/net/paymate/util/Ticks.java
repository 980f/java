package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Ticks.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Ticks.java,v 1.4 2002/11/08 05:06:29 mattm Exp $
 */

// +++ create a date/time class (UTC?) and merge the Ticks.java class with it, renaming the ticks stuff with millis or ms.

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

  // should be longs?
  public static final int toIntSeconds(long ticks) { // truncates for now
    return (int)(ticks/perSecond);
  }

}
//$Id: Ticks.java,v 1.4 2002/11/08 05:06:29 mattm Exp $