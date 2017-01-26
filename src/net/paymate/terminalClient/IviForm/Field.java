/**
* Title:        Field
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: Field.java,v 1.5 2003/07/27 05:35:17 mattm Exp $
*/
package net.paymate.terminalClient.IviForm;
import  net.paymate.lang.Fstring;
import  net.paymate.util.Xml;

public class Field extends Legend {//a place for displayAt text
  Fstring slot;
  final static String xmlType="FIELD";
  public Legend display(String newvalue){
    slot.setto(newvalue); //clips and extends as needed
    super.legend=String.valueOf(slot);
    return this;
  }

  public Field(Legend prompt) {
    super(prompt);
    slot=new Fstring(prompt.legend);
  }


  public String xml(){
    return Xml.wrap(xmlType,super.xml()) ;
  }

}
//$Id: Field.java,v 1.5 2003/07/27 05:35:17 mattm Exp $
