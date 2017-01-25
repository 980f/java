/* $Id: FinancialRequest.java,v 1.19 2001/10/15 22:40:07 andyh Exp $ */
package net.paymate.connection;
import  net.paymate.ISO8583.data.*;
import  net.paymate.awtx.*;
import  net.paymate.util.*;

public class FinancialRequest extends ActionRequest implements isEasy {

  public ActionType Type(){
    return new ActionType(ActionType.financial);
  }

  public TransferType OperationType(){
    return sale.type.op;
  }

  public boolean isReturn() {
    return sale.type.op.is(TransferType.Return);
  }

  public SaleInfo sale;

  public boolean isFinancial() {
    return true;
  }

  public boolean getsSignature(){
    return false;
  }

  // override for the cases where this is true
  public boolean canStandin() {
    return false;
  }

  public RealMoney Amount(){
    return sale!=null? sale.Amount(): new RealMoney();
  }

  public LedgerValue LedgerAmount(){
    return LedgerValue.New(Amount(),sale.isReturn());
  }

  public void save(EasyCursor ezp){
    sale.save(ezp);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    sale.load(ezp);
    super.load(ezp);
  }

  public FinancialRequest(SaleInfo sale) {
    this.sale=new SaleInfo(sale);
  }

  // required for object instantiation on the server
  public FinancialRequest() {
    sale=new SaleInfo();
  }
}
//$Id: FinancialRequest.java,v 1.19 2001/10/15 22:40:07 andyh Exp $
