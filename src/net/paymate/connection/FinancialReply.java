package net.paymate.connection;

/**
* Title:        FinancialReply
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: FinancialReply.java,v 1.41 2001/07/18 22:00:16 andyh Exp $
*/

import net.paymate.ISO8583.data.*;
import net.paymate.util.*;
/**
The errors are intended for the clerk. They are not to be displayed
directly to the client. The text is direct, the clerk needs to add politeness.
If there are any errors then the request is NOT approved.

The content of Approval is the image of the various types of actual
approval codes.

*/

public class FinancialReply extends ActionReply implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.financial);
  }

  protected final static String ApprovalKey="Approval";
  protected static final String CATIDKey= "CATermID";
  protected static final String TIDKey= "TID";

  private String Approval="XXXXXX";//field38
  public TransactionID tid;
  public String CATermID; //must be on receipts,nickname of store

  public String Approval(){
    if(Safe.NonTrivial(Approval)&& !Approval.equals("      ")){
      return Approval;
    }
    if(TextList.NonTrivial(Errors)){
      return Errors.itemAt(0);
    }
    return "N/A";
  }

  public String setApproval(String apptext){
    Approval=Safe.trim(apptext,true,true);
    return Approval();
  }

  public boolean NeedsSignature(){
    return false; //typically not needed.
  }

  protected FinancialReply(){
    Approval="ERROR!"; //should always be overwritten
    tid=TransactionID.Zero();
    CATermID="CATermId";
  }

  protected FinancialReply(String Approval, String CATermID, TransactionID tid){
    this.Approval=Approval;
    this.CATermID=CATermID;
    this.tid = tid;
  }

  protected FinancialReply(String Approval, String CATermID){
    this(Approval, CATermID, TransactionID.Zero());
  }

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setString(ApprovalKey,Approval);
    ezp.setString(CATIDKey,CATermID);
    tid.save(ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    Approval=ezp.getString(ApprovalKey);
    CATermID=   ezp.getString(CATIDKey);
    if(tid == null) {
      tid=TransactionID.Zero();
    }
    tid.load(ezp);
  }

  public FinancialReply simulate(String respCode, String approval, String CATermID, TransactionID transid) {
    if(respCode != null) {
      setResponse(respCode);
    }
    Approval= Safe.TrivialDefault(approval, transid.stan());
    status = new ActionReplyStatus(ActionReplyStatus.Success);
    if(CATermID != null) {
      this.CATermID = CATermID;
    }
    if (transid != null) {
      tid = transid;
    }
    return this;
  }

}
//$Id: FinancialReply.java,v 1.41 2001/07/18 22:00:16 andyh Exp $
