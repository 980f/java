package net.paymate.terminalClient.PosSocket.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/terminalClient/PosSocket/paymentech/PTTransactionCodes.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.lang.StringX;

public class PTTransactionCodes {
  int code=0;
  public int code(){
    return code;
  }

  /**
   * @return whether this code has something to do with batches
   */
  public boolean isBatchy(){
    switch (code) {
      case BatchRelease:
      case QueryBatchUpload:
      case BatchUploadHeader:
      case BatchUploadTrailer:
        return true;
      default:
        return false;
    }
  }

  public static PTTransactionCodes authFrom(PayType pt,TransferType tt) {
    PTTransactionCodes newone=new PTTransactionCodes();
    newone.code=authtrancode(pt,tt);
    return newone;
  }

  public static PTTransactionCodes settleFrom(PayType pt,TransferType tt) {
    PTTransactionCodes newone=new PTTransactionCodes();
    newone.code=settletrancode(pt,tt);
    return newone;
  }

  public static PTTransactionCodes authFrom(SaleType st) {
    return authFrom(st.payby,st.op);
  }
  public static PTTransactionCodes settleFrom(SaleType st) {
    return settleFrom(st.payby,st.op);
  }

  public static PTTransactionCodes From(String twochar) {
    PTTransactionCodes newone=new PTTransactionCodes();
    newone.code=StringX.parseInt(twochar);
    return newone;
  }

  public String toString(){
    return String.valueOf(code);
  }

  public static String edsdecoder(int eye){
    switch(eye){
    default: return "invalid:"+eye;
    case 1: return "unknown source";
    case 2: return "manually entered";
    case 3: return "track 2";
    case 4: return "track 1";
    }
  }

  public static int /*entrysource*/ edsSource(int eye){
    switch(eye){
      default: return EntrySource.Unknown;
      case 2: return EntrySource.Manual;
      case 3: return EntrySource.Machine;
      case 4: return EntrySource.Machine;
    }
  }


  SaleType stype=null;
  public SaleType SaleType(int edscode){
    if(stype==null){
      stype=new SaleType();
      int payby=PayType.Unknown;
      int ttype=TransferType.Unknown;
      int es=edsSource(edscode);

      switch (code) {
        case CreditSale: {
          payby=PayType.Credit;
          ttype=TransferType.Sale;
        } break;// ",";
        case CreditReturn:   {
          payby=PayType.Credit;
          ttype=TransferType.Return;
        } break;//  ",";
        case CreditVoid: {
          payby=PayType.Credit;
          ttype=TransferType.Reversal;
        } break;
        case DebitSale:  {
          payby=PayType.Debit;
          ttype=TransferType.Sale;
        } break;//  ",";
        case DebitReturn:  {
          payby=PayType.Debit;
          ttype=TransferType.Return;
        } break;//  ",";
        case BalanceInquiry:  {
          payby=PayType.GiftCard;
          ttype=TransferType.Query;
        } break;//  ",";
        case GiftcardVoid:  { //only giftcards get actively voided. other t-types get either a refund or voided via not getting settled.
          payby=PayType.GiftCard;
          ttype=TransferType.Reversal;
        } break;//  ",";
        case Redemption:  {
          payby=PayType.GiftCard;
          ttype=TransferType.Sale;
        } break;//  ",";
        case IssuanceAddValue:  {
          payby=PayType.GiftCard;
          ttype=TransferType.Return;
        } break;//  ",";
        case AuthorizationOnly: {
          payby=PayType.Credit;
          ttype=TransferType.Authonly;
        } break;
        case PriorAuthorizationSale: {
          payby=PayType.Credit;
          ttype=TransferType.Force;
        } break;
      }
      stype.setto(new PayType (payby),new TransferType(ttype),new EntrySource(es));
    }
    return stype;
  }

