/**
* Title:        LoginRequest
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: LoginRequest.java,v 1.4 2000/08/31 21:48:39 andyh Exp $
*/
package net.paymate.connection;

public class LoginRequest extends AdminRequest {
  public ActionType Type(){
    return new ActionType(ActionType.clerkLogin);
  }
  //but doesn't add any fields beyond the base

}
//$Id: LoginRequest.java,v 1.4 2000/08/31 21:48:39 andyh Exp $
