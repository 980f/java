package net.paymate.peripheral;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/peripheral/PinRequest.java,v $</p>
 * <p>Description: collects info needed by pinpads to get pin</p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.3 $
 * Prompt has been purposefully omitted as the target device is the only thing that knows what its display capabilities are like.
 * @todo: [low] make provision for Query.
 */

import net.paymate.awtx.RealMoney;//amount, needed for prompt, may or may not get folded into encryption
import net.paymate.jpos.data.CardNumber; //seems to get folded into encryption

public class PinRequest {
  public RealMoney amount;
  public CardNumber account;
  public boolean isRefund; //giving money back to cardholder

  public PinRequest set(CardNumber account){
    this.account=account;
    return this;
  }

  public PinRequest set(RealMoney amount){
    this.amount=amount;
    return this;
  }

  public PinRequest setRefund(boolean isRefund){
    this.isRefund=isRefund;
    return this;
  }

  public PinRequest() {
    amount=RealMoney.Zero();
    account=new CardNumber();
    isRefund=false;
  }

  public static PinRequest From(CardNumber account,RealMoney amount,boolean isRefund){
    PinRequest newone=new PinRequest();
    return newone.set(account).set(amount).setRefund(isRefund);
  }

}
//$Id: PinRequest.java,v 1.3 2003/05/28 15:24:30 andyh Exp $