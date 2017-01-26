/**
 * Title:        Primer<p>
 * Description:  generates prime numbers<p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: Primer.java,v 1.4 2001/07/19 01:06:55 mattm Exp $
 */

package net.paymate.util;

public class Primer {

  public static final long firstPrimeGreaterThan(long thisNumber) {
    long orig = thisNumber;
    thisNumber|=1;//ensure arg is odd
    if(thisNumber <= orig) {
      thisNumber += 2; // has to be GREATER THAN, not EQUAL TO
    }
    while(!isPrime(thisNumber)&&thisNumber>0) {//coulda gone on forever :)
      thisNumber+=2;
    }
    return thisNumber;
  }

  public static final boolean isPrime(long candidate) {
    // Try dividing the number by all odd numbers between 3 and its sqrt
    long sqrt = Math.round(Math.sqrt(candidate));
    for (long i = 3; i <= sqrt; i += 2) {
      if (candidate % i == 0) return false;  // found a factor
    }
    // Wasn't evenly divisible, so it's prime
    return true;
  }

  public static final void main(String [] args) {
    int primer = 0;
    for(int i = 0; i < 27; i++) {
      primer = (int)Primer.firstPrimeGreaterThan((long)primer);
      System.out.println("primer = " + primer);
    }
  }

}
