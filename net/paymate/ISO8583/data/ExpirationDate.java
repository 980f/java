package net.paymate.ISO8583.data;
/**
* Title:        $Source: /cvs/src/net/paymate/ISO8583/data/ExpirationDate.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ExpirationDate.java,v 1.16 2001/10/15 22:39:44 andyh Exp $
*/

import net.paymate.util.*;

public class ExpirationDate {
  protected int month;
  protected int year;
  public final static int Bogus=99;

  public static final boolean ValidMonth(int month){
    return month>=1 && month <=12;
  }

  /**
   * a valid year is one that isn't bogus,
   * and per some rule somewhere less than 17 years into the future..
   */
  public static final boolean ValidYear(int year){
    return year>=0 && year<Bogus || year/100==20;
  }

  public boolean isLegit(){
    return  ValidMonth(month) && ValidYear(year);
  }

  public boolean equals(ExpirationDate rhs){
    return rhs!=null && rhs.month==month && rhs.year==year;
  }

  public String Image(){ //@LOC@
    return Safe.twoDigitFixed(month)+'/'+Safe.twoDigitFixed(year);
  }

  public String YYmm(){
    return Safe.twoDigitFixed(year)+Safe.twoDigitFixed(month);
  }

  public String mmYY(){
    return Safe.twoDigitFixed(month)+Safe.twoDigitFixed(year);
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
    if(Safe.NonTrivial(yymm)){
      yymm = yymm.trim();
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
        String [] part={yymm.substring(0,2), yymm.substring(2,4)};
        return setYear(Integer.parseInt(part[yearfirst?0:1])).setMonth(Integer.parseInt(part[yearfirst?1:0]));
      case 5: //remove middle char and try that, mm/yy or mm-yy
        return setYear(Integer.parseInt(yymm.substring(3,5))).setMonth(Integer.parseInt(yymm.substring(0,2)));
      }//end switch
      } catch(NumberFormatException nfe){
        ErrorLogStream.Debug.ERROR("undecipherable expiration date");
//        ErrorLogStream.Debug.Caught(nfe); // --- we needed the trace to see where it was happening
        return Clear();
      }
    } else {
      return Clear();
    }
  }

  public ExpirationDate Clear(){
    month=year=Bogus;
    return this;
  }

   public boolean isTrivial(){
    return month==Bogus && year==Bogus;
  }

  public ExpirationDate setMonth(int month){
    this.month=ValidMonth(month)?month:Bogus;
    return this;
  }

  public ExpirationDate setYear(int year){
    this.year=ValidYear(year)?year%100:Bogus;
    return this;
  }

  public ExpirationDate(String yymm){
    parseYYmm(yymm);
  }

  public ExpirationDate(ExpirationDate rhs){
    this.year=  rhs.year;
    this.month= rhs.month;
  }

  public ExpirationDate(){
    Clear();
  }

}
//$Id: ExpirationDate.java,v 1.16 2001/10/15 22:39:44 andyh Exp $
