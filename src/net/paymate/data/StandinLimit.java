package net.paymate.data;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/data/StandinLimit.java,v $
 * Description:  limit values for standin approvals
 * Copyright:    Copyright (c) 2001-2002
 * Company:      PayMate.net
 * @author   Paymate.net
 * @version $Id: StandinLimit.java,v 1.11 2005/03/03 05:19:55 andyh Exp $
 * changed: made exact match to limit be 'approved' to make description easier.
 */

import net.paymate.awtx.RealMoney;
import net.paymate.util.*;

public class StandinLimit implements isEasy {

  private RealMoney perTxn;
  private RealMoney total;

  public RealMoney perTxn(){ return perTxn;}
  public RealMoney total(){ return total;}


  public final static String perTxnKey="perTxn";
  public final static String totalKey="total";

  public StandinLimit(long pertxn,long teatotal) {
    this(new RealMoney(pertxn), new RealMoney(teatotal));
  }

  public StandinLimit(RealMoney pertxn,RealMoney teatotal) {
    this.perTxn=pertxn;
    this.total =teatotal;
  }

  public StandinLimit() {
    this(0,0);
  }

  public void save(EasyCursor ezc){
    ezc.setLong(perTxnKey,perTxn.Value());
    ezc.setLong(totalKey, total.Value());
  }

  public void load(EasyCursor ezc){
    perTxn=new RealMoney(ezc.getLong(perTxnKey));
    total =new RealMoney(ezc.getLong(totalKey));
  }

  public String spam(){
    return " limits:"+perTxn.Image()+":"+total.Image();
  }

  public boolean equals(StandinLimit rhs){
    return rhs!=null && rhs.perTxn.compareTo(perTxn)==0 && rhs.total.compareTo(total)==0;
  }

  public boolean totalOk(RealMoney item,LedgerValue sofar){
    return total.NonTrivial() && (sofar.plus(item).compareTo(total)<=0);
  }

  public boolean totalOk(RealMoney item, long centsSofar){
    return totalOk(item, new LedgerValue().setto(centsSofar));
  }

  public boolean itemOk(RealMoney item){
    return  perTxn.NonTrivial() && item.compareTo(perTxn)<=0;
  }


/**
 *
 */
  public boolean NonZero(){
    return perTxn.NonTrivial() && total.NonTrivial();
  }

}
//$Id: StandinLimit.java,v 1.11 2005/03/03 05:19:55 andyh Exp $