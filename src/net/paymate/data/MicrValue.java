/**
* Title:        MicrValue
* Description:
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: MicrValue.java,v 1.2 2000/12/02 04:04:34 andyh Exp $
*/
package net.paymate.data;

import  net.paymate.util.Safe;

public class MicrValue extends TextValue {//need to maintain '?' data
  int maxlength;
  public ContentType charType(){
    return new ContentType(ContentType.micrdata);
  }

  public boolean setto(String image){
    StringBuffer cleaner=new StringBuffer(image);

    for(int i=cleaner.length();i-->0;){
      if(!Character.isDigit(cleaner.charAt(i))){
        cleaner.setCharAt(i,'?');
      }
    }

    while(cleaner.length()>maxlength){//do we need to enforce these rules??
      cleaner.deleteCharAt(0); //remove excess leading digits.
    }
/* do NOT enforce fixed length!
    while(cleaner.length()<maxlength){//and if so what order???
     cleaner.insert(0,'?');
    }
*/
    return super.setto(cleaner.toString());
  }

  public void Clear(){
    super.setto("?");
  }

  public MicrValue(int maxlen,String micrin) {
    maxlength=maxlen;
    if(Safe.NonTrivial(micrin)){
      setto(micrin);
    } else {
      //could throw "NumberFormatException"
      Clear();
    }
  }

  public MicrValue(int maxlen) {
    this(maxlen,"????????????????"); //nothing is THAT long...
  }

}
//$Id: MicrValue.java,v 1.2 2000/12/02 04:04:34 andyh Exp $
