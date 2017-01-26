package net.paymate.ivicm.pinpad;

import net.paymate.serial.DisplayPad;
import net.paymate.util.*;
import net.paymate.lang.ContentType;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/pinpad/EC100DisplayPad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */
import net.paymate.data.*;
public class EC100DisplayPad extends DisplayPad {

  protected boolean doesStringInput(ContentType ct){
    switch(ct.Value()){
      case ContentType.arbitrary :
      case ContentType.purealpha :
      case ContentType.alphanum  :
      case ContentType.password  :
      case ContentType.decimal   :
      case ContentType.hex       :
      case ContentType.money     :
      case ContentType.ledger    :
      case ContentType.cardnumber:
      case ContentType.expirdate :
      case ContentType.micrdata  :
      case ContentType.date      :
      case ContentType.time      :
      case ContentType.zulutime  :
      case ContentType.taggedset :
        return true;
      case ContentType.select    :
      case ContentType.unknown   :
        return false;
    }
    return false;
  }

  /**
   * get input, prompt already sent
   */
//  public void getString(String prompt,String preload){
//    hardware.getString(prompt,preload);
//  }

/**
 * @return translated "Z43" key responses
*/
  protected char cookedKey(int rawkey){//allow extensions to define keymap via code.
    switch (rawkey) {
      case '?':  return IGNORE;     //timeouts shouldn't happen,unless driver needs them to stay-alive
      case '#':  return ENTER;      //"Ok" key
      case '*':  return CLEAR;  //"CLEAR" key
      case 'A':  return ALPHA;      //function key A
      case 'B':  return BACKSPACE;       //function key B
      case 'C':  return FUNCTION;       //function key C
      case 'D':  return CLEAR;      //"CANCEL" key
      default:   return (char )rawkey;//0..9
    }
  }

  public static EC100DisplayPad makePad(encrypt100 hardware) {
    EC100DisplayPad newone=new EC100DisplayPad();
    newone.attachTo(hardware); //hooks up display
    hardware.setKeyListener(newone);//hooks up keystrokes
    return newone;
  }

}
//$Id: EC100DisplayPad.java,v 1.13 2003/07/27 05:35:06 mattm Exp $