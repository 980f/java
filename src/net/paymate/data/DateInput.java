package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/DateInput.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

import net.paymate.util.*; // Safe
import java.util.*;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;

public class DateInput {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DateInput.class, ErrorLogStream.VERBOSE);

  private static final String NAUGHT = "00";
  private static final String MYFORMAT = LocalTimeFormat.DESCENDINGTIMEFORMAT;

  public String year;
  public String month;
  public String day;
  public String hour;
  public String minute;
  public String second;
  private LocalTimeFormat ltf;

  private DateInput(TimeZone tz) {
    setTz(tz);
  }

  public DateInput(UTC utc, TimeZone tz) {
    this(tz);
    setFromUTC(utc);
  }

  private void setTz(TimeZone tz) {
    this.ltf = LocalTimeFormat.New(tz, MYFORMAT);
  }

  public DateInput(String year, String month, String day, String hour, String minute, String second, TimeZone tz) {
    this(tz);
    this.year = year;
    this.month = month;
    this.day = day;
    this.hour = hour;
    this.minute = minute;
    this.second = second;
  }

  public DateInput(DateInput copy) {
    this(copy.ltf.getZone());
    this.year = copy.year;
    this.month = copy.month;
    this.day = copy.day;
    this.hour = copy.hour;
    this.minute = copy.minute;
    this.second = copy.second;
  }

  public static final DateInput Now(TimeZone tz) {
    DateInput di = new DateInput(tz);
    LocalTimeFormat ltftemp = LocalTimeFormat.New(tz, MYFORMAT);
    return di.fromString(ltftemp.format(UTC.Now()));
  }

  private DateInput fromString(String datestr) {
    return FromString(this, datestr);
  }

  // convert from a LocalTimeFormat.DESCENDINGTIMEFORMAT to a DateInput
  private static final DateInput FromString(DateInput di, String datestr) {
    // "yyyyMMddHHmmssSSS"
    if(!StringX.NonTrivial(datestr)) {
      datestr = "";
    }
    // make sure it is the right length, exactly
    datestr = StringX.fill(datestr, '0', 14, false);
    // now take it apart
    di.year  = StringX.subString(datestr, 2,  4); // assumes 20XX
    di.month = StringX.subString(datestr, 4,  6);
    di.day   = StringX.subString(datestr, 6,  8);
    di.hour  = StringX.subString(datestr, 8,  10);
    di.minute= StringX.subString(datestr, 10, 12);
    di.second= StringX.subString(datestr, 12, 14);
    return di;
  }

  /**
   * Beginning of day
   */
  public void beginningOfDay() {
    hour = minute = second = NAUGHT;
  }

//  private static final String EODHOUR = "23";
//  private static final String EODMINSEC = "59";
//  public void endOfDay() {
//    hour = EODHOUR;
//    minute = second = EODMINSEC;
//  }
// replaced by:
  public void beginningNextDay() {
    DateInput next = new DateInput(this);
    next.beginningOfDay();
    UTC time = next.toUTC();
    time.changeByDays(1); // roll a day
    setFromUTC(time);
  }

  public void setDayTo(DateInput other) {
    this.year = other.year;
    this.month = other.month;
    this.day = other.day;
  }

  public void setFromUTC(UTC utc) {
    FromString(this, ltf.format(utc));
  }

  public static final DateInput fromUTC(UTC utc, TimeZone tz) {
    return new DateInput(utc, tz);
  }

  public static final UTC toUTC(DateInput date) {
    return nonTrivial(date) ? date.toUTC() : null;
  }

  public UTC toUTC() {
    // if the date is not trivial, fix its format, convert to a Date, and stuff it in the range
    if(nonTrivial()) {
      twoDigitFix(); // make them all at least 2 digits wide
      // +++ NOT Y21C-compliant!
      if(StringX.NonTrivial(year) && (year.length() < 3)) {
        year = "20" + year;
      }
      String b4 = year + month + day + hour + minute + second + "000";
      if(StringX.parseLong(b4) == 0) {
        return null;
      }
      UTC ret = ltf.parseUtc(b4);
      dbg.VERBOSE("b4="+b4+", LTF="+ltf+", UTC="+ret);
      return ret;
    }
    return null;
  }

  private void twoDigitFix() {
    year   = Formatter.twoDigitFixed(year);
    month  = Formatter.twoDigitFixed(month);
    day    = Formatter.twoDigitFixed(day);
    hour   = Formatter.twoDigitFixed(hour);
    minute = Formatter.twoDigitFixed(minute);
    second = Formatter.twoDigitFixed(second);
  }

  public static final boolean nonTrivial(DateInput date) {
    return (date != null) ? date.nonTrivial() : false;
  }

  public boolean nonTrivial() {
    return  nonTrivialDate() || nonTrivialTime();
  }
  public boolean nonTrivialDate() {
    return StringX.NonTrivial(year) || StringX.NonTrivial(month) || StringX.NonTrivial(day);
  }
  public boolean nonTrivialTime() {
    return StringX.NonTrivial(hour) || StringX.NonTrivial(minute) || StringX.NonTrivial(second);
  }
  public void nullTime() {
    hour = minute = second = null;
  }
}
