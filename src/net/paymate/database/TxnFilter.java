package net.paymate.database;

/**
* Title:        $Source: /cvs/src/net/paymate/database/TxnFilter.java,v $
* Description:  Filter to apply when selecting data from the txn table.
*               A collection of ranges of various types.
*               In fact if made into a true collection it would expand easily!
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.9 $
*/

import net.paymate.data.*;
import net.paymate.util.*;

public class TxnFilter extends TimeFilter {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TxnFilter.class);

//  public TimeRange time; -- super

  public CardRange card;
  public MoneyRange amount;
  public StanRange stan;
  public AuthRange appr;
  public MerchRefRange merch;

  public boolean NonTrivial(){
    // do them ALL so that we can debug the output (change back after satisfied)
    boolean ret = false;
    ret |= ntprint("card"  , StringRange.NonTrivial(card));
    ret |= ntprint("time"  , StringRange.NonTrivial(time));
    ret |= ntprint("amount", StringRange.NonTrivial(amount));
    ret |= ntprint("appr"  , StringRange.NonTrivial(appr));
    ret |= ntprint("stan"  , StringRange.NonTrivial(stan));
    ret |= ntprint("merch" , StringRange.NonTrivial(merch));
    return ret;
  }

  public TxnFilter() {
    //leaves things null
  }
}
