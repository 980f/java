/**
* Title:        TextValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TextValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
*/
package net.paymate.data;

public class TextValue extends Value {
  protected String content;

  public ContentType charType(){
    return new ContentType(ContentType.alphanum);
  }

  public String Image(){
    return content.toString();
  }

  public boolean setto(String image){
    content=new String(image); //+_+
    return true; //this promiscuous type accepts anything superficially textual
  }

  public void Clear(){
    content=new String("");
  }

  public TextValue(String defawlt){
    content=new String(defawlt);
  }

  public TextValue(){
    this("DEFAULT"); //--- just for initial testing
  }

}
//$Id: TextValue.java,v 1.1 2000/11/21 03:03:06 andyh Exp $
