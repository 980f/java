package net.paymate.lang;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/lang/MathX.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import java.lang.Math;

public class MathX {
  private MathX() {
    // don't instantiate me.  I am here for static purposes
  }

  public static final int  INVALIDINTEGER=Integer.MIN_VALUE; //NOT the same as invalid index
  public static final long INVALIDLONG=Long.MIN_VALUE;

  public static final int signum(int signed){
    return signed>0? 1 : signed<0 ? -1 : 0;
  }

  public static final int signum(long signed){
    return signed>0? 1 : signed<0 ? -1 : 0;
  }

  /**
 * @return exponent of log base 2 of @param
 * -1 rather than -infinity for 0.
 * test cases: 0=>-1 1=>0 2,3=>1 4,5,6,7=>2 8..15=>3
 *
 */
  public static int log2(int num){
    int log=-1;//rather than infinity
    if(num<0){//mostly do this to protect against shift being signed
      num=-num; //and 8000 is NOT a problem!
    }
    while(num!=0){
      num>>=1;
      ++log;
    }
    return log;
  }
  /** @return a signed number extracted from a set of bits within an integer.
   *  initially used by ncrA parsing.
   *  doesn't deal with end before start (field crossing boundary)
   * @todo: move into a byte package!
  */
  public static final int getSignedField(int datum,int start,int end){
    return (datum<<(31-start)) & ~((1<<end)-1);
  }

  /** @todo: move into a byte package!
   * @return two nibbles from @param high and @param low ls digits in the ls byte of an int.
   */
  public static final int packNibbles(int high,int low){
    return ((high&15)<<4) + (low&15);
  }

  // +++ why promote to only float, but then promote to double later?
  public static final double ratio(int num, int denom){
    return (denom==0)?0:((double)num)/((double)denom);
  }

  public static final double ratio(long num, long denom){
    return (denom==0)?0:((double)num)/((double)denom);
  }

  public static final long percent(long dividend, long divisor) {
    return Math.round(ratio(dividend * 100, divisor));
  }

  // true of numerator.0 / denominator.0 is a whole number
  public static final boolean /*whole*/multipleOf(int numerator, int denominator) {
    return (numerator % denominator) == 0;
  }
}