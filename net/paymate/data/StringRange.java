package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/StringRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import net.paymate.util.*;

public class StringRange extends ObjectRange {
  private static final ErrorLogStream dbg = new ErrorLogStream(StringRange.class.getName());

  public Comparable filter(String input){
    return input;
  }

  protected StringRange(String one, String two,boolean sorted){
    super(one,two,sorted);
  }

  public static final StringRange New(String one, String two){
    return new StringRange(one, two, false);
  }

  public static final StringRange NewSorted(String one, String two){
    return new StringRange(one, two, true);
  }

}
//$Id: StringRange.java,v 1.4 2001/07/19 01:06:47 mattm Exp $
