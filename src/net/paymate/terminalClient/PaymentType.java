/* $Id: PaymentType.java,v 1.6 2003/10/25 20:34:24 mattm Exp $ */
package net.paymate.terminalClient;
import  net.paymate.data.PayType;

public class PaymentType extends PayType {

  public boolean tisCredit(){
    return is(PayType.Credit);
  }

  public boolean tisDebit(){
    return is(PayType.Debit);
  }

  public boolean tisCheck(){
    return is(PayType.Check);
  }

  public boolean tisCash(){
    return is(PayType.Cash);
  }

  public void reset(){
    super.setto(PayType.Unknown);
  }

}

//$Id: PaymentType.java,v 1.6 2003/10/25 20:34:24 mattm Exp $
