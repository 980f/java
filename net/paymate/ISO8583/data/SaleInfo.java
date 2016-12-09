/* $Id: SaleInfo.java,v 1.25 2001/10/15 22:39:44 andyh Exp $ */
package net.paymate.ISO8583.data;

import net.paymate.util.*;
import net.paymate.awtx.RealMoney;

public class SaleInfo implements isEasy {
  public SaleType   type;
  public String preapproval; //offline approval code
  public SaleMoney  money;

  final static String preapprovalKey="approval";

  public void Clear(){
    type .Clear();
    money.Clear();
    preapproval="";
  }

  public RealMoney Amount(){
    return money.amount;
  }

  public boolean wasSwiped(){
    return type.source.is(EntrySource.Swiped);
  }

  public boolean wasScanned(){
    return type.source.is(EntrySource.MICRed);
  }

  public boolean isOffline(){
    return Safe.NonTrivial(preapproval);
  }

  public boolean isReturn(){
    return type.op.is(TransferType.Return);
  }

  public void load(EasyCursor ezp){
    type .load(ezp);
    money.load(ezp);
    preapproval=ezp.getString(preapprovalKey);
  }

  public void save(EasyCursor ezp){
    type .save(ezp);
    money.save(ezp);
    ezp.setString(preapprovalKey,preapproval);
  }

  public SaleInfo(){
    type  = new SaleType() ;
    money = new SaleMoney();
    preapproval="";
  }

  public String amountHint(){
    return type.amountHint();
  }

  public String noAmountHint(){
    return type.noAmountHint();
  }


  public String spam(){
    return " Sale="+type.spam()+'\n'+money.spam()+ (isOffline()?("\nAPproved:"+preapproval):"");
  }

  public TextList toSpam() {
    TextList tl = new TextList();
    ErrorLogStream.objectDump(type, "type", tl);
    ErrorLogStream.objectDump(money, "money", tl);
    tl.add("preapproval="+preapproval);
    return tl;
  }

  public void setto(SaleInfo sale) {
    // +_+ push down
    this.type.op.setto(sale.type.op.Value());
    this.type.payby.setto(sale.type.payby.Value());
    this.type.source.setto(sale.type.source.Value());
    this.preapproval = sale.preapproval;
    this.money.amount.setto(sale.Amount().Value());
    this.money.cashback.setto(sale.money.cashback.Value());
    this.money.tendered.setto(sale.money.tendered.Value());
  }

  public SaleInfo(SaleInfo sale){
    type  = new SaleType(sale.type) ;
    money = new SaleMoney(sale.money);
    preapproval=new String(sale.preapproval);
  }

}
//$Id: SaleInfo.java,v 1.25 2001/10/15 22:39:44 andyh Exp $
