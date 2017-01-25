package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/CardRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;
import  net.paymate.ISO8583.data.*;
import java.util.*;

public class CardRange extends StringRange {
  // need to check for all chars decimal.
  //  public String filter(String filthy){
    //    return Safe.NonTrivial(filthy)? filthy : null;
  //  }

  /**
  * if looking for exact card number and it is structurally invalid don't bother looking for it
  * if a range then don't require either end to be valid.
  */
  public boolean NonTrivial(){
    return singular()?net.paymate.jpos.data.Mod10.zerosum(one()):super.NonTrivial();
  }

  public CardRange(String one, String two){
    super(one,two,true); //always sorted
  }
}
