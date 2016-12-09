/**
* Title:        CreditReply
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CreditReply.java,v 1.7 2000/12/09 02:03:43 andyh Exp $
*/
package net.paymate.connection;
// import ;
public class CreditReply extends CardReply {
  public ActionType Type(){
    return new ActionType(ActionType.credit);
  }

  public CreditReply(){
    super(); //pro forma
  }

  public boolean NeedsSignature(){
    return true; //temporary hack !!!!
  }

}
//$Id: CreditReply.java,v 1.7 2000/12/09 02:03:43 andyh Exp $
