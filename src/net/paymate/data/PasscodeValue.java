/**
* Title:        PasscodeValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PasscodeValue.java,v 1.6 2003/07/27 05:34:58 mattm Exp $
*/
package net.paymate.data;

import net.paymate.util.*;
import net.paymate.lang.Value;
import net.paymate.lang.StringX;
import net.paymate.lang.ContentType;

public class PasscodeValue extends Value {
  String content;

  public ContentType charType(){
    return new ContentType(ContentType.password);
  }

  public String Image(){
    String image=StringX.fill("",'*',content.length(),true);//String.valueOf(new Fstring(content.length(),'*'));
    return image;
  }

  public boolean setto(String image){
    content=new String(image); //+_+
    return true; //promiscuous type accepts anything superficially textual
  }

  public void Clear(){
    content=new String();
  }

  public String Value(){
    return content;
  }

  public PasscodeValue(){
    Clear();
  }

}
//$Id: PasscodeValue.java,v 1.6 2003/07/27 05:34:58 mattm Exp $
