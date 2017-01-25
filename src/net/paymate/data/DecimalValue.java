/**
* Title:        DecimalValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DecimalValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
*/
package net.paymate.data;

import net.paymate.util.Safe;

public class DecimalValue extends Value {
  long content;

  public ContentType charType(){
    return new ContentType(ContentType.decimal);
  }

  public String Image(){
    return Long.toString(content);
  }

  public boolean setto(String image){
    content=Safe.parseLong(image);
    return true; //+++ detect overflow
  }

  public void Clear(){
    content=0;
  }

  public long asLong(){
    return content;
  }

  public DecimalValue(DecimalValue rhs){
    if(rhs!=null){
      content=rhs.content;
    } else {
      Clear();
    }
  }

  public DecimalValue(long init){
    content=init;
  }

  public DecimalValue(){
    Clear();
  }

}
//$Id: DecimalValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
