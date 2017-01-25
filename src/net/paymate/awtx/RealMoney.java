/**
* Title:        RealMoney
* Description:  Per Andy on 20000929, this class forces positive.
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: RealMoney.java,v 1.17 2001/10/15 22:40:06 andyh Exp $
*/
package net.paymate.awtx;

import net.paymate.data.*;
import net.paymate.ISO8583.data.*;
import  net.paymate.util.*;
import  java.text.*;

public class RealMoney implements Comparable {

  private static final ErrorLogStream dbg = new ErrorLogStream(RealMoney.class.getName());

  protected long cents; //all real activity is in whole cents

  public String dollars() { // for inserting into tranjour
    return Image("#0.00");
  }

  public long Value(){
    return cents;
  }

  public static final long Value(RealMoney rhs){
    return rhs!=null?rhs.Value():0;
  }

  // this method allows us to store a format in the database and use it here to generate numbers
  private static final String defaultFormat = "$#0.00";

  public String Image(){
    return Image(defaultFormat);
  }

  public String Image(String format) {
    StringBuffer sbsnm = new StringBuffer();
    DecimalFormat money = new DecimalFormat();
    money.applyPattern(format);
    sbsnm.setLength(0);
    double amt = cents / 100.0;
    money.format(amt, sbsnm, new FieldPosition(NumberFormat.INTEGER_FIELD));
    return sbsnm.toString();
  }

  public RealMoney parse(String image){
    // +_+ move this stuff to a more public place, not ISO
    setto(LedgerValue.parseImage(image));
    return this;
  }

  private void validateValue() {
    if(cents < 0) {
      cents = 0;
      dbg.WARNING("Someone tried to set the cents to a negative value!");
    }
  }

  public RealMoney(String image){
    parse(image);
  }

  public void setto(long incents){
    cents=incents;
    validateValue();
  }

  public void setto(RealMoney rhs){
    cents=rhs.cents;
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
    else if(obj instanceof String){//presume it is string from tranjour!
      rhs= LedgerValue.parseImage ((String)obj);
    }
    else {//as speced by sun:
      throw new java.lang.ClassCastException();
    }
    return net.paymate.jpos.awt.Math.signum(this.cents-rhs);//reduces long diff to -1,0,1
  }

  public RealMoney plus(RealMoney rhs){
    return new RealMoney(this.Value()+Value(rhs));
  }

  public RealMoney add(RealMoney rhs){
    this.cents+=Value(rhs);
    return this;
  }

  public RealMoney subtract(RealMoney rhs){
    this.cents-=Value(rhs);
    return this;
  }

  public String toString(){
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

}
//$Id: RealMoney.java,v 1.17 2001/10/15 22:40:06 andyh Exp $
