package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/AuthRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.*;
import  net.paymate.ISO8583.data.*;
import java.util.*;

public
class AuthRange extends StringRange {
  public Comparable filter(String auth){
    return Safe.NonTrivial(auth)? Safe.fill(auth, '0', 6, true):null;
  }

  public AuthRange(String one, String two){
    super(one,two,true); //always sorted and filtered
  }
}