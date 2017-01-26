package net.paymate.ivicm.ec3K;

import net.paymate.serial.DisplayPad;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/ec3K/EC3KDisplayPad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */
import net.paymate.awtx.*;
import net.paymate.lang.ContentType;


public class EC3KDisplayPad extends DisplayPad {

  //when a check is scanned the question in progress is interrupted.
  /**
   * notification that a check was just scanned and sent to interested party
   */
  /*package*/ void onCheck(){
//    if(beingAsked.guid== net.paymate.terminalClient.ClerkItem.SalePrice){
//      KeyStroked(13);//act like enter pressed
//    }
//    else
    {
      hardware.Display(beingAsked.inandout.Image());
    }
  }

  protected char cookedKey(int rawkey){//allow extensions to define keymap via code.
    switch (rawkey) {             //incoming value as control char:
      case 26:  return ALPHA;     //^Z
      case 13:  return ENTER;     //^M
      case  8:  return BACKSPACE; //^H
      case 24:  return CLEAR;     //^X
      case '*': return IGNORE;
      case '#': return FUNCTION;
      default:  return (char)rawkey; //pass thru rest as ascii.
    }
  }

  public static EC3KDisplayPad makeFrom(EC3K hardware){
    if(hardware!=null){
      EC3KDisplayPad newone= new EC3KDisplayPad();
      newone.attachTo(hardware);//presently super class does Display() calls.
      hardware.Keypad().setReceiver(newone);
      return newone;
    } else {
//      dbg.ERROR("check manager not configured");
      return null;
    }
  }

}
//$Id: EC3KDisplayPad.java,v 1.8 2003/07/27 05:35:04 mattm Exp $