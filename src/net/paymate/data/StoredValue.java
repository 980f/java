package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/StoredValue.java,v $
 * Description:  so far this is just the returned details report.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.util.*;
import net.paymate.awtx.*;

public class StoredValue implements isEasy {

  public RealMoney balance;
  final static String balanceKey="balance";

  public void save(EasyCursor ezp){
    balance.saveas(balanceKey,ezp);
  }

  public void load(EasyCursor ezp){
    balance.loadfrom(balanceKey,ezp);
  }

  private StoredValue(RealMoney balance) {
    this.balance=balance;
  }

  public static StoredValue New(RealMoney balance){
    return new StoredValue(balance);
  }

  public static StoredValue Zero(){
    return new StoredValue(RealMoney.Zero());
  }

}
//$Id: StoredValue.java,v 1.3 2002/01/14 08:40:56 andyh Exp $