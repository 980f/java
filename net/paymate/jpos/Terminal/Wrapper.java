/* $Id: Wrapper.java,v 1.39 2001/07/19 01:06:50 mattm Exp $ */
package net.paymate.jpos.Terminal;

import jpos.ServiceTracker;
import net.paymate.util.*;
import net.paymate.awtx.print.*;
/*
This class has almost been gutted. It can be completely removed with a trivial
effort if someone wishes to do so, moving its contents into PosTerminal.
*/

public class Wrapper {
  static final Tracer dbg=new Tracer(Wrapper.class.getName());

  //the common user for the services that follow:
  protected Listener thePOSapp;
  //things that can spontaneoulsy throw JposExceptions that we wish to convert
  //into events.
  public CheckReader    checkReader;
  public CardReader     cardReader ;
  public PinEntry       pinEntry   ;
  public FormEntry      former     ;
//  public LinePrinter    prn        ;
  public PrinterModel   printer    ;
//  public CM3000UI       clerkface  ;


  public void restart(){
  //  detachAll();
//  former.
  //  attachAll();
  }

  public void detachAll() {//prepare to restart
    cardReader.Release();
    pinEntry.Release();
    former.Release();
    checkReader.Release();
    //prn is not a true service,+++ needs a finalizer if we don't implement a Release() for it
  }

  public void attachAll(String id) {
    dbg.Enter("attachAll:"+id);
    try {
      //attach each of our parts
      //+++ all error returns are being ignored!
      dbg.mark("cardreader");
      cardReader.Attach(id);
      dbg.mark("pinentry");
      pinEntry.Attach(id);
      dbg.mark("formentry");
      former.Attach(id);
      dbg.mark("checkreader");
      checkReader.Attach(id);

      dbg.mark("printer");
      try {  //not strictly a jpos device, but wrapper is a nice place for it.
        printer= (PrinterModel) ServiceTracker.getService(DeviceName.ReceiptPrinter);
      } catch (Exception jape){
        dbg.Caught("Attaching printer:",jape);
      }
      if(printer==null){
        printer= new PrinterModel();//on error dummy it up
      }
    }
    catch (Exception caught) {
      dbg.Caught(caught);
    } finally {
      dbg.Exit();
    }
  }

  public Wrapper(Listener jtl){
    try {
      dbg.Enter("Wrapper()");
      thePOSapp=jtl;
      cardReader =new CardReader (jtl);
      pinEntry   =new PinEntry   (jtl);
      former     =new FormEntry  (jtl);
      checkReader=new CheckReader(jtl);
//      prn= new LinePrinter("NULL PRINTER");
    } catch (Exception caught) {
      dbg.Caught(caught);
    } finally {
      dbg.Exit();
    }
  }

}
//$Id: Wrapper.java,v 1.39 2001/07/19 01:06:50 mattm Exp $
