/**
* Title:        SaleMoney
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: SaleMoney.java,v 1.1 2003/10/25 20:34:20 mattm Exp $
*/
package net.paymate.data;

import net.paymate.util.*;
import net.paymate.awtx.RealMoney;

public class SaleMoney implements isEasy {
  public RealMoney   amount  = new RealMoney(0);
  public RealMoney   cashback= new RealMoney(0);
  public RealMoney   tendered= new RealMoney(0);

  public final static String amountKey=  "Amount";
  public final static String cashbackKey="cashback";
  public final static String tenderedKey="tendered";

  public boolean isValid(){
    return amount.Value()>0;
  }

  public SaleMoney Clear(){
    amount.setto(0);
    cashback.setto(0);
    tendered.setto(0);
    return this;
  }

  public SaleMoney setAmount(RealMoney anamount){
    amount.setto(anamount);
    tendered.setto(0);
    cashback.setto(0);
    return this;
  }

  public SaleMoney setCashBack(RealMoney anamount){
    cashback.setto(anamount);
    tendered.setto(0);
    return this;
  }

  public SaleMoney setTendered(RealMoney anamount){
    tendered.setto(anamount);
    cashback.setto(tendered.Value()-amount.Value());
    return this;
  }

  public SaleMoney setto(SaleMoney money){
    return setAmount(money.amount).setCashBack(money.cashback).setTendered(money.tendered);
  }

  ////////////////////////
  // isEasy
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

  public String toSpam(){
    return EasyCursor.spamFrom(this);
  }

  public static SaleMoney fromAmount(RealMoney amount){
    return new SaleMoney().setAmount(amount);
  }

  public static SaleMoney fakeOne(){
    return fromAmount(new RealMoney(1500));
  }

}
//$Id: SaleMoney.java,v 1.1 2003/10/25 20:34:20 mattm Exp $
