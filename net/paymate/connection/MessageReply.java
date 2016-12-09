/**
* Title:        $Source: /cvs/src/net/paymate/connection/MessageReply.java,v $
* Description:  to send presentable info to the client INSTEAD of what it expects
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: MessageReply.java,v 1.13 2001/07/06 18:56:37 andyh Exp $
*/
package net.paymate.connection;

import net.paymate.util.*;

public class MessageReply extends ActionReply implements isEasy {
  public String body;
  public ActionType Type(){
    return new ActionType(ActionType.message);
  }

  protected MessageReply(String body){
    this.body=body;
  }

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setString("body",body);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    body=ezp.getString("body");
  }

}
//$Id: MessageReply.java,v 1.13 2001/07/06 18:56:37 andyh Exp $
