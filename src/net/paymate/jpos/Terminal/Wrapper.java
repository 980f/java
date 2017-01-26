package net.paymate.jpos.Terminal;//moveto terminalClient or np.peripheral
/**
 * Title:        $Source: /cvs/src/net/paymate/jpos/Terminal/Wrapper.java,v $
 * Description:  legacy wrapper for jpos devices
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.46 $
 * @todo implement or purge detachAll()
 */

//until we have a terminalGenerator registry:
import net.paymate.ivicm.*;
import net.paymate.ivicm.ec3K.*;

import net.paymate.serial.*;

import net.paymate.util.*;
import net.paymate.awtx.print.*;

public class Wrapper {
  static final Tracer dbg=new Tracer(Wrapper.class);

  private QReceiver posterm;
  private IviTrio   hardware;

  //things that can spontaneoulsy throw JposExceptions that we wish to convert
  //into events.
  public CheckReader    checkReader;
  public CardReader     cardReader ;
  public PinEntry       pinEntry   ;
  public FormEntry      former     ;
  public LinePrinter    prn        ;
  public PrinterModel   printer    ;
  public DisplayPad     clerkface  ;

  public boolean haveSigCap(){
    return hardware.haveSigCap();
  }

  public void detachAll() {//prepare to restart
  //unlink components
  }

  private Wrapper(){
//
  }


  private static IviTrio ivilegacy(String trackingname,EasyCursor equipmentlist){
    return IviTrio.New(trackingname,equipmentlist);
  }

  public static Wrapper fromDescription(QReceiver jtl,EasyCursor equipmentlist){
    Wrapper newone=new Wrapper();
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
//$Id: Wrapper.java,v 1.46 2003/08/04 22:23:48 andyh Exp $
