package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/StanRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;
import  net.paymate.ISO8583.data.*;
import java.util.*;

public
class StanRange extends StringRange {

  public Comparable filter(String stan){
    return Safe.NonTrivial(stan)?Fstring.zpdecimal(Safe.parseInt(stan)%1000000,6):null;
  }

  public StanRange(String one, String two){
    super(one,two,false); //don't sort.
  }

}
//$Id: StanRange.java,v 1.3 2001/11/14 01:47:49 andyh Exp $