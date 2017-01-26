package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/Reconciliation.java,v $
 * Description:  lists of discrepancies
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.data.SettlementItem;

public class Reconciliation  implements isEasy {
  Vector missingFromDatabase;//items in settlement request but not in txn database
  Vector missingFromCapture; //items in txn database but not in txn database
  Vector mismatch; //items that match tracking numbers, but not content.

  public void save(EasyCursor ezp){
  }
  public void load(EasyCursor ezp){
  }

  public void notInDb(SettlementItem submitted){
    missingFromDatabase.add(submitted.trackingNumber());
  }


  public Reconciliation() {
  }
}