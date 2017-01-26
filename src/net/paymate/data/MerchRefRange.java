package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/MerchRefRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.lang.StringX;

public class MerchRefRange extends StringRange {

  public Comparable filter(String stan){
    return StringX.trim(stan); // don't want any spaces on each side
  }

  public MerchRefRange(String one, String two){
    super(one,two,false); //don't sort.
  }

}
//$Id: MerchRefRange.java,v 1.3 2003/10/25 20:34:20 mattm Exp $