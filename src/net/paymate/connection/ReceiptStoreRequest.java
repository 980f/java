package net.paymate.connection;
/**
* Title:        $Source: /cvs/src/net/paymate/connection/ReceiptStoreRequest.java,v $
* Description:  send receipt to server
* Copyright:    2000-2002
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Revision: 1.27 $
* @todo scrub out legacy code when all legacy clients are gone.
*/

import  net.paymate.terminalClient.Receipt;
import  net.paymate.data.*; // id's
import  net.paymate.util.*;

import java.io.File;

public class ReceiptStoreRequest extends AdminRequest implements canBacklog, isEasy  {

  public ActionType Type(){
    return new ActionType(ActionType.receiptStore);
  }
  private Receipt receipt;

  private TxnReference original = TxnReference.New();
  public TxnReference reference(){
    return original;
  }

/**
 *implements canBacklog
 */
  File localFile;
  public File setLocalFile(File f){ return localFile=f;}
  public File getLocalFile(){ return localFile;}

/**
 * if the receipt was stoodin we need to accept bad clerk logins
 */
  public boolean fromHuman(){
    return true;//while associated transaction has already dealt with login priv...
    //we need to know which terminal to associate with the receipt.
    //--- restore to false when legacy clients are gone!!! this is a hack to deal with
    //incomplete txnreference objects, that screws up on stood in requests when
    //a bad client login was accepted.
  }

  private Monitor thisMonitor = new Monitor("ReceiptStoreRequest");

  public Receipt receipt() {
    return receipt;
  }

  public String toDiskImage() {
    return EasyCursor.makeFrom(receipt).asParagraph(OS.EOL);
  }

  public ReceiptStoreRequest(){// public for fromProperties()
    // use defaults
  }

  public static ReceiptStoreRequest New(Receipt receipt, TxnReference original) {
    ReceiptStoreRequest newone=new ReceiptStoreRequest();
    newone.receipt= receipt;
    newone.original= original;
    return newone;
  }

  //////////////////////////////
  // isEasy interface

  private static final String RECEIPTKEY = "RECEIPT";

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setObject(RECEIPTKEY,receipt);
    original.save(ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    receipt=(Receipt)ezp.getObject(RECEIPTKEY,Receipt.class);
    original.load(ezp);
  }
  /**
   * fixup internal reference for bugs previous to rev 1.27 of this module
   */
  public TxnReference patch(Terminalid terminalID,UTC requestInitiationTime){
    return original.patch(terminalID, requestInitiationTime);
  }
  /**
   * @return text suitable for filing the receipt on disk
   */
  public String image(char div) {
    return original.refNum();//
  }

}
//$Id: ReceiptStoreRequest.java,v 1.27 2002/04/30 18:55:34 andyh Exp $
