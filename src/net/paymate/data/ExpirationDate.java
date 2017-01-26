package net.paymate.data;
/**
* Title:        $Source: /cvs/src/net/paymate/data/ExpirationDate.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Rev: ExpirationDate.java,v 1.19 2002/02/06 01:27:27 andyh Exp $
*/

import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.text.Formatter;

public class ExpirationDate implements isEasy {
  static ErrorLogStream dbg;

  private final static int Bogus=99;
  private final static String MOOT="MOOT";//used only for transport layer

  private int month;
  private int year;
  private boolean isMoot=false;

  public boolean isMoot(){
    return isMoot;
  }

  public ExpirationDate moot(){
    isMoot=true;
    return this;
  }

  public String toString(){
    return YYmm(MOOT);
  }

  public static final boolean ValidMonth(int month){
    return month>=1 && month <=12;
  }

  /**
   * a valid year is one that isn't bogus,
   * and per someone's rule somewhere less than 17 years into the future..
   */
  public static final boolean ValidYear(int year){
    if(year/100==20){//fortunately this company was founded in year 2000.
      year %=100;
    }
    return year>=0 && year<Bogus;//+_+ apply 17 year rule?--- too many qualifications are not yet in scope to do so.
  }

  public boolean isLegit(){
    return  isMoot() || (ValidMonth(month) && ValidYear(year));
  }

  public boolean equals(ExpirationDate rhs){
    return rhs!=null && ( (rhs.isMoot&&isMoot) ||(rhs.month==month && rhs.year==year) );
  }

  public String Image(){ //@LOC@
    return mm()+'/'+YY();
  }

  public String YYmm(){
    return YY()+mm();
  }

  public String YYmm(String onmoot){
    return isMoot()?onmoot: YYmm();
  }

  private String mm() {
    return Formatter.twoDigitFixed(month);
  }
  private String YY() {
    return Formatter.twoDigitFixed(year);
  }

  public String mmYY(){
    return mm()+YY();
  }

  public String mmYY(String onmoot){
    return isMoot()?onmoot: mmYY();
  }

  public int Value(){
    return (year%100)*100+month;
  }

  public ExpirationDate parsemmYY(String mmyy){
    return parse(mmyy,false);
  }

  public ExpirationDate parseYYmm(String yymm){
    return parse(yymm,true);
  }

  public ExpirationDate parse(String yymm,boolean yearfirst){
    isMoot=false;
    if(StringX.NonTrivial(yymm)){
      yymm = yymm.trim();
      if(MOOT.equals(yymm)){
        return Clear().moot();
      }
      try {//cause parseInt throws shit
        switch(yymm.length()){
        default://way too bizarre
        case 0://was blank
        case 1://might as well have been blank
        case 2://still too garbagy to parse
          return Clear();
        case 3://presume missing leading zero
          return parse('0'+yymm,yearfirst);//don't modify given string! // wouldn't matter; it's a reference.  Strings are immutable.  You would just get a reference to a new string.
        case 4://expected format
          dbg.VERBOSE("NormalString:"+Ascii.bracket(yymm)+(yearfirst?"yymm":"mmyy"));
          String [] part={yymm.substring(0,2), yymm.substring(2,4)};
          return setYear(StringX.parseInt(part[yearfirst?0:1])).setMonth(StringX.parseInt(part[yearfirst?1:0]));
//        case 5: //remove middle char and try that, mm/yy or mm-yy //not until we test that middle char!
//          return setYear(StringX.parseInt(yymm.substring(3,5))).setMonth(StringX.parseInt(yymm.substring(0,2)));
        }//end switch
      } catch(NumberFormatException nfe){
        dbg.WARNING("undecipherable expiration date:'"+yymm+"'");
        return Clear();
      }
    } else {
      return Clear();
    }
  }

  public ExpirationDate Clear(){
    month=year=Bogus;
    isMoot=false;
    return this;
  }

   public boolean isTrivial(){
    return !isMoot() && (month==Bogus || year==Bogus);
  }

  public ExpirationDate setMonth(int month){
    dbg.VERBOSE("setMonth to:"+month);
    this.month=ValidMonth(month)?month:Bogus;
    return this;
  }

  public ExpirationDate setYear(int year){
    dbg.VERBOSE("setYear to:"+year);
    this.year=ValidYear(year) ? (year%100) : Bogus;
    return this;
  }

  public ExpirationDate(String yymm){
    parseYYmm(yymm);
  }

  public ExpirationDate(ExpirationDate rhs){
    this.year=  rhs.year;
    this.month= rhs.month;
    this.isMoot=rhs.isMoot;
  }

  public ExpirationDate(){
    if(dbg==null) dbg=ErrorLogStream.getForClass(ExpirationDate.class);
    Clear();
  }

  public void save(EasyCursor ezc){
    ezc.setInt("year",year);
    ezc.setInt("month",month);
    ezc.setBoolean("isMoot",isMoot);
  }

  public void load(EasyCursor ezc){
    year=ezc.getInt("year");
    month=ezc.getInt("month");
    isMoot=ezc.getBoolean("isMoot");
  }

}
//$Id: ExpirationDate.java,v 1.2 2003/11/02 07:59:09 mattm Exp $
