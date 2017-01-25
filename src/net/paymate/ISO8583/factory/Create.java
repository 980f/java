/**
* Title:        Create
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Create.java,v 1.53 2001/11/17 06:16:57 mattm Exp $
*/
/**
This module exists to translate typed data into message fields.

*/

package net.paymate.ISO8583.factory;

import net.paymate.data.Value;
import net.paymate.connection.*;   //requests
import net.paymate.awtx.*;
import net.paymate.util.*;
import net.paymate.jpos.data.*;    //pieces of requests
import net.paymate.ISO8583.data.*; //more pieces of requests
import net.paymate.database.ours.query.*; // for Tranjour for reversals

public class Create {
  public Protocol protocol=new LegacyProtocol();//@@@ hack

  protected /*static*/ final void Insert(Message msg, CheckIdInfo id){
    if(CheckIdInfo.useAble(id)){
      if(Safe.NonTrivial(id.license.Image())){
        msg.setField(Field.DriversLicense, id.license.Image());//Driver's License 157 an ..28 LLVAR C Required if DL was presented as ID.
      }
      if(id.ssn!=0){
        msg.setField(Field.SocialSecurityNumber,id.ssn);//Social Security Number 168 n 9 C Required if SS# was presented as ID.
      }
      ErrorLogStream.Debug.WARNING("Other ID type:"+id.otherCard.idType.Image());
      if(AltID.NonTrivial(id.otherCard)){
        //... AltIdType should be invalid when there is no number...
        msg.setField(Field.IdentificationType,  id.otherCard.idType.Value());
        msg.setField(Field.OtherCardNumber,     id.otherCard.Number);//Other Card Number 169 an ..28 LLVAR C Required if other ID was presented.
      }
    }
  }

  protected /*static*/ final void Insert(Message msg, MICRData check){
    msg.setField(Field.CheckRouting,          check.Transit);
    msg.setField(Field.CheckingAccountNumber, check.Account );
    msg.setField(Field.CheckNumber,           check.Serial);
    msg.setField(Field.CheckType,             check.checktype); //wag
  }

  protected /*static*/ final boolean Insert(Message msg, MSRData card){
    if(card.isComplete()){
      msg.setField(Field.PrimaryAccountNumber,card.accountNumber.Image());
      msg.setField(Field.ExpirationDate,card.expirationDate.YYmm());
      //card.person isn't used...
      if(card.Cleanup()) {//we have tracks
        msg.setField(Field.Track1Data,card.track(0).Data());
        msg.setField(Field.Track2Data,card.track(1).Data());
      }
      return true;
    } else {
      return false;
    }
  }

  protected /*static*/ final void Insert(Message msg, PINData pin){
    msg.setField(Field.PersonalIdentificationNumberData,pin.Value());
    //maybe?    msg.setField(Field.SecurityRelatedControlInformation,SRCI.Image());//some ambiguous security thing
  }

  protected /*static*/ final void Insert(Message msg, TransactionID tid){ //unique ID stuff
    msg.setField(Field.RetrievalReferenceNumber,tid.RRN());//
    msg.setField(Field.SystemTraceAuditNumber,tid.stan());
    msg.setField(Field.TransmissionDateTime,tid.noyear());//
    msg.setField(Field.LocalTransactionTime,tid.justtime());//
    msg.setField(Field.LocalTransactionDate,tid.justdate());//
    msg.setField(Field.EmployeeNumber,"PMSERVER001");// +_+ this would be a paymate employee ..
  }

  protected /*static*/ final void Insert(Message msg, SaleMoney money){
    msg.setField(Field.TransactionAmount,money.amount);
    msg.setField(Field.CashbackAmount,money.cashback);
  }

  protected /*static*/ final void Insert(Message msg, SaleType sale){

    ProcessingCode proccode=new ProcessingCode();
    proccode.setto(sale.op,sale.payby);
    msg.setField(Field.ProcessingCode,proccode.Value());

    POSEntryMode em =new POSEntryMode(sale.source.Value());
    msg.setField(Field.PointOfServiceEntryMode,em.Value());

    POSConditionCode whodunit=new POSConditionCode(POSConditionCode.Normal,true/*customerpresent*/);
    msg.setField(Field.PointOfServiceConditionCode,whodunit.Value());
  }

  protected /*static*/ final boolean Insert(Message msg, SaleInfo sale){
    Insert(msg,sale.type);
    Insert(msg,sale.money);
    return true; // ---
  }

  public /*static*/ final void Insert(Message msg, TerminalInfo term){
    msg.setField(Field.CardAcceptorTerminalIdentification,term.getNickName());
    msg.setField(Field.CardAcceptorIdentificationCode,term.si.getIdentificationCode());
    msg.setField(Field.CardAcceptorNameLocation,term.si.getNameLocation());
    msg.setField(Field.ControllerType,1);//
//    msg.setField(Field.PrivateDataForISP,term.authid()); // cmomented out due to code change AFTER mainsail was dropped.
  }

