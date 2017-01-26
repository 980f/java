/**
* Title:        CardReply
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CardReply.java,v 1.10 2000/12/09 02:03:43 andyh Exp $
*/
package net.paymate.connection;

public class CardReply extends FinancialReply {
  //common part of credit and debit
  public ActionType Type(){
    return new ActionType(ActionType.unknown);
  }

  public CardReply(){
    super("FAKED!",/*"111199999001",*/"CATermIdGoesHere");//4debug
  }

  public boolean NeedsSignature(){
    return true; //temporary hack !!!!
  }

}
//$Id: CardReply.java,v 1.10 2000/12/09 02:03:43 andyh Exp $
