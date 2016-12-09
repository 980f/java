package net.paymate.util;
/**
 * Title:        $Source: /cvs/src/net/paymate/util/LocalTimeFormat.java,v $
 * Description:  nominally constant format is used for ascii representation of time.
 *               sometimes the object has to be created before
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version $Id: LocalTimeFormat.java,v 1.16 2001/07/19 01:06:54 mattm Exp $
 */

import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.Date;

public class LocalTimeFormat implements isEasy {
  private static final ErrorLogStream dbg = new ErrorLogStream(LocalTimeFormat.class.getName());

  protected SimpleDateFormat wrapped;
//  protected
   String internalFormat;//cached for making new ones

  public String getFormat(){
    return internalFormat;
  }

  public TimeZone getZone(){
    return wrapped.getTimeZone();
  }
  /**
   * for when construction and use are widely separated.
   */
  private LocalTimeFormat setFormat(String s){
    wrapped.applyPattern(internalFormat=s);
    return this;
  }

  public static final Date genesis = dateFromInts(2000, 03, 15);

  public static final Date dateFromInts(int year, int month, int day) {
    Calendar cal = Calendar.getInstance();
    cal.set(year, month, day);
    return cal.getTime();
  }

  private void setZone(TimeZone fromServer){
    wrapped= new SimpleDateFormat();
    wrapped.set2DigitYearStart(dateFromInts(2000,1,1));
    wrapped.setTimeZone(fromServer);
  }

  protected LocalTimeFormat(TimeZone fromServer,String format){
    setZone(fromServer);
    //+_+ pathological default, but stops exceptions
    setFormat(Safe.OnTrivial(format,"z"));
  }

  public static final LocalTimeFormat copy(LocalTimeFormat rhs){
    return new LocalTimeFormat(rhs.wrapped.getTimeZone(),rhs.internalFormat);
  }

  public LocalTimeFormat Clone(){
    return copy(this);
  }

  public void save(EasyCursor ezp){
    ezp.setString("timezone",this.getZone().getDisplayName());
    ezp.setString("format",this.getFormat());
  }

  public void load(EasyCursor ezp){
    //order matters here!
    setZone(TimeZone.getTimeZone(ezp.getString("timezone")));
    setFormat(ezp.getString("format"));
  }

  /**
   * quite often the format is constant for the life of the object,
   * and known at the point of creations
   */
  public static final LocalTimeFormat New(TimeZone fromServer,String format) {
    return new LocalTimeFormat(fromServer,format);
  }

  public static final LocalTimeFormat New(String tz, String format) {
    return New(TimeZone.getTimeZone(tz),format);
  }

  public static final LocalTimeFormat New(TimeZone fromServer) {
    return New(fromServer,"");
  }

  public static final LocalTimeFormat Utc(String format) {
    return New(TimeZone.getTimeZone("UTC"),format);
  }

  public static final LocalTimeFormat Utc() {
    return Utc("yyyyMMddHHmmssSSS");//decimalized but covers full span
  }

  public String format(Date d){//
    return wrapped.format(d);
  }

  public Date parse(String image, Date onError){
    try {
      return wrapped.parse(image);
    } catch (java.text.ParseException arf){
      return onError;
    }
  }

  public static final Date onError=new Date();//+_+ need a better fallback
  public Date parse(String image){
// hack --- pad with zeroes if image is too short
    if(image.length() < internalFormat.length()) {
      image = Safe.fill(image, '0', internalFormat.length(), false);
    }
    return parse(image,onError);
  }

   /**
  * Year:         yyyy, yy (<--lower case)
  * Month:        MMM, MM, M
  * Day of week:  EEEE, EE
  * Day of Month: dd, d
  * Hour (1-12):  hh, h
  * Hour (0-23):  HH, H
  * Hour (0-11):  KK, K
  * Hour (1-24):  kk, k
  * Minute:       mm
  * Second:       ss
  * Millisecond:  SSS
  * AM/PM:        a
  * Time zone:    zzzz, zz
  * DOW in Month: F (eg: 3rd Thursday)
  * Day in Year:  DDD, D
  * Week in Year: ww
  * Era:          G (eg: BC/AD)
  */

}
//$Id: LocalTimeFormat.java,v 1.16 2001/07/19 01:06:54 mattm Exp $
