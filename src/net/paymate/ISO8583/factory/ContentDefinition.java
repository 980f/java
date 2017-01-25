package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/ContentDefinition.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.*;
import net.paymate.util.*;

public class ContentDefinition {
  public ContentType ctype;
  public int variableLength; //0 for fixed, else number of digits for length field
  public int length;     //either the exact or the limit

  public boolean isValid(){
    return false;
  }

  public boolean isFixed(){
    return variableLength==0;
  }

  public boolean legalLength (String proposed){
    if( proposed!=null && isFixed() ? (proposed.length() > length) : Integer.toString(proposed.length()).length() > variableLength){
      ErrorLogStream.Debug.ERROR("String is too long ["+variableLength+","+length+"]"+proposed);
      return false;
    }
    return true;
  }

  public int lengthFor(String content){
    if(isValid()){
      if(isFixed()){ //pad the content
        return length;
      } else {
        return content.length()+variableLength;
      }
    } else {
      return Safe.INVALIDINTEGER;
    }
  }

  public ContentDefinition(ContentType ctype, int variableLength, int length){
    this.ctype=          ctype;
    this.variableLength= variableLength;
    this.length=         length;
  }

  ////////////////////////
  public static final String Padded(String value,int numDigits, boolean preNumeric){
    //no logarithms or powers...
    StringBuffer padded= new StringBuffer(numDigits);
    int padding=numDigits;
    if(value!=null){
      padding-=value.length();
    }
    if(padding<0){
      //throw error! +_+
      return padded.toString(); //empty string?
    }
    if(preNumeric){
      while(padding-->0){
        padded.append('0');//+_+ out to be a strFill function somewhere already.
      }
    }
    if (value!=null) {
      padded.append(value);
    }
    if(!preNumeric){
      while(padding-->0){
        padded.append(' ');
      }
    }
    return padded.toString();
  }

  public static final String Padded(String value,int numDigits,ContentType ct){
    return Padded(value,numDigits,ct.is(ContentType.decimal));
  }
  public static final String Padded(int value,int numDigits){
    return Padded(Integer.toString(value),numDigits,true);
  }

  public final boolean formatInto(StringBuffer pack,String content){
    if(isValid()){
      if(isFixed()){ //pad the content
        pack.append(ContentDefinition.Padded(content,length,ctype));
      } else {
        //pack padded length
        pack.append(ContentDefinition.Padded(content.length(),variableLength));
        //pack data
        pack.append(content);
      }
      return true;
    } else {
      return false;
    }
  }

}
//$Id: ContentDefinition.java,v 1.1 2001/11/14 13:53:45 andyh Exp $