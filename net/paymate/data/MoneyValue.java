/**
* Title:        MoneyValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: MoneyValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
*/
package net.paymate.data;

import net.paymate.util.Safe;
import net.paymate.awtx.RealMoney;

public class MoneyValue extends Value {
  RealMoney content;

  public ContentType charType(){
    return new ContentType(ContentType.money);
  }

  public String Image(){
    return content.Image();
  }

  public boolean setto(String image){
    //should check for dp and truncate etc. +++
    content.setto(Safe.parseLong(image));
    return true; //+++ detect overflow
  }

  public void Clear(){
    content=new RealMoney();
  }

  public String toString(){
    return content.toString();
  }

  public long asLong(){
    return content.Value();
  }

  public RealMoney Value(){
    return new RealMoney(content);
  }

  public MoneyValue(long cents){
    content=new RealMoney(cents);
  }

  public MoneyValue(){
   this(0);
  }

}
//$Id: MoneyValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
