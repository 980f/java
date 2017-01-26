package net.paymate.ivicm.nc50;

import net.paymate.serial.DisplayPad;
import net.paymate.lang.ContentType;

/**
 * Title:        $Source: /cvs/src/net/paymate/ivicm/nc50/nc50DisplayPad.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */
import net.paymate.data.*;
import net.paymate.util.*;
public class nc50DisplayPad extends DisplayPad  {

  protected boolean doesStringInput(ContentType ct){
    return ! (ct.is(ContentType.select) || ct.is(ContentType.unknown));
  }

  protected boolean refresh(){
    if(! super.refresh()){
      if(beingAsked.isMenu()){
        StartQuestion();//begin again, needed when getSIgnature loops back to get one again.
      }
    }
    return true;//been taken care of even if we don't tweak the device
  }

 /**
 * @return translated key responses
*/
  protected char cookedKey(int rawkey){//allow extensions to define keymap via code.
    switch (rawkey) {
      case 'A':  return CLEAR;  //reset key
      case 'B':  return FUNCTION;   //func key
      case 'C':  return ALPHA;  //doesn't get to us unless in single key mode
      case 'D':  return BACKSPACE;//doesn't get to us unless in single key mode
      case 'E':  return CLEAR;  //doesn't get to us unless in single key mode
      case 'F':  return ENTER;  //doesn't get to us unless in single key mode
      default:   return (char )rawkey;//0..9
    }
  }

  public static nc50DisplayPad makePad(NC50 hardware) {
    nc50DisplayPad newone=new nc50DisplayPad();
    newone.attachTo(hardware); //hooks up display
    hardware.setKeyListener(newone);//hooks up keystrokes
    return newone;
  }

  /**
   *
   */
  static public void main(String[] args) {

  }

}
//$Id: nc50DisplayPad.java,v 1.9 2004/02/26 18:40:50 andyh Exp $