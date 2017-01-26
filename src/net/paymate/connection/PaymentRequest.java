package net.paymate.connection;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/connection/PaymentRequest.java,v $</p>
 * <p>Description: 2nd generation FinancialRequest, replaces that hierarchy with a flat class </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.17 $
 */
import  net.paymate.awtx.*;
import  net.paymate.util.*;
import  net.paymate.data.*;
import  net.paymate.lang.*;

import net.paymate.jpos.data.PINData;
import net.paymate.jpos.data.MSRData;

public class PaymentRequest extends ActionRequest implements isEasy {
  public SaleInfo sale;
  public MSRData card;
  public PINData pin;//only valid when paytype is debit
  TxnReference original;//only valid for modication type transactions.

  final static String cardKey="card";

  public String refNum(){//of txn to modify
    return original.refNum();
  }

  public STAN stan2modify(){
    return original.STAN();
  }

  public void setRefnum(String userinput){//of txn to modify
    if(original==null){
      original= TxnReference.New();//but expect it to be bad.
    }
    original.httn = new STAN(userinput);
  }

  public void setAuthRRN(String authrrn) {//of txn to modify
    if(original==null){
      original= TxnReference.New();//but expect it to be bad.
    }
    original.authrrn = authrrn;
  }

 /**
  * @return request is valid if it APPEARS that we have enough info to make the request
  */
  public boolean isValidVoid() {//of txn to modify
    return original!=null && original.looksValid();
  }

  public boolean findByTxnid(){
    return originalTxnid().isValid();
  }

  /**
   * @return the original transaction's id, or an invalid one.
   */
  public Txnid originalTxnid() {//of txn to modify
    return original!=null ? original.txnId : new Txnid();
  }

  ///////////////////////////////
  // transport

  public PaymentRequest(PaymentRequest old){
    this(old.sale, old.card,old.pin,old.original);
  }

  private PaymentRequest(SaleInfo sale, MSRData card,  PINData pin, TxnReference original){
    this.sale=new SaleInfo(sale);
    this.card= new MSRData(card);
    this.pin=PINData.Clone(pin);
    this.original= TxnReference.clone(original);
    if(sale.type.source.is(EntrySource.Unknown)){
      sale.type.setto(new EntrySource(card.looksSwiped()?EntrySource.Machine:EntrySource.Manual));
    }
  }

  private PaymentRequest(SaleInfo sale, MSRData card,PINData pin){
    this(sale,card,pin,TxnReference.New());
  }

  private PaymentRequest(SaleInfo sale, MSRData card){
    this(sale,card, PINData.Null(),TxnReference.New());
  }

  private PaymentRequest(SaleInfo sale) {
    this(sale, new MSRData());
  }

  /*only ActionRequest needs this*/ PaymentRequest() {
    this(new SaleInfo());
  }

  public static PaymentRequest ResurrectFrom(SaleInfo sale, MSRData card,  PINData pin,TxnReference original){
   return new PaymentRequest(sale,card,pin,original);
  }

  public static PaymentRequest DebitRequest(SaleInfo sale, MSRData card, PINData pin){
    return new PaymentRequest(sale,card,pin);
  }

  public static PaymentRequest GiftCardRequest(SaleInfo sale, MSRData card){
    return new PaymentRequest(sale,card);
  }

  public static PaymentRequest CreditRequest(SaleInfo sale, MSRData card){
    return new PaymentRequest(sale,card);
  }

  private PaymentRequest setModification(TxnReference original,RealMoney finalAmount) {
    this.original= original;//+_+ shoudl copy???
    sale.clearMoney();//4 clarity
    sale.type.payby.setto(PayType.Unknown);//4 clarity
    sale.Amount().setto(finalAmount);
    sale.type.op.setto(sale.Amount().NonTrivial()? TransferType.Modify : TransferType.Reversal);//important
    return this;
  }

  public static PaymentRequest Modify(TxnReference original,RealMoney finalAmount) {
    PaymentRequest newone=new PaymentRequest();
    return newone.setModification( original,finalAmount);
  }

  public static PaymentRequest Void(TxnReference original) {
    return Modify(original,RealMoney.Zero());
  }

