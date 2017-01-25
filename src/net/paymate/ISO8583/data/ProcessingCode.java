package net.paymate.ISO8583.data;
/* $Id: ProcessingCode.java,v 1.18 2001/11/14 01:47:49 andyh Exp $ */

import net.paymate.util.*;

public class ProcessingCode {
  public final static int NA=-1; //not applicable/ invalid value

  public final static String CRRETURN="200030";
  public final static String CRSALE=  "003000";

  public boolean isaReturn(){
    return operation.value ==Operation.Return;
  }

  public boolean isaDebit() {
    return payType.value == PayType2.Debit;
  }

  public boolean isaCredit() {
    return payType.value == PayType2.Credit;
  }

  public String Image(){
    return Fstring.zpdecimal(Value(),6);
  }

  public ProcessingCode parse(String sixchars){
    int value=Safe.parseInt(sixchars);
    //NB: just doing the essential ones right now.
    switch(value){
      case 3000:{
        setto(new TransferType(TransferType.Sale), new PayType(PayType.Credit));
      } break;
      case 200030:{
        setto(new TransferType(TransferType.Return), new PayType(PayType.Credit));
      } break;
      case 40000:{
        setto(new TransferType(TransferType.Sale), new PayType(PayType.Check));
      } break;
    }
    return this;
  }


  public static final ProcessingCode Parse(String sixchars){
    ProcessingCode newone = new ProcessingCode();
    return newone.parse(sixchars);
  }

  //////////////////////////////////////////////////////
  class EBTType {
    public final static int FoodStamp=0;
    public final static int PurchaseSimple=1;
    public final static int PurchaseCashBack=2;
    public final static int Cash=3;

    public int value=NA;

    public int Code(boolean forSale){
      switch(value){
        default: return NA;
        case EBTType.FoodStamp:         return forSale?  9500:209500;
        case EBTType.PurchaseSimple:    return forSale?  2000: NA;
        case EBTType.PurchaseCashBack:  return forSale? 92000: NA;
        case EBTType.Cash:              return forSale? 12000: NA;
      }
    }
  }
  EBTType ebtType=new EBTType();

  class PayType2 {//there are multiple pay/transaction type encodings:(
    public final static int Check=0;
    public final static int ACH=1;
    public final static int Credit=2;
    public final static int Debit=3;
    public final static int EBT=4;

    public int value=NA;

    public void setto(PayType pt){
      switch(pt.Value()){
      default:
      case PayType.Unknown: value=NA; return;
      case PayType.Credit : value=PayType2.Credit;  return;
      case PayType.Check  : value=PayType2.Check;   return;
      case PayType.Debit  : value=PayType2.Debit;   return;
      case PayType.Cash   : value=NA; return;
      }
    }

    public int Code(boolean forSale /* else a return*/){
      switch(value){
        default: return NA;
        case PayType2.Check:   return forSale?40000: NA   ;
        case PayType2.ACH:     return forSale?40090:200090;
        case PayType2.Credit:  return forSale? 3000:200030;
        case PayType2.Debit:   return forSale?    0:200000;
        case PayType2.EBT:     return ebtType.Code(forSale);
      }
    }

  }
  PayType2 payType=new PayType2();

  class GiftOperation {

    public final static int Balance=0;
    public final static int Issuance=1;
    public final static int Redemption=2;
    public final static int Refund=3;
    public final static int Reissue=4;

    public int value=NA;

    public int Code(){
      switch(value){
        default: return NA;
        case GiftOperation.Balance:     return 310098;
        case GiftOperation.Issuance:    return 480098;
        case GiftOperation.Redemption:  return 190098;
        case GiftOperation.Refund:      return 290098;
        case GiftOperation.Reissue:     return 490098;
      }
    }

  }
  GiftOperation gifter;

  class FrequentShopper {
    public final static int Authorization=0;
    public final static int Update=1;

    public int value=NA;

    public int Code(){
      switch(value){
        default: return NA;
        case FrequentShopper.Authorization: return 319999;
        case FrequentShopper.Update:        return 189999;
      }
    }

  }
  FrequentShopper frequer=new FrequentShopper();

  class Operation {
    public final static int Sale=0;
    public final static int Return=1;
    protected final static int GapInEncoding=2;
    public final static int GiftCertificate=3;
    public final static int FrequentShopper=4;

    public int value=NA;

    public int Code(){
      switch(value){
        default: return NA;
        case Operation.Sale:
        case Operation.Return:          return payType.Code(value==Operation.Sale);

        case Operation.GiftCertificate: return gifter.Code();
        case Operation.FrequentShopper: return frequer.Code();
      }
    }

    public void setto(TransferType op){
      switch (op.Value()) {
        default:
        case TransferType.Unknown:  value=NA; return;
        case TransferType.Sale:     value=Operation.Sale; return;
        case TransferType.Return:   value=Operation.Return; return;
        case TransferType.Reversal: value=NA; return; // must pick up from original transOperation.Reversal; return;
      }
    }
  }//end inner class Operation
  public Operation operation=new Operation();

  public int Value(){
    return operation.Code();
  }

  public void setto(TransferType oper,PayType payby){
    operation.setto(oper);
    payType.setto(payby);
    //other features NYI by paymate corp.
  }

  public static final String Reverse(String original){
    String clean= original.trim();
    if(original.equals(ProcessingCode.CRRETURN)){
      return "203000"; //enshrined bug in mainsail
    } else {
      return clean;
    }
  }

  public static final ProcessingCode New(SaleType sale){
    ProcessingCode newone =new ProcessingCode();
    newone.setto(sale.op,sale.payby);
    return newone;
  }



}
//$Id: ProcessingCode.java,v 1.18 2001/11/14 01:47:49 andyh Exp $
