package net.paymate.connection;
//stale code? should be a payment type nt an action type
/**
* Title:        $Source: /cvs/src/net/paymate/connection/CheckReply.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: CheckReply.java,v 1.12 2001/07/06 18:56:36 andyh Exp $
*/

import net.paymate.util.*;

public class CheckReply extends FinancialReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.unknown);
  }

  public CheckReply(){
    //
  }

  public String ManagerOverrideData; //stuff to send back to ntn on manager approavl of denied check

  public void save(EasyCursor ezp){
    ezp.setString("ManagerOverrideData",ManagerOverrideData);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    ManagerOverrideData=ezp.getString("ManagerOverrideData");
    super.load(ezp);
  }

}
//$Id: CheckReply.java,v 1.12 2001/07/06 18:56:36 andyh Exp $
