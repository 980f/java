package net.paymate.connection;
/**
* Title:        ReceiptStoreReply
* Description:  store receipt on server <p>
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ReceiptStoreReply.java,v 1.6 2001/07/06 18:56:37 andyh Exp $
*/

import net.paymate.util.*;

public class ReceiptStoreReply extends ActionReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.receiptStore);
  }
  boolean storedOk;

//  protected ReceiptStoreReply(boolean storedOk){
//    this.storedOk=storedOk;
//  }

  // default one for transmission
  protected ReceiptStoreReply(){
    this.storedOk=false;
  }

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setBoolean("storedOk",storedOk);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    storedOk=ezp.getBoolean("storedOk");
  }

}
//$Id: ReceiptStoreReply.java,v 1.6 2001/07/06 18:56:37 andyh Exp $
