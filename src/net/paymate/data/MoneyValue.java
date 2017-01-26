/**
* Title:        MoneyValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: MoneyValue.java,v 1.4 2003/07/27 05:34:58 mattm Exp $
*/
package net.paymate.data;

import net.paymate.util.*;
import net.paymate.awtx.RealMoney;
import net.paymate.lang.Value;
import net.paymate.lang.StringX;
import net.paymate.lang.ContentType;

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
    content.setto(StringX.parseLong(image));
    return true; //+++ detect overflow
  }

  public void Clear(){
    content=new RealMoney();
  }

  public String toString(){
    return String.valueOf(content);
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
//$Id: MoneyValue.java,v 1.4 2003/07/27 05:34:58 mattm Exp $
