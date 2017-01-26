/**
* Title:        $Source: /cvs/src/net/paymate/jpos/data/Mod10.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Mod10.java,v 1.10 2002/06/28 02:35:17 andyh Exp $
*/
package net.paymate.jpos.data;

public class Mod10 {
  public static final boolean throwStuff=false;
/**
 * specification: "double digit, if result is two digits add those digits"
 * first simplfication: "double digit, if >10 subtract 9"
 * final simplification: precompute and look up value.
 */
 //                                 0,1,2,3,4,5,6,7,8,9
  private static final int remap[]={0,2,4,6,8,1,3,5,7,9};
  private static final int compute(String s){
    int luhnsum=0;
    boolean dublit=false;  //since we start at ls digit and work towards ms.
    for(int i=s.length();i-->0;){
      int digit= Character.digit(s.charAt(i),10); //base 10 digits.
      if(digit>=0){ //valid character
        luhnsum += dublit ? remap[digit] : digit;
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
//$Id: Mod10.java,v 1.10 2002/06/28 02:35:17 andyh Exp $
