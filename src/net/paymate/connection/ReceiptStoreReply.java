package net.paymate.connection;
/**
* Title:        $Source: /cvs/src/net/paymate/connection/ReceiptStoreReply.java,v $
* Description:  return whether receipt got stored on server
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.10 $
*/

import net.paymate.util.*;

public class ReceiptStoreReply extends ActionReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.receiptStore);
  }

  public ReceiptStoreReply(){
//    for reflective instantiation
  }

}
//$Id: ReceiptStoreReply.java,v 1.10 2002/04/30 18:55:34 andyh Exp $
