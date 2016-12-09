package net.paymate.terminalClient;

/**
* Title:
* Description:
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @author $Author: andyh $
* @version $Id: ReceiptManager.java,v 1.18 2001/10/22 23:33:39 andyh Exp $
*/

import net.paymate.connection.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.awt.*;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.ISO8583.data.*;
import net.paymate.util.*;


public class ReceiptManager {
  /**
  * rip==Receipt In Progress
  */
  protected Receipt rip=null;
  protected int reprints=0;
  protected boolean printCheckReceipts=false;
  protected boolean printVoidReceipts=true;

  private static final ErrorLogStream dbg = new ErrorLogStream(ReceiptManager.class.getName());

  public Receipt Receipt(){
    return rip;
  }

  public boolean shouldPrint(FinancialRequest request){
    switch(request.Type().Value()){
      default: return true;
      case ActionType.check: return printCheckReceipts;
      case ActionType.reversal: return printVoidReceipts;
    }
  }

  /**
  * log in a receipt, rest of printing will be done outside scope
  */
  public Receipt start(FinancialRequest request,PrinterModel printer,TerminalInfo termInfo,ClerkIdInfo clerk){
    dbg.Enter("start");
    try {
      reprints=0;
      rip= new Receipt();
      //the order of the following sets the order in the receipt...!!!
      rip.setItem(request);
      rip.setItem(clerk);
      rip.setItem(termInfo);
      rip.printWorthy(shouldPrint(request));
      rip.startPrint(printer,0);
      return rip;
    }
    finally {
      dbg.Exit();
    }
  }

  /**
  * call when signing completes the receipt.
  */
  public Receipt signed(SigData siggy){
    dbg.Enter("signed");
    try {
      if(rip!=null){
        rip.setItem(Hancock.Create(siggy));
        rip.signAndShove();//prints sig, trailer,then feeds
      }
      return rip;
    }
    finally {
      dbg.Exit();
    }
  }

  /**
  * emit receipt, without an available signature. If one is required the receipt class itself makes a manual signing spot.
  */
  public Receipt unsigned(){
    dbg.Enter("unsigned");
    try {
      rip.dropSigning(); //drop the signature even if it exists
      rip.signAndShove();//prints sig, trailer,then feeds
      return rip;
    }
    finally {
      dbg.Exit();
    }
  }

  public Receipt onReply(FinancialReply reply){
    dbg.Enter("onReply");
    try {
      rip.setItem(reply);
      //even if previously silent if there are errors we want to print
      if(net.paymate.util.TextList.NonTrivial(reply.Errors)){
        rip.printWorthy(true);//print even if not normally printed
      }
      rip.printBody();
      return rip;
    }
    finally {
      dbg.Exit();
    }

  }

  public void fault(ActionReply reply,PrinterModel printer){
    if(printer == null) {
      dbg.ERROR("fault():printer is null!");
      return;
    }
    if(reply == null) {
      dbg.ERROR("fault():reply is null!");
      return;
    }
    if(rip == null){
      dbg.VERBOSE("fault():rip is null!");
    }
    //trip==temporary receipt, not stored for reprint.
    Receipt trip= (rip==null || rip.hasReply())?new Receipt():rip;
    trip.onFailure(reply,printer);
    trip.printWorthy(true);
    trip.printBody();
    printer.formfeed();
  }

  /**
  * rePrint is designed to print even receipts that normally are not printed
  */
  public void rePrint(PrinterModel printer){
    if(rip!=null){
      rip.printWorthy(true);
      rip.print(printer,++reprints);
    }
  }

  public ReceiptManager setOptions(EasyCursor ezc){
    printCheckReceipts=ezc.getBoolean("forChecks",false);
    printVoidReceipts=ezc.getBoolean("forVoids",true);
    return this;
  }

  public ReceiptManager() {
    //empty
  }

}
//$Id: ReceiptManager.java,v 1.18 2001/10/22 23:33:39 andyh Exp $
