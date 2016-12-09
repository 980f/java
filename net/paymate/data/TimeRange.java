package net.paymate.data;

/**
* Title:         $Source: /cvs/src/net/paymate/data/TimeRange.java,v $
* Description:   manages a range of ascii representable dates.
* Copyright:     Copyright (c) 2000
* Company:       PayMate.net
* @author        PayMate.net
* @version  $Id: TimeRange.java,v 1.7 2001/10/17 22:07:22 andyh Exp $
*/

import net.paymate.util.*;
import net.paymate.data.*;
import java.util.TimeZone;
import java.util.Date;

public class TimeRange extends ObjectRange {

  private static final ErrorLogStream dbg = new ErrorLogStream(TimeRange.class.getName(), ErrorLogStream.WARNING);
  protected LocalTimeFormat ltf;
  protected String fieldname;

  public Date start(){
    return (Date)one;
  }

  public Date end(){
    return (Date)two;
  }

  public String one(){
    return ltf.format(start());
  }

  public String two(){
    return ltf.format(end());
  }

  // this is needed and was missing
  public Comparable filter(String input){
    return ltf.parse(input);
  }

  //////////////
  // queries
  public long milliSpan(){
    return broad()?end().getTime()-start().getTime():0;
  }

  public String fieldName(){
    return fieldname;
  }

  ////////////////////
  // we can do this without trouble by keeping dates as dates.
  public TimeRange setFormatter(LocalTimeFormat ltf){
    this.ltf=ltf;
    return this;
  }

  public TimeRange setFormat(String s){
    if(!ltf.getFormat().equals(s)){
      ltf=LocalTimeFormat.New(ltf.getZone(),s);
    }
    return this;
  }

  public LocalTimeFormat Formatter(){
    return ltf;
  }

  public TimeRange setStart(String s){
    setOne(s);
    dbg.VERBOSE("Setting start="+one());
    return this;
  }

  public TimeRange setEnd(String s){
    setTwo(s); // calls analyze()
    dbg.VERBOSE("Setting end="+two());
    return this;
  }

  public TimeRange setStart(Date d){
    dbg.VERBOSE("setStart(Date):"+d);
    return (TimeRange) setOne(d);
  }

  public TimeRange setEnd(Date d){
    dbg.VERBOSE("setEnd(Date):"+d);
    return (TimeRange) setTwo(d);
  }

  /**
  * for finding the bounds of an unordered set.
  */
  public TimeRange include(Date d){
    if(d!=null){
      if(two==null|| end().compareTo(d) <0){
        setTwo(d);
      }
      if(one==null|| d.compareTo(start())< 0){
        setOne(d);
      }
    }
    return this;
  }

  public TimeRange setStart(long utc){
    return setStart(new Date(utc));
  }

  public TimeRange setEnd(long utc){
    return setEnd(new Date(utc));
  }

  private TimeRange(LocalTimeFormat ltf,String fieldname) {
    super(true); //sorted
    this.fieldname=fieldname;
    // These lines MUST be here or else the system will find that this object is trivial when it is not -->
    this.ltf=ltf;
  }

  public static final TimeRange Create(String format,String fieldname){
    return new TimeRange(LocalTimeFormat.Utc(format),fieldname);
  }

  public static final TimeRange Create(LocalTimeFormat notdbquery){
    return new TimeRange(notdbquery,"not a query formatter");
  }

  public static final TimeRange Create(){
    return new TimeRange(LocalTimeFormat.Utc(),"not a query formatter");
  }

  private final static String fieldnamekey="fieldname";

  public void load(EasyCursor ezp){
    ltf.load(ezp);
    fieldname=ezp.getString(fieldnamekey);
    super.load(ezp);
  }

  public void save(EasyCursor ezp){
    ltf.save(ezp);
    ezp.setString(fieldnamekey,fieldname);
    super.save(ezp);
  }

  public static final TimeRange NewFrom(String key, EasyCursor ezp){
    TimeRange newone=Create();
    newone.loadfrom(key,ezp);
    return newone;
  }

  public static final TimeRange copy(TimeRange rhs){//+_+ extract a clone() function
    TimeRange newone=Create();
    if(rhs!=null){
      newone.setFormatter(rhs.ltf.Clone());
      newone.fieldname = rhs.fieldname;
      newone.copyMembers(rhs);
    }
    return newone;
  }

}
//$Id: TimeRange.java,v 1.7 2001/10/17 22:07:22 andyh Exp $

