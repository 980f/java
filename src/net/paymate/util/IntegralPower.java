package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/IntegralPower.java,v $
 * Description:  integer raised to an integer, efficent only for small powers
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

import net.paymate.lang.Bool;
import net.paymate.lang.MathX;
import net.paymate.lang.StringX;

public class IntegralPower {
  public int power;
  public int radix;
  public int exponent;
  public boolean overflowed;

  public final static int useRPE=20;

  /**
   * use Russian Peasant Exponentiation, it is faster for bigger exponents
   */
  private void RPE(){
    power=1;
    overflowed=(radix==0);
    int exp=exponent;
    int radpow=radix;
    boolean negroot=radix<0;
    if(negroot){ //need to do this in absolute value for efficiency of overflow detection
      radix=-radix;
    }
    while(!overflowed && exponent>0){
      if( (exponent&1) !=0){
        power*=radix;
      }
      overflowed=power<0;
      radpow*=radpow;
      exponent>>=1;
    }
    if(!overflowed && negroot && Bool.isOdd(exponent)){
      power=-power;
    }
  }

  private void linearImp(){
    power=1;
    overflowed=(radix==0);
    int exp=exponent;
    if(radix>0){
      while(!overflowed && exp-->0){
        power*=radix;
        overflowed=power<0;
      }
    } else {
      boolean benegative=false;
      while(!overflowed && exp-->0){
        power*=radix;
        benegative=!benegative;
        overflowed=benegative? power>0 : power<0;
      }
    }
  }

  private IntegralPower(int radix,int exponent) {
    this.radix=radix;
    this.exponent=exponent;
    if(exponent<20){
      linearImp();
    } else {
      RPE();
    }
  }

  public static IntegralPower forRadix(int radix) {
    return raise(radix,0);
  }

  public static IntegralPower raise(int radix,int exponent) {
    return new IntegralPower(radix,exponent);
  }

  /**
   * @todo deal properly with negative @param value.
   */
  public static IntegralPower Above(int value,int radix){
    IntegralPower igp=IntegralPower.forRadix(radix);
    if(value !=MathX.INVALIDINTEGER){
      value=Math.abs(value);
      while(!igp.overflowed && value>=igp.power){
        igp.increment();
      }
    }
    return igp;
  }

  public IntegralPower increment(){
    if(!overflowed){
      power*=radix;
      ++exponent;
      overflowed= (radix>0 || ((exponent&1)==0) )? power<0 : power>0;
    }
    return this;
  }

  public String toString(){
    return " "+radix+"**"+exponent+"=="+power+ (overflowed?" !?!":" ");
  }

}
//$Id: IntegralPower.java,v 1.5 2004/01/09 11:46:07 mattm Exp $