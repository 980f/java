package net.paymate.awtx;
/**
* Title:        $Source: /cvs/src/net/paymate/awtx/RealMoney.java,v $
* Description:  Per Andy on 20000929, this class forces positive.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: RealMoney.java,v 1.32 2003/10/25 20:34:17 mattm Exp $
*/

import net.paymate.data.*;
import  net.paymate.util.*;
import  java.text.*;
import net.paymate.lang.MathX;

public class RealMoney implements Comparable, isEasy {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(RealMoney.class);

  protected long cents; //all real activity is in whole cents
  //someday we will add:
  //Currency cur=Currency.USA; //=840.

  public long Value(){
    return cents;
  }

  public long absValue() {
    return Math.abs(cents);
  }

  public boolean isZero() {
    return cents == 0L;
  }

  public static final long Value(RealMoney rhs){
    return rhs!=null?rhs.Value():0;
  }

  public boolean NonTrivial(){
    return cents>0;
  }

  public static boolean NonTrivial(RealMoney probate){
    return probate!=null&&probate.NonTrivial();
  }

  public static RealMoney Zero(){
    return new RealMoney(0);
  }

  // this method allows us to store a format in the database and use it here to generate numbers
  private static final String defaultFormat = "$#0.00";//cur.standardFormat();

  public String Image(){//cur.Format(cents);
    return Image(defaultFormat);
  }

  public String Image(String format) {//cur.Format(cents,String format);
    StringBuffer sbsnm = new StringBuffer();
    DecimalFormat money = new DecimalFormat();
    money.applyPattern(format);
    sbsnm.setLength(0);
    double amt = cents / 100.0;
    money.format(amt, sbsnm, new FieldPosition(NumberFormat.INTEGER_FIELD));
    return String.valueOf(sbsnm);
  }

  public RealMoney parse(String image){
    // +_+ move this stuff to a more public place, not ISO
    // +++ do we really need to be incestuously referring to ledgervalue?
    setto(LedgerValue.parseImage(image));
    return this;
  }

  private RealMoney validateValue() {
    if(cents < 0) {
      cents = 0;
      dbg.WARNING("Someone tried to set the cents to a negative value!");
    }
    return this;
  }

  public RealMoney(String image){
    parse(image);
  }

  public RealMoney setto(long incents){
    cents=incents;
    return validateValue();
  }

  public RealMoney setto(int incents){
    cents=(long) incents;
    return validateValue();
  }

  public RealMoney setto(RealMoney rhs){
    cents=rhs.cents;
    return validateValue();
  }

  /**
   * @return -1,0,1 which is more restrictive than the compareable interface requires,
   * but is nice for use as an index
   */
  public int compareTo(Object obj) throws java.lang.ClassCastException {
    long rhs=0;
    if(obj instanceof RealMoney){
      rhs=((RealMoney)obj).cents;
    } else if(obj instanceof LedgerValue){
      LedgerValue lv= (LedgerValue) obj;
      return -lv.compareTo(this);
    }
    else if(obj instanceof String){
      rhs= LedgerValue.parseImage ((String)obj);
    }
    else {//as speced by sun:
      throw new java.lang.ClassCastException();
    }
    return MathX.signum(this.cents-rhs);//reduces long diff to -1,0,1
  }

  public boolean exceeds(RealMoney limit){
    try {
      return compareTo(limit)>0;
    }
    catch (Exception ex) {
      return true;
    }
  }

  public RealMoney plus(RealMoney rhs){
    return new RealMoney(this.Value()+Value(rhs));
  }

  public RealMoney add(RealMoney rhs){
    this.cents+=Value(rhs);
    return this;
  }

  public RealMoney minus(RealMoney rhs){
    return new RealMoney(this.Value()-Value(rhs));
  }

  public RealMoney subtract(RealMoney rhs){
    this.cents-=Value(rhs);
    return this;
  }

  /**
   * @return cents as string.
   */
  public String toString(){//internal image... integral cents
    return Long.toString(cents);
  }

  public RealMoney(long incents){//+++ reject negatives, need an exception +^+
    setto (incents);
  }

  public RealMoney(RealMoney rhs){
    this(rhs.cents);
  }

  public RealMoney(){
    this(0);
  }
  ////////////////////////////
  // isEasy
  public void save(EasyCursor ezp){
    saveas("cents",ezp);
  }

  public void load(EasyCursor ezp){
    loadfrom("cents",ezp);
  }

  public void saveas(String key,EasyCursor ezp){
    ezp.setLong(key,cents);
  }
  public void loadfrom(String key,EasyCursor ezp){
    cents=ezp.getLong(key);
  }

}
//$Id: RealMoney.java,v 1.32 2003/10/25 20:34:17 mattm Exp $
