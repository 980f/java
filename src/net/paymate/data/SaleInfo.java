package net.paymate.data;
/**
 * Title:        $Source: /cvs/src/net/paymate/data/SaleInfo.java,v $
 * Description:  wad of pieces assoicated with a transaction.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */


import net.paymate.lang.StringX;
import net.paymate.util.*;
import net.paymate.awtx.RealMoney;

public class SaleInfo implements isEasy {
  public SaleType   type;
  public String preapproval; //offline approval code
  public STAN stan; //external transactionid
  private SaleMoney money;

  public boolean haveAmount(){
    return money.isValid();
  }

  private String merchantReference;
  public String merchantReferenceInfo(){
    return merchantReference;
  }

  public SaleInfo setMerchantReferenceInfo(String asInput){
    merchantReference=asInput;//perhaps we will have to escape this someday ...
    return this;
  }

  public SaleInfo setMoney(SaleMoney money){
    this.money=money;
    return this;
  }

  public SaleInfo setMoney(RealMoney amount){
    return setMoney(SaleMoney.fromAmount(amount));
  }

  public SaleInfo clearMoney(){
    if(money!=null){
      money.Clear();
    }
    return this;
  }

  public void Clear(){
    type .Clear();
    money.Clear();
    preapproval="";
    setMerchantReferenceInfo(null);
    stan.Clear();
  }

  public RealMoney Amount(){
    return money.amount;
  }

  public LedgerValue netamount(){
    return LedgerValue.New(Amount(),isReturn());
  }

  public boolean typeIsKnown(){
    return type!=null && type.isKnown();
  }

  public TransferType TransferType(){
    return type.op;
  }

  public boolean wasSwiped(){
    return type.source.is(EntrySource.Machine) && payTypeIsCard();
  }

  public boolean payTypeIsCard() {
    return type.payby.is(PayType.Credit) ||
        type.payby.is(PayType.Debit) ||
        type.payby.is(PayType.GiftCard);
  }

  public boolean payTypeIsCheck() {
    return type.payby.is(PayType.Check);
  }

  public boolean wasScanned(){
    return type.source.is(EntrySource.Machine) && payTypeIsCheck();
  }

  public boolean hasPreapproval(){
    return StringX.NonTrivial(preapproval);
  }

  public boolean isReturn(){
    return typeIs(TransferType.Return);
  }

  /** @return whether the operation is the most basic of Sales.
   * @todo add clauses to omit forces , i.e. get pickier
   */
  public boolean isSimpleSale(){
    return typeIs(TransferType.Sale);
  }

  public boolean typeIs(int transfertype){
    return (type!=null&&type.op!=null)? type.op.is(transfertype): (transfertype==TransferType.Unknown);
  }

//the following can share the same branch, they don't overlap
  private final static String typeKey="sale";
  private final static String moneyKey="sale";
  final static String preapprovalKey="approval";
  private final static String merchKey="merchRef";

  public void load(EasyCursor ezp){
    type=(SaleType)ezp.getObject(typeKey,SaleType.class);
    money=(SaleMoney)ezp.getObject(moneyKey,SaleMoney.class);
    preapproval=ezp.getString(preapprovalKey);
    setMerchantReferenceInfo(ezp.getString(merchKey));
    stan.load(ezp);
  }

  public void save(EasyCursor ezp){
    ezp.setObject(typeKey,type);
    ezp.setObject(moneyKey,money);
    ezp.setString(preapprovalKey,preapproval);
    ezp.setString(merchKey,merchantReference);
    stan.save(ezp);
  }

  public String youPay(){
    //too crude   return "You "+ ( isReturn() ? "Get:" : "Pay:" ) + money.amount.Image();
    return amountHint()+" "+money.amount.Image();
  }

  public String amountHint(){
    return type.amountHint();
  }

  public String noAmountHint(){
    return type.noAmountHint();
  }

  public String spam(){
    return toSpam().asParagraph();
  }

  public TextList toSpam() {
    return EasyCursor.makeFrom(this).toSpam(null);
  }

  public SaleInfo setto(SaleInfo sale) {
    type = new SaleType(sale.type);
    money = new SaleMoney(sale.money);
    preapproval = new String(sale.preapproval);
    setMerchantReferenceInfo(sale.merchantReferenceInfo());
    stan = new STAN(sale.stan.value());
    return this;
  }

  public SaleInfo(SaleInfo sale){
    if(sale!=null){
      setto(sale);
    }
  }

  public SaleInfo(){
    type  = new SaleType() ;
    money = new SaleMoney();
    preapproval="";
    stan=new STAN();
    merchantReference="";
  }

  public static SaleInfo New(int pt,int tt,boolean manual,int tid){
    SaleInfo newone=new SaleInfo();
    newone.type=SaleType.New(pt,tt,manual);
    newone.stan=new STAN(tid);
    return newone;
  }

  public static SaleInfo fakeOne(){
    return New(PayType.Credit,TransferType.Sale,true,92010).setMoney(SaleMoney.fakeOne());
  }

}
//$Id: SaleInfo.java,v 1.1 2003/10/25 20:34:20 mattm Exp $

