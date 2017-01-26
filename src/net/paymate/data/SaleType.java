/**
* Title:        SaleType
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: SaleType.java,v 1.1 2003/10/25 20:34:21 mattm Exp $
*/
package net.paymate.data;
import net.paymate.util.*;

public class SaleType implements isEasy {
  public PayType      payby = new PayType      ();
  public TransferType op    = new TransferType ();
  public EntrySource  source= new EntrySource  ();

  public boolean amountReqd(){
    if(op!=null){
      switch(op.Value()){
        default:
          return true;//harshest
        case TransferType.Query:
        case TransferType.Reversal:
          return false;
      }
    }
    return false;
  }

  public boolean cardReqd(){
    if(op!=null){
      switch(op.Value()){
        default:
          return true;
        case TransferType.Reversal:
        case TransferType.Modify:
          return false;
      }
    }
    return false;
  }

  public boolean cardholderReqd(){
    if(op!=null){
      switch(op.Value()){
        default:
          return true;
        case TransferType.Force:
        case TransferType.Modify:
          return false;
      }
    }
    return false;
  }

  public boolean isKnown(){
    return TransferType.IsLegal(op) && ! op.is(TransferType.Unknown);
  }

  public boolean canBeDebit(){
    if(op!=null){
     switch(op.Value()){
       default:
       case TransferType.Unknown : return false;
       case TransferType.Sale    : return true;
       case TransferType.Return  : return true;
       case TransferType.Reversal: return false;//moot
       case TransferType.Query   : return true; //balance inquiry
       case TransferType.Authonly: return false;
       case TransferType.Modify  : return false;//moot
       case TransferType.Force   : return false;
     }
    }
    return false;
  }

  public String shortOp() {
    String retval = "?";
    if(op != null) {
//      switch(op.Value()) {
//        case TransferType.Sale: {
//          retval = "S";
//        } break;
//        case TransferType.Return: {
//          retval = "R";
//        } break;
//        case TransferType.Reversal: {
//          retval = "V";
//        } break;
//      }
      retval = String.valueOf(op.Char());
    }
    return retval;
  }

  public boolean isComplete(){//+_+ pick out illegal combos
    return !payby.is(PayType.Unknown) && !op.is(TransferType.Unknown) && !source.is(EntrySource.Unknown);
  }

  public SaleType setto(PayType payby, TransferType op, EntrySource source){
    this.payby =  payby ;
    this.op    =  op    ;
    this.source=  source;
    return this;
  }

  public SaleType setto(EntrySource source){
    this.source=  source;
    return this;
  }

  public SaleType setto(TransferType op){
    this.op    =  op    ;
    return this;
  }

  public SaleType setto(PayType payby){
    this.payby =  payby ;
    return this;
  }

  public void Clear(){
    payby.setto(PayType.Unknown);
    op.setto(TransferType.Unknown);
    source.setto(EntrySource.Unknown);
  }

  public SaleType(){
    Clear();
  }

  public SaleType(SaleType old){
    payby = new PayType      (old.payby);
    op    = new TransferType (old.op);
    source= new EntrySource  (old.source);
  }

  public void save(EasyCursor ezp){
    ezp.saveEnum("payby"     , payby);
    ezp.saveEnum("type"    , op);
    ezp.saveEnum("Source", source);
  }

  public void load(EasyCursor ezp){
    ezp.loadEnum("payby"     ,payby);
    ezp.loadEnum("type"    ,op);
    ezp.loadEnum("Source",source);
  }

  public String toSpam(){
    return EasyCursor.spamFrom(this);
  }

  /**
   * @note: 12 characters here limits sale amount to 999.99 on entouch
   */
  public String amountHint(){
    switch(op.Value()){
      case TransferType.Return:
        switch(payby.Value()){
          case PayType.GiftCard: return "Add/Refund";
          default: return "Refund Amt";
        }
      case TransferType.Modify:   return "Final Amt";
      case TransferType.Force:    return "Force Amt";
      case TransferType.Sale:     return "Sale Amount";
      case TransferType.Reversal: return "Voided Amt";  //probably never used...
      case TransferType.Query:    return "For Query";  //probably never used...
      case TransferType.Authonly: return "Auth Amount"; // if this is set to settle, then it is a SALE, and not just an AUTH
    }
    return "NOT APPLICABLE";
  }

  public String noAmountHint(){
    switch(op.Value()){       ////////////1234567890123456789
      case TransferType.Return:
      switch(payby.Value()){
        case PayType.GiftCard: return "Add/Refund Zero?";
        default: return "Refunding Zero!!!";
      }
      case TransferType.Force:    return "Force Zero?";
      case TransferType.Authonly: return "Auth Zero?";
      case TransferType.Modify:   return "Final amt is zero?";//usually no signature
      case TransferType.Sale:     return "Sign within the Box";
      case TransferType.Reversal: return "Sign within the Box";
      case TransferType.Query:    return "For Balance info";  //waiting on swipe
    }
    return "NOT APPLICABLE";
  }

  public static SaleType New(int pt,int tt,boolean manual){
    SaleType newone=new SaleType();
    newone.payby.setto(pt);
    newone.op.setto(tt);
    newone.source.setto(manual?EntrySource.Manual:EntrySource.Machine);
    return newone;
  }

}
//$Id: SaleType.java,v 1.1 2003/10/25 20:34:21 mattm Exp $
