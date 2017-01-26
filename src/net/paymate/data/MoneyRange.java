package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/MoneyRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */
import net.paymate.util.*;
import java.util.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public class MoneyRange extends ObjectRange  {
  public final static String moneyFormat="#0.00";

  public Comparable filter(String filthy){
    if(StringX.NonTrivial(filthy)) {
      if(filthy.indexOf(".") < 0) {
        filthy += ".00";
      }
      return new LedgerValue(moneyFormat,filthy);
    }
    return null; // must be null
  }

  public MoneyRange(String one, String two){
    super(one,two,true); //money is always sorted and filtered
  }

  public String oneImage(){
    Object one = one();
    return ObjectX.NonTrivial(one) ? String.valueOf(((LedgerValue)one).Value()) : "";
  }

  public String twoImage(){
    Object two = two();
    return ObjectX.NonTrivial(two) ? String.valueOf(((LedgerValue)two).Value()) : "";
  }


}