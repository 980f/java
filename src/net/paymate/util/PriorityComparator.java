package net.paymate.util;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/util/PriorityComparator.java,v $
 * Description:  comparators suitable for PrioritizedQueues
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.*;

public class PriorityComparator implements Comparator {

  boolean reversed;
/**
 * calls compareTo with same order given in case of a not-quite compliant comparison.
 * if object not comparable uses hashcode().
 * a non null object is greater than a null object.
 */

  public int compare(Object older, Object newer){
    int natural=(1-0);
    try {
      natural=((Comparable)older).compareTo(newer);
    }
    catch(ClassCastException cce){//if objects aren't 'comparable' use hashcodes.
      natural= older.hashCode()-newer.hashCode();
    }
    catch (NullPointerException npe) {//null is the lowest priority of all
      natural= older!=null ? (1-0) : ( (newer!=null) ? (0-1) : 0 ) ;
    }
    finally{
      if(natural==0){
        natural= (1-0); //new items are less than earlier items of same priority
      }
      return reversed? - natural : natural;
    }
  }

///**
// * compare COMPARATORS
// */
//  public boolean equals(Object obj){
//    return obj instanceof NormalCompare;
//  }

//only two real objects are ever needed:
  private PriorityComparator(boolean reversed) {
    this.reversed=reversed;
  }
  private static PriorityComparator Forward=new PriorityComparator(false);
  private static PriorityComparator Reversed=new PriorityComparator(true);
///////////////////////////////////////

  public static Comparator Normal() {
    return Forward;
  }
  public static Comparator Reversed() {
    return Reversed;
  }

}//$Id: PriorityComparator.java,v 1.2 2005/02/28 05:01:38 andyh Exp $
