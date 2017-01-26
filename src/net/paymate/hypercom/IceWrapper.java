package net.paymate.hypercom;

/**
 * Title:        $Source: /cvs/src/net/paymate/hypercom/IceWrapper.java,v $
 * Description:  make multiple peripherals out of a single hypercom 5500
 * Copyright:    Copyright (c) 2002
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import net.paymate.serial.*;
import net.paymate.jpos.data.*;
import net.paymate.awtx.*;
import net.paymate.awtx.print.*;

import net.paymate.util.*;
import net.paymate.data.*;
import net.paymate.peripheral.*;

import net.paymate.terminalClient.IviForm.*;
import net.paymate.terminalClient.*;

import net.paymate.hypercom.IceTerminal; //first instance of what will be a base class or interface

public class IceWrapper extends PeripheralSet {
  private ErrorLogStream dbg;

  IceTerminal sterm; //+_+ not basic
  /*package*/ StoreInfo si;

  private IceWrapper(Port port) {
    sterm = new IceTerminal(port);
    dbg = ErrorLogStream.getExtension(IceWrapper.class, port.nickName());
  }

  ////////////////////////
  // user interfaces
  public boolean gettingSignature() {
    return sterm != null && sterm.sigCapturing();
  }

  public boolean gettingSwipe() {
    return sterm != null && sterm.swipeCapturing();
  }

  public boolean gettingPin() {
    return sterm != null && sterm.pinCapturing();
  }
  /**
   * only some forms appear on hypercom. rest are overridden by clerk interface items
   */
  private boolean doForm(OurForm form, Uinfo dynamic) {
    switch (form.Id().Value()) {
      case POSForm.WaitPatronCredit://ask credit/debit/ok all at once
        IceCommand formless=IceCommand.Create(100);
        formless.append(IceCommand.KeyInput); formless.append(Ascii.I);
//  template for positioning text   "  LEFT  Middle  Right "; //font on device is proportional making this a rough guide
        String keys;
        if(dynamic.debitAllowedHack){
          formless.appendFrame(" CREDIT   or    DEBIT ");
          keys=IceTerminal.formKeys;
          formless.appendNumericFrame(IceTerminal.formKeys.charAt(0),1);
          formless.appendNumericFrame(IceTerminal.formKeys.charAt(1),1);
          formless.appendNumericFrame(IceTerminal.formKeys.charAt(2),1);

        } else {
          formless.appendFrame("  OK           CANCEL ");
          formless.appendNumericFrame(IceTerminal.formKeys.charAt(0),1);
          formless.appendNumericFrame(IceTerminal.formKeys.charAt(1),1);
          formless.appendNumericFrame(IceTerminal.formKeys.charAt(3),1);
        }
        sterm.sendCommand(formless);
        sterm.display.Display(dynamic.sale.youPay());
        return true;
    }
    return false;//unless intercepted let cashier interface rule the day, ummm, the device.
  }

  public boolean updateInterfaces(ClerkItem clrkItem, OurForm desiredForm, boolean formIsStale, Uinfo uinfo) {
    boolean change = formIsStale;

    if (desiredForm.isPinPad) {
      if (change || ! gettingPin()) {
        dbg.WARNING("presenting pin pad:" + desiredForm.myName);
        stopSwiper(); //disble swiping during pin acquisition to avert operator errors
        startPinEntry(uinfo.card.accountNumber, uinfo.sale.Amount(), uinfo.sale.typeIs(TransferType.Return));
      }
      return true; //disable clerkpad functions
    } else {//not wanting a pin and pinpad is active
      if(gettingPin()){
        stopPinEntry();
      }
    }

    if (desiredForm.hasSignature()) {
      if (!gettingSignature()) {
        stopSwiper();
        startSignature(desiredForm);
      }
      return true; //can't do clerk interface stuff
    } else {
      if (gettingSignature()) {
        endSigcap(false);
      }
    }

    if (desiredForm.isSwiper) {
      dbg.VERBOSE("Form accepts swipes");
      startSwiper();
      //pick swipe text variants here
    }
    else {
      stopSwiper(); //which has side effect of disabling as well as discarding input
    }
    //swiping is independent of other customer input, sigcap and pinpad are not!
    //i.e. during pin acquisition we disable all other forms of input to ensure there
    // is no temporal ambiguity as to which card a pin is being entered for.
    switch(clrkItem.Value()){//pick out ones which supercede forms
      case ClerkItem.SaleType:
      case ClerkItem.SalePrice:
      case ClerkItem.MerchRef:
      case ClerkItem.AVSstreet:
      case ClerkItem.AVSzip:
      case ClerkItem.SecondCopy:
      case ClerkItem.WaitApproval:
        return false;
    default:
      return doForm(desiredForm,uinfo);
    }
  }

  Tablet sigcapdevice;
  public boolean haveSigCap() {
    if (sigcapdevice == null) {
      sigcapdevice = sterm.getTablet();
    }
    return sigcapdevice != null;
  }

  public void startSignature(OurForm currentForm) {
    if (haveSigCap()) {
      sterm.getTablet().setEnable(true);
    }
  }

  public void endSigcap(boolean andAcquire) {
    if(andAcquire){
      sterm.getTablet().getSignature();
    } else {
      sterm.getTablet().setEnable(false);
    }
  }

  ////////////////////////////
  // DisplayInterface
  /**
   * at this point the architecture is really twisted. It presumes that the clerkpad is a free standing device,
   * it makes it hard to have one class also serve other logical components.
   * we must extract a higher level interface ...
   */
  public DisplayInterface getClerkPad() {
    return sterm.DisplayPad();
  }

  public PrinterModel getPrinter() {
    if (printer == null) {
      printer = sterm.LinePrinter();
    }
    return super.getPrinter(); //which has backup for failure of sterm.
  }

  ///////////////////////////////////////
  public void overlayText(Legend overlay) { //forms interface
    /**@todo: implement this net.paymate.terminalClient.PeripheralSet abstract method*/
  }

  public void startSwiper() { //swipe interface
    sterm.CardSwipe().setEnable(true);
  }

  public void startPinEntry(CardNumber accountNumber, RealMoney amt, boolean isRefund) {
    if(sterm!=null){
      sterm.PinPad().Acquire(PinRequest.From(accountNumber,amt,isRefund));
    }
  }

  public void stopPinEntry(){
    if(sterm!=null){
      sterm.PinPad().setEnable(false);
    }
  }

  public void stopSwiper() { //swipe interface
    sterm.CardSwipe().setEnable(false);
  }

  public void showIdentity(String ME) {
    sterm.DisplayPad().flash(ME);
    sterm.LinePrinter().println(ME);
  }

  public void detachAll() {
    /**@todo: implement this net.paymate.terminalClient.PeripheralSet abstract method*/
  }


  public void setStoreInfo(StoreInfo si){
    this.si=si;
  }

  final static int dukform=0;
  private IceCommand configPinpad(){
    IceCommand newone=IceCommand.Create();
    newone.append(IceCommand.Pinpad);
    newone.append('C');
    newone.appendNumericFrame(dukform,1);
    return newone;
  }

  ///////////////////////////

  public static PeripheralSet fromDescription(QReceiver jtl,
                                              EasyCursor equipmentlist) {
    //get port from eqlist, expects cursor to have a "port" section.
    Port port = PortProvider.openPort("IceWrapper", equipmentlist);
    //@todo: i fport is defective DO something tangible about it!
    IceWrapper newone = new IceWrapper(port);
    newone.sterm.config(equipmentlist).attachTo(jtl);
    newone.setParent(jtl);
    return newone;
  }

}
//$Id: IceWrapper.java,v 1.10 2004/03/08 17:19:10 andyh Exp $
