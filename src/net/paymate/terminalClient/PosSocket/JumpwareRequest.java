package net.paymate.terminalClient.PosSocket;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/terminalClient/PosSocket/JumpwareRequest.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import net.paymate.connection.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.RealMoney;
import net.paymate.util.TextList;
import net.paymate.data.*;
import net.paymate.lang.StringX;

class JumpwareRequest {
  /**
 * if picky then only stuff for Jumpware is allowed thru'
 * if not then unknown input is passed to asciiFormatter.
 */

  static int memoField=10;//changed from 1 to 10 per discussion with JL on 2004 0107.
  TextList fields;

  String Field(int fieldnum){//make one based psuedo array
    return fields.itemAt(fieldnum-1);
  }
  PaymentRequest request;

  private int getIntField(int index){
    return StringX.parseInt(Field(index));
  }

//parent.termInfo.id()
  protected TxnReference getReference(String refnum) {
    if (StringX.NonTrivial(refnum)) {
      return TxnReference.New().setStan(STAN.NewFrom(refnum));
    } else {
      return TxnReference.New(); //a defective one
    }
  }

  private boolean insertCardInfo(int tracknum,String carddata) {
    switch (tracknum) {
      case 0: { //account '+' mmyy: (reverse order of track2 data)
        int cutat = StringX.cutPoint(carddata, '+');
        request.card.accountNumber.setto(StringX.subString(carddata, 0, cutat));
        request.card.expirationDate.parsemmYY(StringX.restOfString(carddata, cutat + 1));
        request.card.ServiceCode = "101"; //to fixup for 'offline manual entry' bug. this may be moot.
        //invalid content will get audited by authorizer, we let it pass.
        request.sale.type.setto(new EntrySource(EntrySource.Manual)); //track will be erased later, when KeyedIn is discovered.
      }
      break;
      case 1: {
        request.card.setTrack(MSRData.T1, carddata);
        request.sale.type.setto(new EntrySource(EntrySource.Machine));
      }
      break;
      case 2: {
        request.card.setTrack(MSRData.T2, carddata);
        request.sale.type.setto(new EntrySource(EntrySource.Machine));
      }
      break;
      default:
        return false; //card data not set.
    }
    request.card.ParseFinancial();
    return true;
  }

  private boolean insertCardInfo(){
    return insertCardInfo(StringX.parseInt(Field(4)),Field(5)) ;
  }

  private boolean insertAVS(){
    //field 11 zip, 12 for street
    request.card.setAVSAddress(Field(12)).setZip(Field(11));
    return request.card.avsInfo().NonTrivial();
  }

  private void setMerchantInfo(){
    request.sale.setMerchantReferenceInfo(Field(memoField));
  }

  private void setMoney1(){
    request.sale.setMoney(new RealMoney(Field(6)));
  }

  private void addMoneyField( String field){
    RealMoney addAmount=new RealMoney(field);
    if(addAmount.NonTrivial()){
      request.sale.Amount().add(addAmount);
    }
  }

  /**
   * add tip provided during regular auth
   */
  private boolean tipit() {
    boolean rmode = false;
    RealMoney tipAmount = new RealMoney(Field(17));
    if (tipAmount.NonTrivial()) {
      request.sale.Amount().add(tipAmount);
      return true;
    }
    return false;
  }

  /**
   * tip sent in after initial auth.
   */
  private PaymentRequest mkApplyTip() {
    RealMoney finalAmount = RealMoney.Zero();
   // finalAmount.setto(getIntField(8));//#orig auth amount NOT the usual money field
    finalAmount.setto(getIntField(6));//#actually it is in the usual place, the written spec is *useless*
    RealMoney tipAmount = RealMoney.Zero();
    tipAmount.setto(getIntField(17));
    finalAmount.add(tipAmount);
    return PaymentRequest.Modify(getReference(Field(14)),finalAmount);
  }

  private void mkCreditRequest(int tt) {
    request = PaymentRequest.Null();
    request.sale.type.setto(new PayType(PayType.Credit));
    request.sale.type.setto(new TransferType(tt));
    setMerchantInfo();
    insertCardInfo();
    insertAVS();
    setMoney1();
    request.sale.stan = new STAN(Field(14));
    //@AVS@ pick up AVS data here
  }

  private void mkDebitRequest(int tt) {
    request = PaymentRequest.Null();
    request.sale.type.setto(new PayType(PayType.Debit));
    request.sale.type.setto(new TransferType(tt));
    setMerchantInfo();
    insertCardInfo();
    //no AVS ever on debit. ignore if present.
    setMoney1();
    request.pin=PINData.Dukpt(Field(13),Field(20));
    request.sale.stan = new STAN(Field(14));
  }

  private PaymentRequest financialRequest() {
    // @todo: check that the minimum number of fields exists before beginning, or else use defaults and prevent exceptions?
    switch (Field(3).charAt(0)) {
      case '0': { //credit return
        mkCreditRequest(TransferType.Return);
        //and only the common fields are required
      }
      break;
      case '1': { //void
        request= PaymentRequest.Void(getReference(Field(14)));
      } break;
      case '2':{ //force
        mkCreditRequest(TransferType.Force);
        request.sale.preapproval= Field(9);
        //we don't do tips with forces
      } break;
      case 'T':{ //Tips, after an auth
        request= mkApplyTip();
      } break;
      case '5': { //credit sale
        mkCreditRequest(TransferType.Sale);
        tipit();//apply tip if present
      } break;
      case '8':{//debit sale
        mkDebitRequest(TransferType.Sale);
      } break;
      case '9':{//debit return
        mkDebitRequest(TransferType.Return);
      } break;
    }

    return request; //card info invalid
  }

  /**
   * @param fields are ignored as right now server ignores filters
   */
  private BatchRequest localListingRequest() {
    return BatchRequest.Listing();
  }

  /**
   * @param fields are ignored
   * we used "withdetail" so that we can generate a summary report. else the totals are correct but "sales" and "refund" are zero.
   * we dropped making the subtotals as the time consumed was noticable.
   */
  private BatchRequest submit() {
    return BatchRequest.CloseAndSubmit(BatchRequest.WITHOUTDETAIL);
  }

  BypassRequest onError(String errortext){
    return BypassRequest.New(JumpwareFormatter.requestError(errortext));
  }

  ActionRequest getRequest() {
    ActionRequest ar=null;

    //if first word IS a word then super() it
    char typetoken=StringX.firstChar(Field(2));//retain 4debug
    switch (typetoken) {
      case 'P':
        ar=financialRequest();
        break;
      case 'L':
        ar= localListingRequest();
        break;
      case 'S':
        ar= submit();
        break;
        //abdeikow  are the known tokens that we don't support
      default: { //hook for fiona<->sinet operations, such as ftp.
        ar=onError(StringX.bracketed(JumpwareFormatter.NotSupported, String.valueOf(typetoken)));
      }
    }
    return ar != null ? ar : onError(JumpwareFormatter.NotUnderstood); //unknown tokens in request
  }

  public JumpwareRequest(TextList fields) {
    this.fields=fields;
  }

}
//$Id: JumpwareRequest.java,v 1.13 2004/02/03 18:28:54 andyh Exp $
