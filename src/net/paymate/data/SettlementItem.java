package net.paymate.data;

/**
 * Title:        $Source: /cvs/src/net/paymate/data/SettlementItem.java,v $
 * Description:  items to describe a transaciton for settlement purposes
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;

public class SettlementItem implements isEasy {
  private Integer trackingNumber;
  public Integer trackingNumber(){
    return trackingNumber;
  }

  public void save(EasyCursor ezp){
  }
  public void load(EasyCursor ezp){
  }

  public SettlementItem() {
  }

}
//$id$