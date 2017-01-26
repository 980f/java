/**
* Title:        TextValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: TextValue.java,v 1.4 2003/07/27 05:34:58 mattm Exp $
*/
package net.paymate.data;
import net.paymate.util.*;
import net.paymate.lang.Value;
import net.paymate.lang.ContentType;

public class TextValue extends Value {
  protected String content;

  public ContentType charType(){
    return new ContentType(ContentType.alphanum);
  }

  public String Image(){
    return String.valueOf(content);
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
//$Id: TextValue.java,v 1.4 2003/07/27 05:34:58 mattm Exp $
