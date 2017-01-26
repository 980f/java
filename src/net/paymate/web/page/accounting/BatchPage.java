package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/BatchPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.10 $
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
import net.paymate.web.table.query.TerminalsFormat;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.database.ours.query.TerminalPendingRow;
import net.paymate.connection.*;

public class BatchPage extends TxnListingPage {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(BatchPage.class);

  public BatchPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp, boolean archive) {
    this(linfo, opcodeused, ezp, null, archive);
  }
  public BatchPage(LoginInfo linfo, AdminOpCode opcodeused, Batchid batchid, boolean archive) {
    this(linfo, opcodeused, null, batchid, archive);
  }

  public BatchPage(LoginInfo linfo, AdminOpCode opcodeused,
                   EasyProperties ezp, Batchid batchid, boolean archive) {
    super(linfo, opcodeused, archive);
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    if(!Batchid.isValid(batchid)) {
      batchid = new Batchid(ezp.getInt(BatchesFormat.BID));
    }
    Element utf = null;
    // +++ How do we check to see that we have access to this bookmark?  Using this method, other people can access this drawer closing, can't they?
    if(batchid.isValid()) {
      Terminalid terminalid = db.getTerminalForBatch(batchid);
      TerminalInfo ti = db.getTerminalInfo(terminalid);
      if(linfo.isaGod() && ! archive) {
        String rpw = ezp.getString("rpw");
        boolean canResubmit = db.canResubmitBatch(batchid); // checks to see if the actioncode is D or F and if the count > 0
        Authid authid = db.getAuthForBatch(batchid);
        canResubmit &= !ti.isGateway();
        if(StringX.NonTrivial(rpw)) {  // this means they wanted to resubmit the batch
          if(canResubmit) {
            if(linfo.assoc.passes(rpw)) {
              boolean succeeded = ConnectionServer.THE().authmgr.retrySubmittal(db, batchid);
              ec.addElement(new StringElement("Resubmittal of batch # " + batchid + " was " + (succeeded ? "" : "NOT ") + "spawned!"));
            } else {
              String res = "Cannot resubmit due to invalid password";
              dbg.WARNING(res + " linfo="+linfo);
              ec.addElement(res);
            }
          }
        }
        if(canResubmit) {
          // make sure that the record was a failure
          // add the batches resubmit stuff IF the batch failed AND if the batch has count>0 (has not already been retried) !!!!
          TD td1 = new TD().addElement("Enter Password to resubmit: ");
          TD td2 = new TD().addElement(new Input(Input.PASSWORD, "rpw", ""));
          TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Resubmit batch"));
          TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
          Table t = new Table().addElement(tr1);
          Form f = NewPostForm("./"+BatchesFormat.URL(batchid)).
              addElement(new Input(Input.HIDDEN, BatchesFormat.BID, String.valueOf(batchid))).
              addElement(t);
          ec.addElement(f);
        }
      }
      TxnRow stmt = db.getBatchCursor(batchid, linfo.ltf());
      boolean showAVS = (ti != null) ? ti.askforAvs() : false;
      utf = doDetailsWithSubs(stmt, stmt.title(), false /* countLosses */, linfo, showAVS, archive);
    }
    if(utf != null) {
      ec.addElement(utf);
      if(linfo.isaGod() && ! archive) {
        ec.addElement(RecordEditPage.editRecord(db.batch, batchid, false, null, linfo));
      }
    } else {
      dbg.ERROR("Batchid is trivial.");
      ec.addElement("Unable to locate batch.  Please notify webmaster.");
    }
    fillBody(ec);
  }

}