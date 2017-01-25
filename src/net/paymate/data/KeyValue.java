/**
* Title:        KeyValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: KeyValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
*/
package net.paymate.data;

public class KeyValue extends Value {
  Character content;

  public int asInt(){
    return Character.digit(content.charValue(),10);
  }

  public ContentType charType(){
    return new ContentType(ContentType.select);
  }

  public String Image(){
    return content.toString();
  }

  public boolean setto(String image){
    content= new Character(image.charAt(0));
    return true; //so long as we are 'arbitrary'
  }

  public void Clear(){
    content=new Character('0');
  }

  public KeyValue(){
    Clear();
  }

}
//$Id: KeyValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
