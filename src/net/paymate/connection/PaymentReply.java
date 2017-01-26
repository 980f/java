package net.paymate.connection;

/**
* Title:        $Source: /cvs/src/net/paymate/connection/PaymentReply.java,v $
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Revision: 1.8 $
*/

import net.paymate.data.*;//id's
import net.paymate.util.*;
import net.paymate.awtx.RealMoney;
import net.paymate.jpos.data.*;

/**
The errors are intended for the clerk. They are not to be displayed
directly to the client. The text is direct, the clerk needs to add politeness.
If there are any errors then the request is NOT approved.

The content of Approval is the image of the various types of actual
approval codes.

*/

public class PaymentReply extends ActionReply implements isEasy {
//////////////////
// sub-class attributes
  public ActionType Type(){
    return new ActionType(ActionType.payment);
  }

  // from ReversalReply
  //+_+ replace following with complete Request as reconstructed from txn.
  public RealMoney originalAmount =new RealMoney();
  public MSRData card=new MSRData();
  protected final static String originalAmountKey="originalAmount";

  // from DebitReply
  public RealMoney surcharge=RealMoney.Zero();
  private static final String surchargeKey="surcharge";

  // from GiftCardReply
  public StoredValue sv=StoredValue.Zero();//not null or getBlock() won't load it
  final static String svKey="storedValue";
  public RealMoney shortage=RealMoney.Zero();
  final static String shortageKey="shortage";

  public TransferType transferType = new TransferType();
  final static String TTKEY = "transfertype";
  public PayType payType = new PayType();
  final static String PTKEY = "paytype";

/////////////////////////
  private TxnReference tref=TxnReference.New();//load() relies upon its existence.
  private /*protected*/ AuthResponse auth; //the majority of operations go to an authorizer.

  public PaymentReply setAuth(AuthResponse auth){
    this.auth=auth;
    setState(auth.authed());
    if( ! auth.isApproved()){
      Errors.add(auth.message());
    }
    return this;
  }

  /**
   * hack, should publish auth members, not whole object +_+
   */
  public AuthResponse auth(){
    return auth;
  }

  public String authAction(){
    return auth.action();
  }

  public boolean isApproved(){
    return auth.isApproved();
  }

  public String authMessage(){
    return auth.message();
  }

  public TxnReference tref(){
    return tref;
  }
  public PaymentReply setReference(TxnReference tref){
    this.tref=tref;
    return this;
  }

  public String refNum(){
    return tref.refNum();
  }

  public void setReferenceTime(UTC u){
    tref.crefTime=u;
  }

  public UTC refTime(){//overloads ActionReply reftime() to match the related request.
    if(TxnReference.NonTrivial(tref)){
      return tref.refTime();
    } else {
      return refTime;
    }
  }

  /**
   * only public references are hacks to re-encode errors after they have been converted to text.
   */
  public final static String ApprovalErrorText="ERRORS";
  public final static String DeclinedText="Declined";

  /**
   * either approval code or DECLINED or ERRORS
   */
  public String Approval(){
    return auth.isApproved() ? auth.authcode() :
    auth.isDeclined() ? DeclinedText :
    TextList.NonTrivial(Errors) ? ApprovalErrorText : "N/A";
  }

  public PaymentReply setApproval(String apptext){//;legacy
    auth=AuthResponse.mkApproved(apptext).setRefInfo("", "by internal agent");
    return this;
  }

  /**
   * @beware - used by reflection in base class only
   */
  public PaymentReply(){
    auth=AuthResponse.mkTrivial();//was too much work to check null every freaking place.
  }

  private final static String trefKey=  "TxnRef";
  private final static String ApprovalKey="Approval";

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setBlock(auth,ApprovalKey);
    ezp.setBlock(tref,trefKey);
    ezp.setLong(originalAmountKey,originalAmount.Value());
    if(card!=null){
      card.save(ezp);//modified txn's card info
    }
    ezp.setLong(surchargeKey,surcharge.Value());
    ezp.setBlock(sv,svKey);
    ezp.setBlock(shortage,shortageKey);
    ezp.saveEnum(TTKEY, transferType);
    ezp.saveEnum(PTKEY, payType);
  }

  public void load(EasyCursor ezp){//used by client
    super.load(ezp);
    ezp.getBlock(auth,ApprovalKey);
    ezp.getBlock(tref,trefKey);
    originalAmount=new RealMoney(ezp.getLong(originalAmountKey));
    card=new MSRData(ezp);
    surcharge.setto(ezp.getLong(surchargeKey));//added for debit, but no-one feeds info into it yet.
    ezp.getBlock(sv,svKey);
    ezp.getBlock(shortage,shortageKey);
    ezp.loadEnum(TTKEY, transferType);
    ezp.loadEnum(PTKEY, payType);
  }

}
//$Id: PaymentReply.java,v 1.8 2003/12/10 02:16:50 mattm Exp $
