package net.paymate.connection;
/**
 * Title:        ReversalReply<p>
 * Description:  <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ReversalReply.java,v 1.11 2001/07/06 18:56:37 andyh Exp $
 */

import net.paymate.awtx.RealMoney;
import net.paymate.util.*;
import net.paymate.jpos.data.*;
import net.paymate.ISO8583.data.*;

public class ReversalReply extends FinancialReply implements isEasy {

  public ActionType Type(){
    return new ActionType(ActionType.reversal);
  }
//+_+ replace following with Request as reconstructed from tranjour.
  public RealMoney originalAmount =new RealMoney();
  public MSRData card=new MSRData();

  protected final static String originalAmountKey="originalAmount";

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setLong(originalAmountKey,originalAmount.Value());
    if(card!=null){
      card.save(ezp);
    }
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    originalAmount=new RealMoney(ezp.getLong(originalAmountKey));
    card=new MSRData(ezp);
  }

  public ReversalReply simulate(String respCode, String approval,
       String catid, TransactionID transid, MSRData card,
      RealMoney originalAmount) {
    this.card = card;
    this.originalAmount = originalAmount;
    super.simulate(respCode, approval, catid, transid);
    return this;
  }

}
//$Id: ReversalReply.java,v 1.11 2001/07/06 18:56:37 andyh Exp $
