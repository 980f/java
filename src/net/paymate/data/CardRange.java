package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/CardRange.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.jpos.data.Mod10;

public class CardRange extends StringRange {
  // need to check for all chars decimal.
  //  public String filter(String filthy){
    //    return StringX.NonTrivial(filthy)? filthy : null;
  //  }

  /**
  * if looking for exact card number and it is structurally invalid don't bother looking for it
  * if a range then don't require either end to be valid.
  */
//  public boolean NonTrivial(){
//    return singular()?net.paymate.jpos.data.Mod10.zerosum(oneImage()):super.NonTrivial();
//  }

  public boolean isValid() {
    String img = oneImage();
    return singular() ?
        (img.length() > 4 ? Mod10.zerosum(oneImage()) :
         (img.length() == 4 ? super.NonTrivial() : false)) : false /* we only do singular now! */ ;
  }

  public CardRange(String one, String two){
    super(one,two,true); //always sorted
  }
}