  public static PaymentRequest Void(Txnid original) {
    return Void(TxnReference.New(original));//+_+ should copy for safety
  }

  /**
   * make a request to reverse the reply givne
   */
  public static PaymentRequest Void(PaymentReply originalReply){
    return Void(originalReply.tref());//this propagates legacy flag
  }

  public static PaymentRequest Null(){
    return new PaymentRequest();//+_+
  }

  public void save(EasyCursor ezp){
    sale.save(ezp);

    ezp.setBlock(card,cardKey);//+_+ also check for nontrivial
    if(PINData.NonTrivial(pin)){
      pin.save(ezp);
    }
    original.save(ezp);
    super.save(ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);//done first to pick up revision info
    sale.load(ezp);
    ezp.getBlock(card,cardKey);
    pin.load(ezp);
    original=TxnReference.New(ezp);
  }

  public boolean getsSignature(){
    return PaymentType().is(PayType.Credit);// only CreditRequest gets sigs
  }

  public ActionType Type(){
    return new ActionType(ActionType.payment);
  }

  public boolean isModifiable(){
    if( PaymentType().is(PayType.Credit) && card.seemsToBeCredit()) {
      switch(OperationType().Value()) {
        case TransferType.Sale:
        case TransferType.Force:
        case TransferType.Authonly:
          return true;
      }
    }
    return false;
  }

  public boolean canStandin() {//@todo: use isModifiable()
    if( PaymentType().is(PayType.Credit) && card.seemsToBeCredit()) {
      switch(OperationType().Value()) {
        case TransferType.Sale:
        case TransferType.Force:
        case TransferType.Authonly:
          return true;
        case TransferType.Modify:
          return true;//shall we only allow modify if terminal is 'external' ???
        default: return false;
      }
    } else
      return false;
  }

  public TransferType OperationType(){
    return sale.type.op;
  }

  public PayType PaymentType(){
    return sale.type.payby;
  }

  public boolean isReturn() {
    return sale.typeIs(TransferType.Return);
  }
  public boolean isVoider(){
    return sale.typeIs(TransferType.Reversal);
  }
  public boolean isModify() {
    return sale.typeIs(TransferType.Modify);
  }

  public boolean hasSomeCardInfo(){
    return MSRData.NonTrivial(card);// card.isComplete();//+_+ ad hoc
  }
  public boolean hasGoodCardInfo(){
    return card.isComplete();//+_+ ad hoc
  }

  public boolean hasPin(){
    return PINData.NonTrivial(pin);
  }
  public boolean hasApprovalCode(){
    return sale!=null && StringX.NonTrivial( sale.preapproval);
  }
  public TxnReference TxnReference(){//we know everything except database index number.
    return TxnReference.New(terminalid,sale.stan,requestInitiationTime);
  }

  /**
   * @return whether money moves due to a request with @param tt type
   * added to skip checking queries and voids for duplciation, retries of either
   * are infintiely acceptible.
   */
//  public boolean isMoneyTxn() {//@todo: evaluate new operations to see if htey need to be included here
//    return sale.type.op.is(TransferType.Sale) || sale.type.op.is(TransferType.Return);
//  }

  public boolean createsTxn() {//@todo: evaluate new operations to see if htey need to be included here
    try {
      switch (sale.type.op.Value()) {
        case TransferType.Sale:
        case TransferType.Return:
        case TransferType.Force:
        case TransferType.Authonly:
          return true;
        default:
          return false;
      }
    }
    catch (Exception ex) {
      return false; //hideously defective request
    }
  }

  public boolean modifiesTxn() {//@todo: do usage analysis, try to discard.
    try {
      switch (sale.type.op.Value()) {
        case TransferType.Reversal:
        case TransferType.Modify:
          return true;
        default:
          return false;
      }
    }
    catch (Exception ex) {
      return false; //hideously defective request
    }
  }

  public RealMoney Amount(){
    return sale!=null? sale.Amount(): RealMoney.Zero();
  }

  public LedgerValue LedgerAmount(){
    return LedgerValue.New(Amount(),sale.isReturn());
  }

}
//$Id: PaymentRequest.java,v 1.17 2004/02/03 09:00:47 mattm Exp $
