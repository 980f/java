package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/BatchLineItem.java,v $
 * Description:  subset of Transaction data for Listings
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Name:  $ $Revision: 1.37 $
 * @todo: consider adding MerchRef info
 */

import net.paymate.util.*;
import net.paymate.jpos.data.*;
import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.awtx.*;
import net.paymate.data.AuthResponse;
import net.paymate.lang.StringX;
import java.util.*;
import net.paymate.lang.Fstring;

public class BatchLineItem implements isEasy {
  private static final boolean legacy=true; //emit legacy fields, relevent on server only
  public UTC date=new UTC(); //terminal's time of transaction.
  private SaleInfo sale;
  private SettleInfo settleinfo;
  private boolean voided=false;
  private StandinState si=new StandinState();
  private AuthResponse authresp=AuthResponse.mkTrivial();//prevent nulls.

  public BatchLineItem setServerStoodin(boolean wasstoodin){
    si.setServer(wasstoodin);
    return this;
  }

  public String approval(){
    return authresp.authcode();
  }
  public String stan(){
    return sale.stan.toString();
  }
  public String TypeColData = "CC";//2+1=3 //+_+ should add to card number.
  public CardNumber card=new CardNumber();
  public MICRData check= new MICRData();

  public boolean wasManual(){
    return sale.type.source.is(EntrySource.Manual);
  }
  public boolean isCheck(){
    return sale.payTypeIsCheck();
  }

  public boolean isCard(){
    return sale.payTypeIsCard();
  }
  public boolean isReturn(){
    return sale!=null && sale.isReturn();
  }
  public TransferType TransferType(){
    return sale.type.op;
  }
  public boolean isPending(){
    return authresp.isPending() ;
  }
  public boolean isAuthed(){
    return authresp.authed();
  }
  public boolean isVoided(){
    return voided;
  }
  public boolean isDeclined(){
    return authresp.isDeclined();
  }
  public boolean isAccepted(){
    return authresp.isApproved() || si.wasStoodin();
  }
  public boolean reallyApproved(){
    return authresp.isApproved();
  }
  ///////////////////////////
  public BatchLineItem setVoided(boolean wasvoided){
    voided=wasvoided;
    return this;
  }
  public BatchLineItem setAuth(AuthResponse auth){
    this.authresp=auth; //not copied as auth's are not modified after creation
    return this;
  }
  public BatchLineItem setSettleInfo(SettleInfo si) {
    settleinfo = si;
    return this;
  }

  public RealMoney finalAmount(){
    return settleinfo!=null? settleinfo.Amount(): sale!=null? sale.Amount() : RealMoney.Zero();
  }

  public LedgerValue finalNetAmount(){
    return settleinfo!=null? settleinfo.netAmount(): sale!=null? sale.netamount() : LedgerValue.Zero();
  }

  /**
   * @param mf (ab)used for its formatter
   */
  public String forSale(LedgerValue mf){
//    mf.setto(sale.Amount().Value());
//    return mf.Image()+ (sale.isReturn()?"-":" ");//tired of trying to figure out money formats...
    mf.setto(finalAmount().Value());
    return mf.Image()+ (settleinfo.isReturn()?"-":" ");//tired of trying to figure out money formats...
  }

  final static String institutionTagKey ="institutionTag";
  final static String cardKey=    "card";
  final static String checkKey=   "check";
  final static String dateKey=    "reftime";
  final static String stanKey=    "refnum";
  final static String approvalKey="approval";
  final static String authKey=    "auth";
  final static String settleKey=  "settle";
  final static String voidedKey=  "voided";
  final static String saleKey=    "info";


  public void save(EasyCursor ezc){
    if(isCard()){
      ezc.setString(institutionTagKey,TypeColData);
      ezc.setObject(cardKey,card);
    } else if(isCheck()){
      ezc.setObject(checkKey,check);
    }
    ezc.setUTC(dateKey,date);
    if(legacy){
      ezc.setString(stanKey,stan());
      ezc.setString(approvalKey,approval());
    }
    ezc.setObject(authKey,authresp);
    ezc.setBoolean(voidedKey,voided);

    ezc.setObject(saleKey,sale);
    si.save(ezc);
    if(settleinfo != null) {
      ezc.setObject(settleKey, settleinfo);
    }
  }

