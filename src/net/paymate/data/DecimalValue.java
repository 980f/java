/**
* Title:        DecimalValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: DecimalValue.java,v 1.4 2003/07/27 05:34:57 mattm Exp $
*/
package net.paymate.data;

import net.paymate.util.*;
import net.paymate.lang.Value;
import net.paymate.lang.StringX;
import net.paymate.lang.ContentType;

public class DecimalValue extends Value {
  long content;

  public ContentType charType(){
    return new ContentType(ContentType.decimal);
  }

  public String Image(){
    return Long.toString(content);
  }

  public boolean setto(String image){
    content=StringX.parseLong(image);
    return true; //+++ detect overflow
  }

  public void Clear(){
    content=0;
  }

  public int asInt(){
    return (int) content;
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
//$Id: DecimalValue.java,v 1.4 2003/07/27 05:34:57 mattm Exp $
