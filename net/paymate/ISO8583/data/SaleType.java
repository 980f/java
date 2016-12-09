/**
* Title:        SaleType
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: SaleType.java,v 1.12 2001/10/15 22:39:44 andyh Exp $
*/
package net.paymate.ISO8583.data;
import net.paymate.util.*;

public class SaleType implements isEasy {
  public PayType      payby = new PayType      ();
  public TransferType op    = new TransferType ();
  public EntrySource  source= new EntrySource  ();

  public String shortOp() {
    String retval = "?";
    if(op != null) {
      switch(op.Value()) {
        case TransferType.Sale: {
          retval = "S";
        } break;
        case TransferType.Return: {
          retval = "R";
        } break;
        case TransferType.Reversal: {
          retval = "V";
        } break;
      }
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
    ezp.saveEnum("saleOp"    , op);
    ezp.saveEnum("saleSource", source);
  }

  public void load(EasyCursor ezp){
    ezp.loadEnum("payby"     ,payby);
    ezp.loadEnum("saleOp"    ,op);
    ezp.loadEnum("saleSource",source);
  }

  public String spam(){
    EasyCursor newone=new EasyCursor();
    save(newone);
    return newone.asParagraph();
  }

  public String amountHint(){
    switch(op.Value()){
      case TransferType.Return:   return "Refund Amt";
      case TransferType.ReEntry:  return "Final Amt";
      case TransferType.Sale:     return "Sale Amount";
      case TransferType.Reversal: return "Voided Amt";  //probably never used...
    }
    return "NOT APPLICABLE";
  }

  public String noAmountHint(){
    switch(op.Value()){       ////////////1234567890123456789
      case TransferType.Return:   return "Refunding Zero!!!";
      case TransferType.ReEntry:  return "ReEntry is zero!!!";
      case TransferType.Sale:     return "Sign within the Box";
      case TransferType.Reversal: return "Sign within the Box";
    }
    return "NOT APPLICABLE";
  }

}
//$Id: SaleType.java,v 1.12 2001/10/15 22:39:44 andyh Exp $
