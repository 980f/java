/**
* Title:        SaleMoney
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: SaleMoney.java,v 1.9 2001/07/06 18:59:01 andyh Exp $
*/
package net.paymate.ISO8583.data;

import net.paymate.data.*;
import net.paymate.util.*;
import net.paymate.awtx.RealMoney;

public class SaleMoney implements isEasy {
  public RealMoney   amount  = new RealMoney(0);
  public RealMoney   cashback= new RealMoney(0);
  public RealMoney   tendered= new RealMoney(0);

  public final static String amountKey="saleAmount";
  public final static String cashbackKey="cashback";
  public final static String tenderedKey="tendered";

  public boolean isValid(){
    return amount.Value()>0;
  }

  public void Clear(){
    amount.setto(0);
    cashback.setto(0);
    tendered.setto(0);
  }

  public void setAmount(RealMoney anamount){
    amount.setto(anamount);
    tendered.setto(0);
    cashback.setto(0);
  }

  public void setCashBack(RealMoney anamount){
    cashback.setto(anamount);
    tendered.setto(0);
  }

  public void setTendered(RealMoney anamount){
    tendered.setto(anamount);
    cashback.setto(tendered.Value()-amount.Value());
  }

  public void save(EasyCursor ezp){
    ezp.setLong (amountKey, amount.Value());
    ezp.setLong (cashbackKey  , cashback.Value());
    ezp.setLong (tenderedKey  , tendered.Value());
  }

  public void load(EasyCursor ezp){
    amount.setto  (ezp.getLong(amountKey));
    cashback.setto(ezp.getLong(cashbackKey));
    tendered.setto(ezp.getLong(tenderedKey));
  }

  public SaleMoney() {
    Clear();
  }
  public SaleMoney(SaleMoney old) {
    amount  = new RealMoney(old.amount);
    cashback= new RealMoney(old.cashback);
    tendered= new RealMoney(old.tendered);
  }

  public String spam(){
    EasyCursor newone=new EasyCursor();
    save(newone);
    return newone.asParagraph();
  }

  public TextList toSpam() {
    TextList tl = new TextList();
    tl.add("amount=" + amount.Value());
//    tl.add("cashback=" + cashback.Value());
//    tl.add("tendered=" + tendered.Value());
    return tl;
  }

}
//$Id: SaleMoney.java,v 1.9 2001/07/06 18:59:01 andyh Exp $
