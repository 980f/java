package net.paymate.ivicm.et1K;

import net.paymate.awtx.print.PrinterModel;

/**
 * Title:         $Source: /cvs/src/net/paymate/ivicm/et1K/EtPrinter.java,v $
 * Description:  print to enTouch, kinda like we saw at homeDepot
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @version $Id: EtPrinter.java,v 1.6 2003/01/09 00:25:32 andyh Exp $
 * @todo: delete this class
 */

import  net.paymate.jpos.Terminal.LinePrinter;

public class EtPrinter extends PrinterModel {

  void onConstruction(){
    super.myTextWidth=19;
    super.setLineFeed((char)0,0);
  }
/**
 * @deprecated Not completed yet.
 */
  public EtPrinter(LinePrinter lp) {
    super(lp);
  }

  public boolean formfeed() {
    if(lp instanceof ScreenPrinter){
      return ((ScreenPrinter)lp).clearOnNextWrite=true;//+_+ CST (Cheap Syntactic Trick)
    }
    return super.formfeed();
  }


}
//$Id: EtPrinter.java,v 1.6 2003/01/09 00:25:32 andyh Exp $