  public void load(EasyCursor ezc){
    sale=(SaleInfo)ezc.getObject(saleKey,SaleInfo.class);
    settleinfo=(SettleInfo)ezc.getObject(settleKey, SettleInfo.class);
    if(isCard()){
      TypeColData=ezc.getString(institutionTagKey);
      card=(CardNumber)ezc.getObject(cardKey,CardNumber.class);
    } else if(isCheck()){
      TypeColData=ezc.getString(institutionTagKey,"CK");
      check=(MICRData)ezc.getObject(checkKey,MICRData.class);
    }

    date=ezc.getUTC(dateKey);
    si.load(ezc);
    ezc.getBlock(authresp,authKey);
    if( ! StringX.NonTrivial(authresp.action() )){//legacy clause-interpret approval code, not all cases distinguishable.
      String approval=ezc.getString(approvalKey);
      String actioncode=ActionCode.Approved; //formerly only approveds were allowed in listing
      if (approval.equals("999999")) {//sever standin for version <=pgsql16 ? looked like it reading code, but never got written into database.
        si.setServer(true);
        actioncode=ActionCode.Pending;
      }
      else if (approval.equals(AuthResponse.DEFAULTAUTHCODE)) {//client standin, standin.localListing function
        si.setServer(true); //even though it might actually be a client standin
        actioncode=ActionCode.Pending;
      }
      else if ( !sale.isReturn() && ! StringX.NonTrivial(approval)) {//si loss,  redone for jumpware but acceptible to PosTerminal
        si.setServer(true);
        actioncode=ActionCode.Declined;
      }
      authresp.setTrio(actioncode,approval,"");
    }
  }

  private final static char space=' ';

  /**
   * @return new FormattedLineItem from most recently set values
   */
  public FormattedLineItem formatted(LocalTimeFormat ltf,LedgerValue mf) {
    StringBuffer fixedStuff=new StringBuffer(40);
    fixedStuff.append(ltf.format(date));
    fixedStuff.append(space);
    fixedStuff.append(Fstring.righted(sale.stan.toString(),6,'0'));
    fixedStuff.append(space);

    if(isCard()){
      fixedStuff.append(TypeColData);
      fixedStuff.append(card.Greeked(" "));
    } else if(isCheck()){
      fixedStuff.append(TypeColData);
      fixedStuff.append(StringX.subString(check.Account,0,4));
    }
    return new FormattedLineItem(String.valueOf(fixedStuff),forSale(mf),' ',FormattedLineItem.justified);
  }

  public BatchLineItem setSaleInfo(SaleInfo sinfo){
    sale = sinfo;
    return this;
  }
  /**
   * USE CAREFULLY! not always what you expect
   * @param request
   * @param reply
   * @return a batch line item that does NOT reflect subsequent changes
   */
  public static BatchLineItem MakeFrom(PaymentRequest request, PaymentReply reply){
    BatchLineItem bli = new BatchLineItem();
    if (request.hasSomeCardInfo()) {
      bli.card = request.card.accountNumber;
    }
    bli.date = reply.refTime;
    bli.sale = request.sale;
    bli.setSettleInfo(SettleInfo.MakeFrom(bli.sale));
    return bli;
  }
  ////////////////////
  // for testing
  public static BatchLineItem FakeOne(int nth){
    BatchLineItem bli=new BatchLineItem();
    bli.date=UTC.New(UTC.Now().getTime()-3000*nth);
//    bli.stan="23"+String.valueOf(nth*11);
    bli.sale= SaleInfo.New(PayType.Credit,TransferType.Sale,true,nth*7+nth%3);
    bli.settleinfo= SettleInfo.New(new SettleOp(SettleOp.Sale), new RealMoney(nth*7+nth%3+1),true);
    bli.sale.setMoney(RealMoney.Zero().setto(nth*17+nth/3));

    if(bli.isCard()){
      bli.card.synthesize("3412302032"+String.valueOf(nth));
      bli.TypeColData = "CC";
    } else if(bli.isCheck()){
      bli.check= MICRData.fromTrack("t314977405t9123215268o143"+nth);
      bli.TypeColData = "CK";
    }
    return bli;
  }

}
//$Id: BatchLineItem.java,v 1.37 2004/02/25 22:31:06 andyh Exp $