/**
* Title:        $Source: /cvs/src/net/paymate/jpos/data/Mod10.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Mod10.java,v 1.7 2001/07/19 01:06:51 mattm Exp $
*/
package net.paymate.jpos.data;

public class Mod10 {
  public static final boolean throwStuff=false;

  protected static final int compute(String s){
    int luhnsum=0;
    boolean dublit=false;  //since we start at ls digit and work towards ms.
    for(int i=s.length();i-->0;){
      int digit= Character.digit(s.charAt(i),10); //base 10 digits.
      if(digit>=0){ //valid character
        if(dublit){//must double and wrap before adding to accumulator
          digit<<=1;
          if(digit>=10){//
            digit-=9; // (-10 + 1);
          }
        }
        luhnsum+=digit;
        dublit=!dublit; //double every other one.
      } else {
        if(throwStuff){
          throw new NumberFormatException("CardNumber: ["+s.charAt(i)+"] is not 0..9 @index:"+i);
        }
        return -1;//less drastic than an exception...
      }
    }
    return luhnsum%10 ;
  }

  public static final boolean zerosum(String s){
    return compute(s)==0;
  }

  /**
   * for faking card numbers!
   */
  public static final int sum(String shortByOne){
    return 10-compute(shortByOne+'0');
  }

  public static final String spam(String totest){
    return Integer.toString(sum(totest.substring(0,totest.length()-1)))+((zerosum(totest)?" Ok":" Fails"));
  }

  public static final String spam(CardNumber number){
    return spam(number.Image());
  }

}
//$Id: Mod10.java,v 1.7 2001/07/19 01:06:51 mattm Exp $
