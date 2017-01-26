package net.paymate.terminalClient.PosSocket.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/UTFrequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.*;

import net.paymate.util.*;

public class UTFrequest {
  public String msgtype;
  public String routing;
  public String merchid;
  public String terminal;
  public boolean multi;
  public int stan; //pt calls this sequence number, but that name is too ambiguous; oh, and stan isn't confusing? it is after all the sequence number in the batch, so the original name was correct.
  public String txnclass;
  public PTTransactionCodes trancode;
  public int uselessRefNumber; //'last retrieval reference number'
  public int origRefNumber;   // refnumber of what to void
  public CardNumber origCardnumber; //hcard number of orig transaction.

  public int PinCap;
  public int edscode;
  public MSRData card= new MSRData();
  public RealMoney amount=new RealMoney();
  public SaleType sale;
  public int industryCode;
  public String ExternalTranIdent;
  public int employeenum;
  public boolean cashout;
  public int gcseqnum; //which of a multi-gc issuance this is
  public int gcblocksize;//multi-gc how many there will be
  public PINData pin;
  public RealMoney cashback=new RealMoney();
  public int invoicenumber;
  public long itemcode;

  public SaleInfo SaleInfo(){
    SaleInfo wad= new SaleInfo();
    wad.type=sale;
    wad.setMoney(amount);
    wad.stan= STAN.NewFrom(stan);
    return wad;
  }


  public UTFrequest parseCreditvoid(VisaBuffer vb){
    //picks up just after trancode
    uselessRefNumber=vb.getDecimalFrame(/*8*/);//useless 'previous retrieival reference number'
    origRefNumber=vb.getDecimalFrame(/*8*/);//waht to void
    origCardnumber= new CardNumber(vb.getROF());
    //++++ sale not set!!!
      sale=trancode.SaleType(edscode=1);//eds is unknown
    return this;
  }

  public UTFrequest parseSVCblock(VisaBuffer vb){//  C INDUSTRY SPECIFIC DATA
    industryCode=vb.getDecimalInt(3);//1 Industry Code Pic 9(3) Ö 014 = Stored Value
    ExternalTranIdent=vb.getFixed(15);//2 External Transaction Identifier Pic X(15) Ö Cash Register transaction identifier
    employeenum=vb.getDecimalInt(10);//3 Employee Number Pic X(10) Ö LJSF
    cashout=vb.getDecimalInt(1)==1;//4 Cash Out Indicator Pic X(1) Ö N
    gcseqnum=vb.getDecimalInt(2);//5 Sequence number of card being issued/activated.
    gcblocksize=vb.getDecimalInt(2);//6 Total number of cards being issued/activated. Pic 9(2)
    return this;
  }

  public UTFrequest parseGcvoid(VisaBuffer vb){
    parseCreditvoid(vb);
    return parseSVCblock(vb);
  }

  /**
   */
  public UTFrequest parseFrom(VisaBuffer vb){
//    vb.dump("utfrq.parseFrom(bv)");
    msgtype=vb.getMsgType();
    routing=vb.getFixed(6);
    merchid=vb.getFixed(4+12);
    terminal=vb.getFixed(3);
    multi= vb.getDecimalInt(1)==2;
    stan=vb.getDecimalInt(6);
    txnclass=vb.getFixed(1);
    trancode=PTTransactionCodes.From(vb.getFixed(2));
    switch(trancode.code()){
      case PTTransactionCodes.CreditVoid: return parseCreditvoid(vb);
      case PTTransactionCodes.GiftcardVoid: return parseGcvoid(vb);
      //??? what other specials ???
    }

    //techulator  may not actually need any of the rest.
    PinCap=vb.getDecimalInt(1);
    edscode=vb.getDecimalInt(2);
    // EDScode=edsdecoder(edscode);
  //card info
    if(card==null){
      card= new MSRData();
    }
    switch (edscode) {
      case 2: {
         card.accountNumber.setto(vb.getROF());
         card.expirationDate.parsemmYY(vb.getROF());
      } break;
      case 3:  card.setTrack(card.T2,vb.getROF()); card.ParseTrack2();
        break;
      case 4:  card.setTrack(card.T1,vb.getROF()); card.ParseTrack1();
        break;
    }

    amount.parse(vb.getROF());
    vb.getROF();

    sale=trancode.SaleType(edscode);
    switch (sale.payby.Value()) {
      case PayType.GiftCard:{
         vb.getROF();
         vb.getROF();
         parseSVCblock(vb);
      } break;
      case PayType.Debit:{
         pin= PINData.Dukpt(vb.getFixed(16),vb.getROF());
         cashback.parse(vb.getROF());
      } break;
      case PayType.Credit:{
         vb.getROF();
         vb.getROF();
         // expect "retail" industry block
         industryCode=vb.getDecimalInt(3);
         invoicenumber=vb.getDecimalInt(6);
         itemcode=vb.getDecimalLong(20);
         vb.getROF();
         vb.getROF();
         vb.getROF();
         vb.getROF();
         vb.getROF();
         vb.getROF();
      } break;
    }
    return this;
  }

public TextList toSpam(TextList decoder){
    if(decoder == null) {
      decoder=new TextList();
    }
    decoder.add("msgtype",this.msgtype);
    decoder.add("routing",this.routing);
    decoder.add("merchid",this.merchid);
    decoder.add("terminal",this.terminal);
    decoder.add(this.multi?"multi":"single","transaction");
    decoder.add("stan",this.stan);
    decoder.add("class",this.txnclass);
    decoder.add("trancode",trancode.toSpam());
    decoder.add("PinCap",this.PinCap);
    decoder.add("EDScode", PTTransactionCodes.edsdecoder(this.edscode));
  //card info
    switch (edscode) {
      case 1: decoder.add("card data source","unknown"); break;
      case 2: {
        decoder.add("cardnum",this.card.accountNumber.Image());
        decoder.add("exp mmYY",this.card.expirationDate.Image());//#diagnostic
      } break;
      case 3: decoder.add("track2",this.card.track(card.T2).Data());
        break;
      case 4: decoder.add("track1",this.card.track(card.T1).Data());
        break;
      default: decoder.add("defective card data source","no further data extracted");
        return decoder;
    }
    decoder.add("amount",this.amount.Image());

    switch (sale.payby.Value()) {
      case PayType.GiftCard:{
        decoder.add("industryCode",this.industryCode);
        decoder.add("ExternalTranIdent",this.ExternalTranIdent);
        decoder.add("employeenum",this.employeenum);
        decoder.add("cashout?",this.cashout);
        decoder.add("gcseqnum",this.gcseqnum);
        decoder.add("gcblocksize",this.gcblocksize);
      } break;
      case PayType.Debit:{
        decoder.add("keyseqnum (hex)",this.pin.ksnImage());
        decoder.add("encoded pin(hex)",this.pin.Image());
        decoder.add("cashback",this.cashback.Image());
      } break;
      case PayType.Credit:{
        decoder.add("industryCode",this.industryCode);
        decoder.add("invoicenumber",this.invoicenumber);
        decoder.add("itemcode",this.itemcode);
      } break;
    }
    return decoder;
  }

  private UTFrequest() {
  //is this needed?
  }

  public static UTFrequest From(VisaBuffer vb){
//    if(vb!=null){
//      vb.dump("UTFrequest.From(vb)");
//    }
    return new UTFrequest().parseFrom(vb);
  }

  /**
   * @param line is BODY of a visa packet
   */

  public static UTFrequest From(byte[] line){
    return From(VisaBuffer.FrameThis(line));
  }

}
//$Id: UTFrequest.java,v 1.1 2003/12/10 02:16:54 mattm Exp $