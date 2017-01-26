package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/SettlementRequest.java,v $
 * Description:  info to submit a settlement
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.util.*;
import java.util.*;
import net.paymate.data.SettlementItem;

public class SettlementRequest extends AdminRequest implements isEasy {
  int batchNumber;
  Vector items; //settlementItems
  public void addItem(SettlementItem item){
    items.add(item);
  }

  public SettlementItem getItem(int index){
    return (SettlementItem )items.elementAt(index);
  }

  static final String itemsKey="items";
  static final String batchNumberKey="batchNumber";

  public void save(EasyCursor ezp){
    ezp.saveVector(itemsKey,items);
    if(batchNumber>0){
      ezp.setInt(batchNumberKey,batchNumber);
    }
  }

  public void load(EasyCursor ezp){
    batchNumber=ezp.getInt(batchNumberKey);
    ezp.loadVector(itemsKey,SettlementItem.class);
  }

  public SettlementRequest() {

  }
}