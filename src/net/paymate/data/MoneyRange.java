package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/MoneyRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */
import net.paymate.util.*;
import net.paymate.data.*;
import  net.paymate.ISO8583.data.*;
import java.util.*;

public class MoneyRange extends ObjectRange  {
  public final static String moneyFormat="#0.00";

  public Comparable filter(String filthy){
    if(Safe.NonTrivial(filthy)) {
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

}