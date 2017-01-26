package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/StanRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.lang.StringX;

public class StanRange extends StringRange {

  public Comparable filter(String stan){
    // this makes sure what we took in was really an integer; is this necessary?  Can we just leave it the original String
    //if we leave alhpa trash in it won't the query get a syntax error?
    String ret = Integer.toString(StringX.parseInt(stan));
    if(StringX.equalStrings(ret, "0")) { // parseInt returns 0 if Trivial, then that makes it NonTrivial!  However, a stan of 0 *is* trivial!
      ret = "";
    }
    return ret;
  }

  public StanRange(String one, String two){
    super(one,two,false); //don't sort.
  }

}
//$Id: StanRange.java,v 1.9 2003/10/25 20:34:21 mattm Exp $