package net.paymate.database;

/**
* Title:        $Source: /cvs/src/net/paymate/database/TranjourFilter.java,v $
* Description:  Filter to apply when selecting data from the tranjour table.
*               A collection of ranges of various types.
*               In fact if made into a true collection it would expand easily!
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.2 $
*/

import net.paymate.data.*;

public class TranjourFilter {
  public CardRange card;
  public TimeRange time;
  public MoneyRange amount;
  public StanRange stan;
  public AuthRange appr;

  public boolean NonTrivial(){
    return
    StringRange.NonTrivial( card) ||
    StringRange.NonTrivial( time) ||
    StringRange.NonTrivial( amount) ||
    StringRange.NonTrivial( stan) ||
    StringRange.NonTrivial( appr) ;
  }

  public TranjourFilter() {
    //leaves things null
  }

}
