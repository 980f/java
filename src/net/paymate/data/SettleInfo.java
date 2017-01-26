package net.paymate.data;
/**
 * Title:        $Source: /cvs/src/net/paymate/data/SettleInfo.java,v $
 * Description:  wad of pieces assoicated with a transaction.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */


import net.paymate.lang.StringX;
import net.paymate.util.*;
import net.paymate.awtx.RealMoney;
import net.paymate.lang.Bool;


public class SettleInfo implements isEasy {
  public SettleOp   settleop;
  private RealMoney  settleamount;
  public boolean    settle;

  public SettleInfo setMoney(RealMoney money){
    this.settleamount=money;
    return this;
  }

  public void Clear(){
    settleop.Clear();
    settleamount.setto(0);
    settle=false;
  }

  public RealMoney Amount(){
    return settleamount!=null? settleamount: RealMoney.Zero();
  }

  public LedgerValue netAmount(){
    return LedgerValue.New(Amount().Value(),isReturn());
  }


  public boolean isReturn(){
    return typeIs(SettleOp.Return);
  }

  public boolean isSale(){
    return typeIs(SettleOp.Sale);
  }

  public boolean typeIs(int transfertype){
    return (settleop!=null)? settleop.is(transfertype): (transfertype==SettleOp.Unknown);
  }

//the following can share the same branch, they don't overlap
  private final static String settleOpKey="settleop";
  private final static String settleAmountKey="settleamount";
  private final static String settleKey="settle";

  public void load(EasyCursor ezp){
    ezp.loadEnum(settleOpKey, settleop);
    settleamount=(RealMoney)ezp.getObject(settleAmountKey,RealMoney.class);
    settle=ezp.getBoolean(settleKey);
  }

  public void save(EasyCursor ezp){
    ezp.saveEnum(settleOpKey, settleop);
    ezp.setObject(settleAmountKey,settleamount);
    ezp.setBoolean(settleKey,settle);
  }

  public String spam(){
    return toSpam().asParagraph();
  }

  public TextList toSpam() {
    return EasyCursor.makeFrom(this).toSpam(null);
  }

  public SettleInfo setto(SettleInfo sale) {
    settleop.setto(sale.settleop);
    settleamount.setto(sale.settleamount);
    settle=sale.settle;
    return this;
  }

  public SettleInfo(SettleInfo sale){
    this();
    if(sale!=null){
      setto(sale);
    }
  }

  public SettleInfo(){//#public for EasyCursor getObject.
    settleop = new SettleOp();
    settleamount = new RealMoney();
    settle = false;
  }

  public static final SettleInfo MakeFrom(SaleInfo req){
    SettleInfo si = new SettleInfo();
    si.settleop.setto(SettleOp.Sale);//the only thing we are supposed to standin
    si.settleamount.setto(req.Amount());
    si.settle = true;
    return si;
  }

  public static final SettleInfo New(SettleOp sop, RealMoney samt, boolean settle) {
    SettleInfo si = new SettleInfo();
    si.settleop.setto(sop);
    si.settleamount.setto(samt);
    si.settle = settle;
    return si;
  }

}
//$Id: SettleInfo.java,v 1.7 2003/10/25 20:34:21 mattm Exp $

