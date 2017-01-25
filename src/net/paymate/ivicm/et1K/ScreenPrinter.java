package net.paymate.ivicm.et1K;

import net.paymate.jpos.Terminal.LinePrinter;

/**
* Title:
* Description:  prints double high and double wide to entouch screen
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: andyh $
* @version $Id: ScreenPrinter.java,v 1.5 2001/06/26 01:35:21 andyh Exp $
*/

public class ScreenPrinter extends LinePrinter {
  FormService link;
  boolean clearOnNextWrite=true;
  boolean overFlowed=false;
  int cursorRow;
  int cursorColumn;

  ScreenPrinter(String name,FormService fs){
    super(name);
    link=fs;
  }

  protected synchronized void sendLine(byte [] rawline){
    try { //has embedded jpos crap
      //      dbg.Enter("sendLine(byte[])");
      if(clearOnNextWrite){
        clearOnNextWrite=false;
        overFlowed=false;
        link.clearScreen();
      }
      if(!overFlowed){
        String purified= new String(rawline);
        link.setFont(Codes.CMFONT_16X16,Codes.CMFONT_PLAIN);//+_+ find Jpos Constants for this.
        link.displayTextAt(cursorRow,cursorColumn,purified);
        //the following constants are a quick fix, need to transfer parts of terminalClient.OurForm into this package
        cursorColumn=1;
        cursorRow+=2;
        overFlowed=cursorRow>29;
      }
    } catch(jpos.JposException jape){
      //dbg.Caught(jape);
    } finally {
      //dbg.Exit();
    }
  }

}
//$Id: ScreenPrinter.java,v 1.5 2001/06/26 01:35:21 andyh Exp $
