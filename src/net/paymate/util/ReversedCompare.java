package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/ReversedCompare.java,v $
 * Description:  reverse order returned by existing comparator of a class
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import java.util.*;

public class ReversedCompare extends NormalCompare {
/**
 * calls compare() with same order given in case of a not-quite compliant comparison.
 * uses negation to reverse the sense of the compare.
 */
  public int compare(Object o1, Object o2){
    return -super.compare(o1,o2);
  }
/**
 * compare COMPARATORS
 */
  public boolean equals(Object obj){
    return obj instanceof ReversedCompare;
  }

  private static ReversedCompare rshared=new ReversedCompare();

  public static Comparator New() {
    return rshared;
  }

}
//$Id: ReversedCompare.java,v 1.2 2001/11/30 03:41:07 andyh Exp $
