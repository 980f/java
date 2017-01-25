package net.paymate.ivicm.et1K;

import net.paymate.awtx.print.PrinterModel;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author $Author: andyh $
 * @version $Id: EtPrinter.java,v 1.4 2001/06/26 01:35:21 andyh Exp $
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
//$Id: EtPrinter.java,v 1.4 2001/06/26 01:35:21 andyh Exp $
