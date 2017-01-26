package net.paymate.web.page.accounting;

import net.paymate.web.page.Acct;
import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.AuthStoreFullRow;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.UnsettledTransactionFormat;
import net.paymate.web.table.query.CardSubtotalsFormat;
import net.paymate.web.table.query.AVSSubtotalsFormat;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.database.ours.query.TerminalPendingRow;
import net.paymate.connection.*;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/TxnListingPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
 */

public class TxnListingPage extends Acct {
  public TxnListingPage(LoginInfo linfo, AdminOpCode opcodeused, boolean archive) {
    super(linfo, opcodeused, archive);
  }

  // used by deposit, drawer, and batch pages
  protected static Element doDetailsWithSubs(TxnRow stmt, String title,
                                             boolean countLosses, LoginInfo linfo,
                                             boolean showAVS, boolean archive) {
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    TextList tl = db.getStorePayInst(linfo.store.storeId());
    TextList avstl = AVSDecoder.getAVSCodeSet();
    SubTotaller totaller = new SubTotaller(tl);
    SubTotaller avstotaller = new SubTotaller(avstl);
    // Since the code below runs long before we actually generate its content,
    // the subtotaller names available to the CardSubtotalsFormat is that of the storeauth only, and not that of the txns displayed
    // this is solved in the CardSubtotalsFormat via overloading the output() function.
    UnsettledTransactionFormat utf  = new UnsettledTransactionFormat(linfo,
        stmt, totaller, avstotaller, title, null, countLosses, archive);
    CardSubtotalsFormat utf2 = new CardSubtotalsFormat(linfo, totaller, "Summary");
    AVSSubtotalsFormat utf3 = null;
    if(showAVS || (linfo.isaGod() && ! archive)) { // always show the avs stuff for gawds, just in case
      utf3 = new AVSSubtotalsFormat(linfo, avstotaller, "AVS Summary");
    }
    if(utf != null) {
      ec.addElement(utf);
      if(utf2 != null) {
        ec.addElement(PayMatePage.BRLF);
        ec.addElement(utf2);
      }
      if(utf3 != null) { // might be null if not supposed to show it
        ec.addElement(PayMatePage.BRLF);
        ec.addElement(utf3);
      }
    }
    return ec;
  }
}
