package net.paymate.hypercom;

import net.paymate.awtx.print.PrinterModel;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IcePrinter.java,v $
 * Description:
 * Copyright:    Copyright (c) 2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 * @todo: add printer attributes packet parsing.
 */
import net.paymate.util.*;
import net.paymate.lang.StringX;

public class IcePrinter extends PrinterModel {
  private boolean weWrap=true;  //coonditional compilation, remove after first field release.
  private IceTerminal sterm; //manages port usage

  private boolean haveCutter; //some terminals do, some don't
//  #define DOUB     '\x0E'         // DOUBLE WIDE
// #define NORM     '\x14'         // NORMAL ( UNDOUBLE )
  private final static char doubleFont=Ascii.SO;
  private final static char normalFont=Ascii.DC4;


  public PrinterModel configure(EasyCursor cfg){
    cfg.push(IceEasyKey.printer);
    try {
      //defaults set for plain ICE5500
      haveCutter=cfg.getBoolean(IceEasyKey.hasCutter,true);
      myTextWidth=cfg.getInt(IceEasyKey.textWidth,40);
    }
    finally {
      cfg.pop();
    }
    return this;
  }

  public boolean formfeed() {
    dbg.VERBOSE("FORM FEED");
    return sterm.sendCommand(IceCommand.Simple(IceCommand.Printer,IceCommand.FormFeed));
  }

  public boolean HasCutter(){//override for printers that cut paper on formfeed.
    return haveCutter;
  }

  public boolean print(byte[] bytes) {//have to trap this for debug of module.
    dbg.ERROR("printing of raw bytes directly invoked");
    dbg.showStack(dbg.ERROR);
    return false;
  }

  /**
   * horrid hack for adding double attribute to some items.
   * should take in a list of names ...
   * this function is hacked for the tastes of MerchantConcepts, hopefully they have good taste
   */
  protected boolean doubleThis(FormattedLineItem fl){
    return fl.canDouble(textWidth()); //expan anything that we can
//    return fl.value.indexOf('$')>=0 || fl.name.indexOf('#')>0; //minimum expandees
  }

  public void print(FormattedLineItem fl) {
    if(fl!=null&& doubleThis(fl)){
      println(fl.doubleWide(textWidth(),doubleFont,normalFont));
    } else {
      super.print(fl);
    }
  }
  /**
 * all text printing eventually goes through here
 * @param stuff text to print, line terminator will be added
 */
  public void println(String stuff) {
    dbg.VERBOSE("println: <"+stuff+">");
    //if we aren't doing linewrap for the remote printer OR
    //   we don't have to wrap then
    //      send it out.
    if( ! weWrap|| ! autoWrap(stuff)){
      IceCommand cmd=IceCommand.Create(2+StringX.lengthOf(stuff));
      cmd.append(IceCommand.Printer);
      cmd.append(IceCommand.PrintLine);
      cmd.frame(stuff);
      sterm.sendCommand(cmd);
    }
  }

  public IcePrinter(IceTerminal sterm) {
    this.sterm=sterm;
    //set pessimistic defaults, works with all ICE models
    haveCutter=false;
    myTextWidth=40;//+_+ until we get feedback from the device itself.
  }

}
//$Id: IcePrinter.java,v 1.10 2003/07/27 05:35:03 mattm Exp $