  public /*static*/ final Message Check(CheckRequest request){
    Message msg=new Message(protocol);
    Insert(msg,request.sale);
    // we only do sales right now:
    msg.Type=100;

    //omitted for 1st test: courtesy card
    // if("have card"){
      //Insert(msg,request.card)
    // } else
    {
      msg.setField(Field.PrimaryAccountNumber,"0");
    }
    Insert(msg,request.check);
    Insert(msg,request.checkId);
    //Manager override NYI, needs to be a variant of check request???
    //msg.setField(Field.ANSIAdditionalTraceData,request.ManagerOverrideData)
    //
    return msg;
  }

  protected /*static*/ final Message CommonCard(CardRequest request){
    Message msg=new Message(protocol);
    msg.Type=200;
    boolean okay = false;
    //here is where we check whether the "card is swiped" and "card has trackdata" agree
    if(!request.card.Cleanup()){//if no tracks present
      if(request.sale.type.source.is(EntrySource.Swiped)){//but were expected
        request.sale.type.source.setto(EntrySource.KeyedIn);//convert to manual
      }
    }
    okay = Insert(msg,request.sale);
    okay &= Insert(msg,request.card);
    return okay ? msg : null;
  }

  public /*static*/ final Message Credit(CreditRequest request){
    return CommonCard((CardRequest) request);
  }

  public /*static*/ final Message Debit(DebitRequest request){
    Message msg=CommonCard(request);
    Insert(msg,request.pin);
    return msg;
  }


  public /*static*/ final Message ForFinancialRequest(FinancialRequest request){
    if(request instanceof CheckRequest){
      return Check((CheckRequest) request);
    }

    if(request instanceof DebitRequest){
      return Debit((DebitRequest) request);
    }

    if(request instanceof CreditRequest){//only remaining variation is credit.
      return Credit((CreditRequest) request);
    }

    return null; //nukes the caller!!! --- need a 'badMessage'

  }

  public /*static*/ final Message ForLogin(LoginRequest request){
    //4debug: make an 800 message
    Message msg=new Message(protocol);
    msg.Type=800;
    //caller must insert Terminal Info and transaction info
    //+_+ is this all?
    return msg;

  }

  protected /*static*/ final void BlindCopy(Message msg, TxnRow orig, int field){
//    String value= orig.ISO(field); +++
//    if(value!=null){    //reconcile with tranjour
//      msg.setField(field,value);
//    }
  }

  protected /*static*/ final void BlindCopyMoney(Message msg, TxnRow orig, int field){
//    String value= orig.ISO(field); +++
//    if(value!=null){    //reconcile with tranjour
//      value= new RealMoney(value).toString();
//      msg.setField(field,value);
//    }
  }

  /** for autoreversals
  * note that Message.copyField(Message,int) will remove a field if not present int the original
  */
  public /*static*/ final Message ForReversal(TransactionID tid, TerminalInfo term, Message original) {
    Message msg=null; //defer creation until we know what type
    if(original != null) {
      msg=new Message(protocol);
      msg.Type=400;
      int msgtype = original.Type;
      int stan=     Integer.parseInt(original.ValueFor(Field.SystemTraceAuditNumber));
      String time=  original.ValueFor(Field.TransmissionDateTime);

      ReversalData f90=new ReversalData(msgtype, stan, time);
      msg.setField(Field.OriginalDataElements,f90.Value());

      msg.copyField(original,Field.PrimaryAccountNumber);
      //there is one value of processing code that doesn't match its reversal!!
      msg.setField(Field.ProcessingCode,ProcessingCode.Reverse(original.ValueFor(Field.ProcessingCode)));
      msg.copyField(original,Field.TransactionAmount);
      msg.copyField(original,Field.ExpirationDate);
      // below item per Duane Walzer, always manually keyed in, no pin required
      msg.setField(Field.PointOfServiceEntryMode,new POSEntryMode(EntrySource.KeyedIn).Value());//Point of Service Entry Mode 22 n 3 M
      // below item per Duane Walzer, all reversals are manual and customer never present.
      msg.setField(Field.PointOfServiceConditionCode,new POSConditionCode(POSConditionCode.ManualReversal,false).Value());

      msg.copyField(original,Field.Track2Data);//Track-2 Data 35 z..37 LLVAR C Required if present in the
      msg.copyField(original,Field.Track1Data);//Track-1 Data 45 ans..76 LLVAR C ]
      msg.copyField(original,Field.CashbackAmount);//Cashback Amount 147 n 12 M
      msg.copyField(original,Field.EmployeeNumber);//Employee Number 148 an ..16 LLVAR M
      msg.copyField(original,Field.ControllerType);//Controller Type 149 n 3 M Value is 001.
      msg.copyField(original,Field.CheckType);//Check Type 150 n 1 M
      msg.copyField(original,Field.DriversLicense);//Driver's License 157 an ..28 LLVAR C Required if present in the
      msg.copyField(original,Field.CheckRouting);//Check Routing Number 165 ans ..9 LLVAR C Required if present in the
      msg.copyField(original,Field.CheckingAccountNumber);//Checking Account Number 166 an ..19 LLVAR C Required if present in the
      msg.copyField(original,Field.CheckNumber);//Check Number 167 n ..5 LLVAR C Required if present in the
      msg.copyField(original,Field.SocialSecurityNumber);//Social Security Number 168 n 9 C Required if present in the
      msg.copyField(original,Field.OtherCardNumber);//Other Card Number 169 an ..28 LLVAR C Required if present in the
      msg.copyField(original,Field.IdentificationType);//Identification Type 170 an 2 O Same value as in the

      Insert(msg,tid);  //new transaction's ID
      Insert(msg,term);
    }
    return msg;
  }

