/**
 * Title:        ReceiptGetRequest
 * Description:  get signature from server <p>
 * Copyright:    2000<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ReceiptGetRequest.java,v 1.7 2001/07/24 17:52:57 mattm Exp $
 */

package net.paymate.connection;
import net.paymate.util.*;
import  net.paymate.ISO8583.data.TransactionID;

public class ReceiptGetRequest extends ActionRequest implements isEasy {
  public ActionType Type(){
    return new ActionType(ActionType.receiptGet);
  }

  public TransactionID tid = null;

  // default one for transmission
  public ReceiptGetRequest(){
     // stub
  }

  public ReceiptGetRequest(TransactionID tid){
     this.tid= TransactionID.NewCopy(tid);
  }

  //////////////////////////////
  // load/save stuff

  private static final String TIDKEY       = "TID";

  public void save(EasyCursor ezp){
    super.save(ezp);
    if(tid != null) {
      ezp.setString(TIDKEY, tid.image());
    }
  }

  public void load(EasyCursor ezp){
    super.load(ezp);
    tid = TransactionID.New(ezp.getString(TIDKEY));
  }

}
//$Id: ReceiptGetRequest.java,v 1.7 2001/07/24 17:52:57 mattm Exp $
