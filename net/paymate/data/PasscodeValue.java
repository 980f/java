/**
* Title:        PasscodeValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: PasscodeValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
*/
package net.paymate.data;

import net.paymate.util.Fstring;

public class PasscodeValue extends Value {
  String content;

  public ContentType charType(){
    return new ContentType(ContentType.alphanum);
  }

  public String Image(){
    return new Fstring(content.length(),'*').toString();

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
//$Id: PasscodeValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
