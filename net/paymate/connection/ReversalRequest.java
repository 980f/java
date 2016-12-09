package net.paymate.connection;
/**
 * Title:        $Source: /cvs/src/net/paymate/connection/ReversalRequest.java,v $
 * Description:  <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ReversalRequest.java,v 1.16 2001/10/02 17:06:37 mattm Exp $
 */


import  net.paymate.ISO8583.data.*;
import net.paymate.util.*;

public class ReversalRequest extends FinancialRequest implements isEasy {//+_+ AdminRequest would have been simpler. (admin is not financial; reversal is)

  public ActionType Type(){
    return new ActionType(ActionType.reversal);
  }

  public TransactionID toBeReversed = null;
  public FinancialRequest failed;

  public boolean isReversal() {
    return true;
  }

  public boolean byTID(){
    return failed==null;
  }

  public ReversalRequest(TransactionID toBeReversed) {
    super();
    timeoutseconds=20;//+_+ on gandalf!!!
    super.sale.money.Clear();//4 clarity
    super.sale.type.op.setto(TransferType.Reversal);//important
    super.sale.type.payby.setto(PayType.Unknown);//4 clarity
    this.toBeReversed = TransactionID.NewCopy(toBeReversed);
  }

  public ReversalRequest() {
    this(TransactionID.Zero());
  }

  private static final String TRANSIDKEY = "transactionid";
  private static final String reqKEY = "request";
  private static final String typeKey= "byTid";

  public void save(EasyCursor ezp){
    super.save(ezp);
    if(byTID()){
      toBeReversed.saveas(TRANSIDKEY,ezp);
    } else {
      failed.saveas(reqKEY,ezp);
    }
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    if(ezp.getBoolean(typeKey,true)){//default for legacy code
      toBeReversed= TransactionID.NewFrom(TRANSIDKEY,ezp);
      failed=null;
    } else {
      toBeReversed= TransactionID.Zero();
      ezp.push(reqKEY);
      failed=(FinancialRequest)ActionRequest.fromProperties(ezp);
      ezp.pop();
    }
  }

}
//$Id: ReversalRequest.java,v 1.16 2001/10/02 17:06:37 mattm Exp $