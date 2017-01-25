/**
* Title:        ExpirValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ExpirValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
*/
package net.paymate.data;

import net.paymate.ISO8583.data.ExpirationDate;

public class ExpirValue {
  ExpirationDate content;

  public ContentType charType(){
    return new ContentType(ContentType.expirdate);
  }

  public String Image(){
    return content.Image();
  }

  public boolean setto(String image){
    content.parseYYmm(image);
    return true; //+++ detect overflow
  }

  public void Clear(){
    content=new ExpirationDate();
  }

  public String toString(){
    return content.toString();
  }

  public long asLong(){
    return (long)content.Value();
  }

  public ExpirationDate Value(){
    return new ExpirationDate(content);
  }

  public ExpirValue(){

  }

}
//$Id: ExpirValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
