package net.paymate.connection;

/**
* Title:        ReceiptGetReply
* Description:  get signature from server <p>
* Copyright:    2000 PayMate.net
* Company:      paymate
* @author       paymate
* @version      $Id: ReceiptGetReply.java,v 1.12 2001/10/18 05:33:03 andyh Exp $
*/

import  net.paymate.terminalClient.Receipt;
import  net.paymate.util.*;

public class ReceiptGetReply extends ActionReply implements isEasy {
  private static final ErrorLogStream dbg = new ErrorLogStream(ReceiptGetReply.class.getName(), ErrorLogStream.WARNING);

  public ActionType Type(){
    return new ActionType(ActionType.receiptGet);
  }

  private Monitor receiptGetReplyMonitor = new Monitor("ReceiptGetReply");
  private Receipt receipt      = null;
  private String  receiptString = null;

  public Receipt receipt() {
    try {
      receiptGetReplyMonitor.getMonitor();
      if(receipt == null) {
        dbg.VERBOSE("BEFORE: " + receiptString);
        receipt = new Receipt(receiptString);
//punitively expensive debug statement        dbg.VERBOSE("AFTER: " + receipt.toTransport().toString());
      }
    } finally {
      receiptGetReplyMonitor.freeMonitor();
    }
    return receipt;
  }

  public ReceiptGetReply setReceipt(String receiptString) {
    try {
      receiptGetReplyMonitor.getMonitor();
      this.receiptString = receiptString;
      receipt = null;
    } finally {
      receiptGetReplyMonitor.freeMonitor();
    }
    return this;
  }

  public ReceiptGetReply setReceipt(Receipt receipt) {
    try {
      receiptGetReplyMonitor.getMonitor();
      this.receiptString = null;
      this.receipt= receipt;
    } finally {
      receiptGetReplyMonitor.freeMonitor();
    }
    return this;
  }

  ////////////////////////
  /// constructors

  // default for transmission
  public ReceiptGetReply() {
    // use defaults
  }

  public ReceiptGetReply(Receipt receipt) {
    setReceipt(receipt);
  }

  public ReceiptGetReply(String receiptString) {
    setReceipt(receiptString);
  }

  //////////////////////////////
  // load/save stuff

  private static final String RECEIPTKEY = "RECEIPT";

  public void save(EasyCursor ezp){
    super.save(ezp);
    if((receiptString == null) && (receipt != null)) {
      receiptString = receipt.toTransport();
    }
    ezp.setString(RECEIPTKEY, receiptString);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    setReceipt(ezp.getString(RECEIPTKEY));
  }

}
//$Id: ReceiptGetReply.java,v 1.12 2001/10/18 05:33:03 andyh Exp $