  private static int authtrancode(PayType pt,TransferType tt) {
    switch (pt.Value()) {
      case PayType.Credit:{
        switch(tt.Value()){
          case TransferType.Sale:   return CreditSale;//1 = Sale, 22 = Sale with cash back
          case TransferType.Authonly: return AuthorizationOnly;//2 = Auth no settle.
          case TransferType.Force: return AuthorizationOnly;
          case TransferType.Return: return CreditReturn;//6 = Return
        }
      }
      case PayType.Debit:{
        switch(tt.Value()){
          case TransferType.Sale:   return DebitSale;//21 = Sale, 22 = Sale with cash back
          case TransferType.Return: return DebitReturn;//24 = Return
        }
      }
      case PayType.GiftCard:{
        switch(tt.Value()){
          case TransferType.Query:  return BalanceInquiry;//79 =balance inquiry, sale amount=0.00.
          case TransferType.Reversal: return GiftcardVoid;//78 = voids are allowed!
          case TransferType.Sale:   return Redemption; //73=redemption
          case TransferType.Return: return IssuanceAddValue;  //70=issue / add value
        }
      }
    }
    return -1;
  }

  private static int settletrancode(PayType pt,TransferType tt) {
    switch (pt.Value()) {
      case PayType.Credit:{
        switch(tt.Value()){
          case TransferType.Sale:     return CreditSale;//1 = Sale, 22 = Sale with cash back
          case TransferType.Force:    return PriorAuthorizationSale;//3
          case TransferType.Authonly: return PriorAuthorizationSale;//3
          case TransferType.Return:   return CreditReturn;//6 = Return
        }
      }
      case PayType.Debit:{
        switch(tt.Value()){
          case TransferType.Sale:   return DebitSale;//21 = Sale, 22 = Sale with cash back
          case TransferType.Return: return DebitReturn;//24 = Return
        }
      }
      case PayType.GiftCard:{
        switch(tt.Value()){
          case TransferType.Query:  return BalanceInquiry;//79 =balance inquiry, sale amount=0.00.
          case TransferType.Reversal: return GiftcardVoid;//78 = voids are allowed!
          case TransferType.Sale:   return Redemption; //73=redemption
          case TransferType.Return: return IssuanceAddValue;  //70=issue / add value
        }
      }
    }
    return -1;
  }

  /* package */
  public static final int CreditSale               = 1;
  public static final int AuthorizationOnly        = 2;
  public static final int PriorAuthorizationSale   = 3;
  public static final int NoShow                   = 4; // [VI, MC, AX Only]
  public static final int CreditReturn             = 6;
  public static final int IncrementalAuthOnly      = 8;
  public static final int AuthPartialReversal      = 9;
  public static final int DiscountCardSale         = 13; // or Sale w/coupon
  public static final int DiscountCardReturn       = 14; // or Return w/coupon
  public static final int DiscountCardPriorSale    = 15; // or Prior Sale w/coupon
  public static final int DebitSale                = 21;
  public static final int DebitSaleCashBack        = 22; // with cash back
  public static final int DebitReturn              = 24;
  public static final int CheckAuthRequest         = 35;
  public static final int CreditVoid               = 41;
  public static final int BatchRelease             = 51;
  public static final int QueryBatchUpload         = 53;
  public static final int BatchUploadHeader        = 54;
  public static final int BatchUploadTrailer       = 55;
  // here down, GiftCard
  public static final int IssuanceAddValue         = 70;
  public static final int Activation               = 71; // (Contact PNS)
  public static final int Redemption               = 73;
  public static final int PriorIssuanceAddValue    = 74;
  public static final int PriorActivation          = 75; // (Contact PNS)
  public static final int PriorRedemption          = 77; // (Force)
  public static final int GiftcardVoid             = 78;
  public static final int BalanceInquiry           = 79;
  public static final int RedemptionAuthOnly       = 80; // (future use only)
  public static final int RedemptionAuthCompletion = 81; // (future use only)


  public String toSpam(){
    switch (code) {
      case CreditSale:        return "Credit,Sale";
      case AuthorizationOnly: return "Credit,Authonly";
      case CreditReturn:      return "Credit,Return";
      case CreditVoid:        return "Credit,Reversal";

      case DebitSale:         return "Debit,Sale";
      case DebitReturn:       return "Debit,Return";
      case BalanceInquiry:    return "GiftCard,Query";
      case GiftcardVoid:      return "GiftCard,Reversal";
      case Redemption:        return "GiftCard,Sale";
      case IssuanceAddValue:  return "GiftCard,Return";
      default:                return "invalid:"+code;
      // +++ add more from above ???
    }
  }

}
//$Id: PTTransactionCodes.java,v 1.1 2003/12/10 02:16:54 mattm Exp $