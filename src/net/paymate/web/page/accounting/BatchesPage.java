package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/BatchesPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

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
import net.paymate.web.table.query.BatchesFormat;
import net.paymate.database.ours.query.BatchesRow;
import net.paymate.web.table.query.CardSubtotalsFormat;

public class BatchesPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(BatchesPage.class);

  public BatchesPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezc, boolean archive) {
    super(linfo, opcodeused, archive);
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    TimeRange newWebDate = null;
    if(archive) {
      newWebDate = TimeRange.Forever();// get the full range of web dates for this customer !!!
    } else {
      TimeRange lastDate = DrawersPage.TimeSearch(
          db.mostRecentStoreBatch(linfo.store.storeId()), linfo.ltf());
      newWebDate = DrawersPage.EntryForm(ec, ezc, lastDate,
          linfo.ltf().getZone(), this,
          AdminOp.BatchesAdminOp.url());
    }
    if(newWebDate != null) {
      SubTotaller totaller = new SubTotaller(); // new stuff
      TableGen utf = new BatchesFormat(BatchesRow.NewSet(
          db.runStoreBatchQuery(linfo.store.storeId(), linfo.isaGod(), newWebDate)),
                                       "Batches for "+linfo.store.storename, linfo, totaller, archive);
      if(utf != null) { // this is the new stuff
        ec.addElement(utf);
        if(linfo.store.enlistsummary && ! archive) {
          CardSubtotalsFormat utf2 = new CardSubtotalsFormat(linfo, totaller, "Summary");
          if(utf2 != null) {
            ec.addElement(utf2);
          }
        }
      }
    } else {
      // ???
    }
    fillBody(ec);
  }

}