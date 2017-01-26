package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/AuthRange.java,v $
 * Description: autorization code range, NOT authorizer bin range.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.lang.StringX;

public class AuthRange extends StringRange {
  public Comparable filter(String auth){
    return StringX.NonTrivial(auth)? StringX.fill(auth, '0', 6, true):null;
  }

  public AuthRange(String one, String two){
    super(one,two,true); //always sorted and filtered
  }
}