  /**
  * instead of attempting Tranjour->message conversion
  */
  public /*static*/ final Message ForReversal(TransactionID tid, TerminalInfo term, TxnRow original) {
    Message msg=null; //defer creation until we know what type
//    if(original != null) {
//      msg=new Message();
//      msg.Type=400;
//      //o.d.e. pieces:
//      int msgtype = Integer.parseInt(original.messagetype);
//      int stan=     Integer.parseInt(original.stan);
//      String time= original.transmissiontime;//PRECERT:reversals
//      ReversalData f90=new ReversalData(msgtype, stan, time);//PRECERT:reversals
//      msg.setField(Field.OriginalDataElements,f90.Value());
//
//      BlindCopy(msg,original,Field.PrimaryAccountNumber);
//      //there is one value of processing code that doesn't match its reversal!!
//      msg.setField(Field.ProcessingCode,ProcessingCode.Reverse(original.processingcode));
//      BlindCopyMoney(msg,original,Field.TransactionAmount);
//      BlindCopy(msg,original,Field.ExpirationDate);
//      // below item per Duane Walzer, always manually keyed in, no pin required
//      msg.setField(Field.PointOfServiceEntryMode,new POSEntryMode(EntrySource.KeyedIn).Value());//Point of Service Entry Mode 22 n 3 M
//      // below item per Duane Walzer, all reversals are manual and customer never present.
//      msg.setField(Field.PointOfServiceConditionCode,new POSConditionCode(POSConditionCode.ManualReversal,false).Value());
//
//      BlindCopy(msg,original,Field.Track2Data);//Track-2 Data 35 z..37 LLVAR C Required if present in the
//      BlindCopy(msg,original,Field.Track1Data);//Track-1 Data 45 ans..76 LLVAR C ]
//      BlindCopyMoney(msg,original,Field.CashbackAmount);//Cashback Amount 147 n 12 M
//      BlindCopy(msg,original,Field.EmployeeNumber);//Employee Number 148 an ..16 LLVAR M
//      BlindCopy(msg,original,Field.ControllerType);//Controller Type 149 n 3 M Value is 001.
//      BlindCopy(msg,original,Field.CheckType);//Check Type 150 n 1 M
//      BlindCopy(msg,original,Field.DriversLicense);//Driver's License 157 an ..28 LLVAR C Required if present in the
//      BlindCopy(msg,original,Field.CheckRouting);//Check Routing Number 165 ans ..9 LLVAR C Required if present in the
//      BlindCopy(msg,original,Field.CheckingAccountNumber);//Checking Account Number 166 an ..19 LLVAR C Required if present in the
//      BlindCopy(msg,original,Field.CheckNumber);//Check Number 167 n ..5 LLVAR C Required if present in the
//      BlindCopy(msg,original,Field.SocialSecurityNumber);//Social Security Number 168 n 9 C Required if present in the
//      BlindCopy(msg,original,Field.OtherCardNumber);//Other Card Number 169 an ..28 LLVAR C Required if present in the
//      BlindCopy(msg,original,Field.IdentificationType);//Identification Type 170 an 2 O Same value as in the
//
//      Insert(msg,tid);  //new transaction's ID
//      Insert(msg,term); // !!! must use the original terminal's info!
//    }
    return msg;
  }

  public /*static*/ final Message ForRequest(ActionRequest request, TransactionID tid, TerminalInfo term){
    Message msg=null; //defer creation until we know what type
    if (request.isFinancial()) {
      msg=ForFinancialRequest((FinancialRequest) request);
    } else if (request instanceof LoginRequest) {
      msg=ForLogin((LoginRequest) request);
    }
    //any of the above may leave msg still null:
    if(msg!=null){
      Insert(msg,tid);
      Insert(msg,term);
    }

    return msg; //caller beware of null!!!
  }

}

//$Id: Create.java,v 1.53 2001/11/17 06:16:57 mattm Exp $
