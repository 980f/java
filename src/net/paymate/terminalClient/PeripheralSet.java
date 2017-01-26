package net.paymate.terminalClient;

/**
 * Title:        $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/terminalClient/PeripheralSet.java,v $
 * Description:
 * Copyright:    Copyright (c) 2002..2003
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import net.paymate.awtx.DisplayInterface;
import net.paymate.awtx.RealMoney;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.data.StoreInfo;
import net.paymate.data.TerminalInfoKey;
import net.paymate.hypercom.IceWrapper;
import net.paymate.jpos.Terminal.IviWrapper;
import net.paymate.jpos.common.JposWrapper;
import net.paymate.jpos.data.CardNumber;
import net.paymate.terminalClient.IviForm.Legend;
import net.paymate.util.EasyCursor;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.QReceiver;

public abstract class PeripheralSet {
  protected static ErrorLogStream sdbg;
  protected ErrorLogStream dbg;
  protected QReceiver posterm;
 /////////////////////////////
 // clerk interface
  public abstract DisplayInterface getClerkPad();
  abstract public boolean gettingSignature();
  abstract public boolean gettingSwipe();


  //////////////////////////
  // printing
  protected PrinterModel printer;
  public PrinterModel getPrinter(){
    return printer!=null?printer: PrinterModel.Null();
  }
  public boolean printerCuts(){
    return printer!=null && printer.HasCutter();
  }

  public abstract void startPinEntry(CardNumber accountNumber, RealMoney amt, boolean isRefund);

  ///////////////////////////
  //signature capture
  public abstract boolean haveSigCap();
  public abstract void endSigcap(boolean andAcquire);

  public abstract void startSwiper();
  public abstract void stopSwiper();

  //////////////////////////
  // forms interface
//  public abstract void startForm(OurForm currentForm);
  public abstract void overlayText(Legend overlay);
  public abstract void startSignature(OurForm currentForm);

  /**
 * done whenever a form definition changes, or program starts
 * no required functionality.
 */
  public void cacheForm(OurForm form){
    //advisory function.
  }

  /**
   * for info that might be worth cacheing on remote terminal
   */
  public void setStoreInfo(StoreInfo si){
    //advisory function.
  }

  /**
   * @return true if customer display takes precedence over clerk display, i.e. of form requires suppression of clerk interface item
   */
  abstract public boolean updateInterfaces(ClerkItem clrkItem,OurForm desiredForm,boolean formIsStale,Uinfo uinfo);

//////////////////////
//system debug
  public abstract void showIdentity(String ME);

/////////////////////////////
// construction
  public abstract void detachAll();//must release hardware resources, should delete internal objects
  protected PeripheralSet(){
  }

  protected PeripheralSet setParent (QReceiver posterm){
    this.posterm=posterm;
    return this;
  }

  protected PeripheralSet (QReceiver posterm){
    this();
    dbg=ErrorLogStream.getForClass(this.getClass());
    setParent(posterm);
  }

  public static PeripheralSet fromDescription(QReceiver jtl,EasyCursor equipmentlist){
    if(sdbg==null) {
      sdbg = ErrorLogStream.getForClass(PeripheralSet.class);
    }
    String classclue=equipmentlist.getString(TerminalInfoKey.WrapperClass);
    //+_+ was saved as a class. should get a class here and invoke the method on it
    //or enumerate allowed classes and save that enumeration.
    //while we have only two instances ...
    sdbg.VERBOSE("WrapperToken "+classclue );
    if(classclue.indexOf("IceWrapper")>=0){
      sdbg.VERBOSE("Making an IceWrapper");
      return IceWrapper.fromDescription(jtl,equipmentlist);
    }
    if(classclue.indexOf("JposWrapper")>=0){
      sdbg.VERBOSE("Making a JposWrapper");
      return JposWrapper.fromDescription(jtl,equipmentlist);
    }
    else {//legacy.
      sdbg.VERBOSE("Making an IviWrapper");
      return IviWrapper.fromDescription(jtl,equipmentlist);
    }
  }

}
//$Id: PeripheralSet.java,v 1.13 2005/02/28 05:01:38 andyh Exp $
