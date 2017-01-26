package net.paymate.jpos.Terminal;
/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/IviWrapper.java,v $
 * Description:  legacy wrapper for jpos devices
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 * @todo implement or purge detachAll()
 */

//until we have a terminalGenerator registry:
import net.paymate.ivicm.*;
import net.paymate.ivicm.ec3K.*;

import net.paymate.serial.*;

import net.paymate.util.*;

import net.paymate.jpos.data.*;
import net.paymate.data.TransferType;
import net.paymate.awtx.*;
import net.paymate.awtx.print.*;
import net.paymate.terminalClient.*;
import net.paymate.terminalClient.IviForm.*;

public class IviWrapper extends PeripheralSet {
  private IviTrio   hardware;

  //things that can spontaneoulsy throw JposExceptions that we wish to convert
  //into events.
  private CheckReader    checkReader;
  private CardReader     cardReader ;
  private PinEntry       pinEntry   ;
  private FormEntry      former     ;
  private LinePrinter    prn        ;
  private DisplayPad     clerkface  ;

  public DisplayInterface getClerkPad(){
    return clerkface!=null ? clerkface : DisplayPad.Null();
  }

  public void startPinEntry(CardNumber accountNumber, RealMoney amt, boolean isRefund) { //
    dbg.ERROR("in start pin pad");
    cardReader.Flush();//et1k returns EC error code if swiping is enabled
    //+_+ ignoring jape return.

    if(!pinEntry.Acquire(accountNumber,amt,isRefund)){
      //pinform probably not showing!  show "alt text" for it:
      former.Acquire(OurForms.WaitPatronDebit(),false);
    }
  }

  public boolean haveSigCap(){
    return hardware.haveSigCap();
  }

  ////////////////////////
  // card reader
  public void startSwiper(){
    dbg.VERBOSE("startSwiper");
    cardReader.Acquire();
  }
  public void stopSwiper(){
    dbg.VERBOSE("stopSwiper");
    cardReader.Flush();
  }

  public void detachAll() {//prepare to restart
  //unlink components
  }

  public void endSigcap(boolean andAcquire){
    former.EndForm(); //and hope that creates events to move us along...gotta finish purging jpos indirect stuff
    //+_+ set flag to ignore any incoming signature data.
  }

  private OurForm currentForm;
  public boolean gettingSignature(){
    return currentForm!=null&& currentForm.hasSignature();
  }

  public boolean gettingSwipe(){
    return currentForm!=null && currentForm.isSwiper;
  }

  private void startForm(OurForm form){
    former.Acquire(currentForm=form,currentForm.isStored);
  }

  public void startSignature(OurForm form){
    former.Acquire(form,form.isStored);//same as startForm...
  }

  public void overlayText(Legend overlay){
    former.displayLegend(overlay);
  }

  public void cacheForm(OurForm form){
    former.StoreForm(form);
    form.isStored=true; //can't move this action into class.OurForm without making that class know about hardware...
  }

  /**
   * add dynamic content.
   */
  private void refreshForm(OurForm form, Uinfo uinfo) { //@ivitrio@
    dbg.Enter("refreshForm");
    try {
      if (form.showsAmount) { //must follow forms.Acquire!
        String saleText;
        if (uinfo.sale.Amount().Value() > 0) {
          saleText = form.ValuePair(uinfo.sale.amountHint(),
                                       uinfo.sale.Amount().Image());
        }
        else {
          saleText = form.Bannerize(uinfo.sale.noAmountHint());
        }
        dbg.VERBOSE("Form shows txn amount:" + saleText);
        overlayText(form.AmountLegend(saleText));
      }
    }
    finally {
      dbg.Exit();
    }
  }

  /**
   * @return whether form overrides clerk interface
   */
  public boolean updateInterfaces(ClerkItem clrkItem,OurForm desiredForm,boolean formIsStale,Uinfo uinfo){
    boolean change=currentForm==null || formIsStale|| desiredForm!=currentForm;//# yes, compare objects not content
    if(desiredForm.isPinPad){
      if(change){
        dbg.WARNING("presenting pin pad:"+desiredForm.myName);
        stopSwiper();
        startPinEntry(uinfo.card.accountNumber,uinfo.sale.Amount(),uinfo.sale.typeIs(TransferType.Return));
      }
    } else if(desiredForm.hasSignature()){
      if(change || !gettingSignature()){
        stopSwiper();
        startSignature(desiredForm);
        //do we need this? refreshForm(desiredForm, uinfo);
      }
    } else {
      if (change) {
        startForm(desiredForm);
      }
      refreshForm(desiredForm, uinfo);
      if(desiredForm.isSwiper){//startForm kills the card swipe, ingenico/NCR have brilliant engineers.
        dbg.VERBOSE("Form accepts swipes");
        startSwiper();//restart,in case we are here due to a device fuggup
        //pick swipe text variants here
      } else {
        stopSwiper(); //which has side effect of disabling as well as discarding input
      }
    }
    currentForm=desiredForm;
    return false;//never a conflict between cashier and customer interfaces
  }


  public void showIdentity(String ME){
//    clerkui.flash(ME);
//+_+    selectForm(POSForm.ClerkLogin);
    net.paymate.terminalClient.IviForm.Legend identifier=
    new net.paymate.terminalClient.IviForm.Legend(1,1,ME,"1");
    former.displayLegend(identifier);
    printer.println("This is terminal:"+ME);
    printer.formfeed();
}


  private static IviTrio ivilegacy(String trackingname,EasyCursor equipmentlist){
    return IviTrio.New(trackingname,equipmentlist);
  }

  private IviWrapper(QReceiver jtl){
    super(jtl);
  }

  public static PeripheralSet fromDescription(QReceiver jtl,EasyCursor equipmentlist){
    IviWrapper newone=new IviWrapper(jtl);
    newone.hardware= ivilegacy(String.valueOf(jtl),equipmentlist);
    newone.checkReader= new CheckReader (newone.hardware.micrReader() ,jtl);
    newone.cardReader = new CardReader  (newone.hardware.msrReader()  ,jtl);
    newone.pinEntry=    new PinEntry    (newone.hardware.pinReader()  ,jtl);
    newone.former  =    new FormEntry   (newone.hardware.cat()        ,jtl);
    newone.clerkface=   newone.hardware.DisplayPad();//links to jtl later.
    newone.printer=     newone.hardware.Printer();
    return newone;
  }

}
//$Id: IviWrapper.java,v 1.10 2004/02/26 18:40:51 andyh Exp $
