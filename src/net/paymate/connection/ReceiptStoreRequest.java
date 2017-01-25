/**
* Title:        ReceiptStoreRequest
* Description:  store receipt on server <p>
* Copyright:    2000<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: ReceiptStoreRequest.java,v 1.15 2001/10/02 17:06:37 mattm Exp $
*/

package net.paymate.connection;
import  net.paymate.terminalClient.Receipt;
import  net.paymate.ISO8583.data.TransactionID;
import  net.paymate.util.*;

import java.io.File;

public class ReceiptStoreRequest extends ActionRequest implements canBacklog, isEasy  {

  public ActionType Type(){
    return new ActionType(ActionType.receiptStore);
  }
  private boolean       loaded         = false;//we use this to defer parsing
  private Receipt       receipt       = null; //...this object
  private String        receiptString = null; //... from this string

  public  TransactionID tid            = TransactionID.Zero();//remove null checking
/////////////////

/**
 *implements canBacklog
 */
  File localFile;
  public File setLocalFile(File f){ return localFile=f;}
  public File getLocalFile(){ return localFile;}
/////////////////
/**
 * if the receipt was stoodin we need to accept bad clerk logins
 */
  public boolean fromHuman(){
    return true; //false;//--- remove when +++ %%% login security hole is fixed.
  }
///////////////

  private Monitor thisMonitor = new Monitor("ReceiptStoreRequest");

  public Receipt receipt() {
    Receipt retval = null;
    try {
      thisMonitor.getMonitor();
      if(!loaded) {
        receipt = new Receipt(receiptString);
        loaded = true;
      }
      retval = receipt;
    } finally {
      thisMonitor.freeMonitor();
      return retval;
    }
  }

  public String receiptString() {
    if((receiptString == null) && (receipt != null)) {
      receiptString = receipt.toTransport();
    }
    return receiptString;
  }

  public void setReceipt(String receiptString) {
    try {
      thisMonitor.getMonitor();
      this.receiptString = receiptString;
      receipt = null;
      loaded = false;
    } finally {
      thisMonitor.freeMonitor();
    }
  }

  ////////////////////////
  /// constructors

  // default one for transmission
  protected ReceiptStoreRequest(){
    // use defaults
  }

  public ReceiptStoreRequest(Receipt receipt, TransactionID tid) {
    this.receipt= new Receipt(receipt);
    this.tid= TransactionID.NewCopy(tid);
    loaded = true;
  }

  public ReceiptStoreRequest(String receiptString) {
    this(null, null);
    this.receiptString = receiptString;
    setReceipt(receiptString);
  }

  //////////////////////////////
  // load/save stuff

  private static final String RECEIPTKEY = "RECEIPT";
  private static final String TIDKEY     = "RECEIPTTID";

  public void save(EasyCursor ezp){
    super.save(ezp);
    ezp.setString(RECEIPTKEY, receiptString());
    tid.saveas(TIDKEY,ezp);
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    receiptString = ezp.getString(RECEIPTKEY);
    tid.loadfrom(TIDKEY,ezp);
  }

}
//$Id: ReceiptStoreRequest.java,v 1.15 2001/10/02 17:06:37 mattm Exp $
