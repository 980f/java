/* $Id: PaymentType.java,v 1.5 2000/06/04 20:37:36 alien Exp $ */
package net.paymate.terminalClient;
import  net.paymate.ISO8583.data.PayType;

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

//$Id: PaymentType.java,v 1.5 2000/06/04 20:37:36 alien Exp $
