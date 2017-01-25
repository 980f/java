/**
* Title:        DebitReply
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DebitReply.java,v 1.7 2000/11/30 04:11:48 andyh Exp $
*/
package net.paymate.connection;
// import ;
public class DebitReply extends CardReply {
  public ActionType Type(){
    return new ActionType(ActionType.debit);
  }

  public DebitReply(){
    //
  }

//  public DebitReply(String apr,String stan){
//    super(apr,stan);
//  }

}
//$Id: DebitReply.java,v 1.7 2000/11/30 04:11:48 andyh Exp $
