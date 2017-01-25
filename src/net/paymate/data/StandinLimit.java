package net.paymate.data;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: StandinLimit.java,v 1.5 2001/07/13 18:48:20 andyh Exp $
 */

import net.paymate.awtx.RealMoney;
import net.paymate.util.*;

public class StandinLimit implements isEasy {

  public RealMoney perTxn;
  public RealMoney total;
//  public RealMoney perCard;

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
    ezc.setLong(totalKey, total.plus(perTxn).Value());//#standin3: later over limit problem
  }

  public void load(EasyCursor ezc){
    perTxn=new RealMoney(ezc.getLong(perTxnKey));
    total =new RealMoney(ezc.getLong(totalKey));
    total.subtract(perTxn);//#standin3: later over limit problem
  }

  public String spam(){
    return " limits:"+perTxn.Image()+":"+total.Image();
  }

  public boolean equals(StandinLimit rhs){
    return rhs!=null && rhs.perTxn.compareTo(perTxn)==0 && rhs.total.compareTo(total)==0;
  }

}
//$Id: StandinLimit.java,v 1.5 2001/07/13 18:48:20 andyh Exp $