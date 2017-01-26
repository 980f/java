package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/StringRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;

public class StringRange extends ObjectRange {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StringRange.class);

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
//$Id: StringRange.java,v 1.6 2002/07/09 17:51:23 mattm Exp $
