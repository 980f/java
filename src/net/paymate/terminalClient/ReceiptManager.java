package net.paymate.terminalClient;

/**
* Title:        $Source: /cvs/src/net/paymate/terminalClient/ReceiptManager.java,v $
* Description:  Posterminal's receipt management.
* Copyright:    Copyright (c) 2000
* Company:      PayMate.net
* @version $Revision: 1.33 $
 * @todo: 2nd receipt doesn't have header or footer, at least not when auto printed.
*/

import net.paymate.connection.*;
import net.paymate.jpos.data.*;
import net.paymate.jpos.awt.*;
import net.paymate.awtx.print.PrinterModel;
import net.paymate.data.*;
import net.paymate.util.*;


public class ReceiptManager {
  /**
  * rip==Receipt In Progress
  */
  private Receipt rip=null;
  private int reprints=0;
  PrinterModel printer; //where we last printed

  private boolean printCheckReceipts=false;
  private boolean printVoidReceipts=true;

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ReceiptManager.class);

  public Receipt Receipt(){
    return rip;
  }

  public Receipt AdminReceipt(){
    Receipt newone=new Receipt();
    newone.setOptions(recipe(), timezone(),mrp);
    return newone;
  }


/**
 * @return receipt gets printed, @param depends primarliy upon request type.
 */
  public boolean shouldPrint(PaymentRequest request){
    switch(request.OperationType().Value()){
      default: return true;
//      case ActionType.check: return printCheckReceipts;
      case TransferType.Reversal: return printVoidReceipts;
    }
  }

  /**
  * log in a receipt, rest of printing will be done outside scope
  */
  public Receipt start(PaymentRequest  request,PrinterModel printer,TerminalInfo termInfo,ClerkIdInfo clerk){
    dbg.Enter("start");
    try {
      reprints=0;
      rip= new Receipt();
      rip.setOptions(recipe, timezone,mrp);
      this.printer=printer;
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
  public Receipt firstCopy(SigData siggy){
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
  public Receipt firstCopy(boolean pretty){
    dbg.Enter("unsigned");
    try {
      rip.manSigning(pretty); //drop the signature even if it exists
      rip.signAndShove();//prints sig, trailer,then feeds
      return rip;
    }
    finally {
      dbg.Exit();
    }
  }

  public Receipt secondCopy(){
    //force next print to be pretty, but don't change what reprint reprints!
    Receipt prettyone=Receipt.Prettify(rip);
    if(prettyone!=null){
      prettyone.print(printer,0);
    }
    return prettyone;
  }

  public Receipt onReply(PaymentReply reply){
    dbg.Enter("onReply");
    try {
      rip.setItem(reply);
      //even if previously silent if there are errors we want to print
      if(TextList.NonTrivial(reply.Errors)){
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
    rip= (rip==null || rip.hasReply()) ? new Receipt() : rip;
    this.printer=printer;
    rip.onFailure(reply,printer);
    rip.printWorthy(true);
    rip.printBody();
    printer.formfeed();
  }

  /**
   * void receipt in progress
   * @param voider is request that does the voiding.
   */
  public void modifyReceiptInProgress(PaymentRequest voider){
    rip.setItem(voider);
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

  public void rePrint(){
    rePrint(printer);//use last used printer
  }

  public ReceiptManager setOptions(EasyCursor ezc){
    printCheckReceipts=ezc.getBoolean("forChecks",false);
    printVoidReceipts=ezc.getBoolean("forVoids",true);
    return this;
  }

  /**
   * @return whether this receipt should be saved on paymate server
   */
  public boolean shouldbeSaved(){
    return rip!=null && rip.shouldbeCaptured();
  }

  public ReceiptManager() {
    //empty
  }

  ///////////////////////////////////////////
  //format controls, for all subsequent receipts
  private LocalTimeFormat localTime=LocalTimeFormat.Utc(ReceiptFormat.DefaultTimeFormat);

  private ReceiptFormat recipe = null;
  private String timezone = null;
  private String mrp=null;

  private final void setTimeFormat(String tz, String newformat){
    try {
      localTime= LocalTimeFormat.New(tz,newformat);
    } catch(Exception ignored){
      //leave time format alone.
      dbg.ERROR("Incoming time format rejected:"+newformat);
    }
  }

  public final LocalTimeFormat Formatter(){
    return localTime;
  }

  public final String timezone() {
    return timezone;
  }

  public final ReceiptFormat recipe() {
    return recipe;
  }

  public final void setOptions(ReceiptFormat recipe, String timezone, String mrp){
    dbg.VERBOSE("Setting options:"+recipe.showSignature+" "+recipe.TimeFormat);
    this.recipe = recipe;
    this.timezone = timezone;
    setTimeFormat(timezone, recipe.TimeFormat);
    this.mrp=mrp;
  }
  //end format controls
  //////////////
}
//$Id: ReceiptManager.java,v 1.33 2003/10/25 20:34:25 mattm Exp $
