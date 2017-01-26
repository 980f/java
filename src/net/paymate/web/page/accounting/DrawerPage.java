package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/DrawerPage.java,v $</p>
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
//import net.paymate.database.ours.query.AuthStoreFullRow;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.DrawerClosingFormat;
import net.paymate.web.table.query.TerminalsFormat;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.database.ours.query.TerminalPendingRow;
import net.paymate.connection.*;

public class DrawerPage extends TxnListingPage {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DrawerPage.class);

  public DrawerPage(LoginInfo linfo,
                       AdminOpCode opcodeused, EasyProperties ezp, boolean archive) {
    this(linfo, opcodeused, ezp, null, archive);
  }
  public DrawerPage(LoginInfo linfo, AdminOpCode opcodeused, Drawerid applid, boolean archive) {
    this(linfo, opcodeused, null, applid, archive);
  }

  public DrawerPage(LoginInfo linfo, AdminOpCode opcodeused,
                    EasyProperties ezp, Drawerid drawerid, boolean archive) {
    super(linfo, opcodeused, archive);
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    if(!Drawerid.isValid(drawerid)) {
      drawerid = new Drawerid(ezp.getInt(DrawerClosingFormat.BMID))    ;
    }
    // +++ give the title something meaningful?
    String title = "";
    TxnRow stmt = null;
    boolean history = false;
    TerminalInfo tinfo = null;
    // +++ How do we check to see that we have access to this bookmark?  Using this method, other people can access this drawer closing, can't they?
    if(drawerid.isValid()) {
      history = true;
      stmt = db.getDrawerClosingCursor(drawerid, linfo.ltf());
      title = stmt.title();
      Terminalid tid = db.getTerminalForDrawer(drawerid);
      tinfo = db.getTerminalInfo(tid);
    } else {
      history = false;
      // see if this is for the terminals current drawer
      String terminalidfullname = ezp.getString(TerminalsFormat.TERMID);
      dbg.WARNING("pending pages for terminal " + TerminalsFormat.TERMID + "=" + terminalidfullname);
      Terminalid termid = new Terminalid(terminalidfullname);
      if (termid.isValid()) {
        tinfo = db.getTerminalInfo(termid);
        String terminalname = db.getTerminalName(termid);
        if (linfo.permits(AssociatePermissions.PermitClose)) {
          // handle the closing, if needed
          String DCPWD = "dcpwd";
          String dcpwd = ezp.getString(DCPWD);
          if(StringX.NonTrivial(dcpwd)) {
            String status = "";
            if (linfo.assoc.passes(dcpwd)) {
              // this means they wanted to close the drawer
              dbg.ERROR("Attempting drawer closing from web for terminal t=" + terminalidfullname);
              BatchRequest br = BatchRequest.JustClose(BatchRequest.WITHOUTDETAIL);
              br.setTerminalInfo(termid);
              ActionReply ar = ConnectionServer.THE().generateReply(br, linfo, false, null, null, true, null);
              status = "Attempting to close drawer on terminal " + terminalname + " result: " + ar.status.Image();
            } else {
              status = "Attempting to close drawer on terminal " +
                  terminalname + " result: " + "Drawer NOT closed.  Password mismatch!";
            }
            ec.addElement(status + BRLF);
            dbg.ERROR(status);
          }
          // add the form for closing the drawer - ALWAYS print this form for those with permissions
          TD td1 = new TD().addElement("Enter Password to close drawer: ");
          TD td2 = new TD().addElement(new Input(Input.PASSWORD, DCPWD, ""));
          TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Close drawer"));
          TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
          Table t = new Table().addElement(tr1);
          Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.drawerAdminOp.url()).
              addElement(new Input(Input.HIDDEN, "t", String.valueOf(termid))).
              addElement(t);
          ec.addElement(f);
        }
        UTC starttime = db.getPendingStartTime(termid); // get this separately?
        title = "Current Drawer for terminal " + terminalname +
            " from " + linfo.ltf().format(starttime) + " to " +
            linfo.ltf().format(DateX.Now());
        stmt = db.unsettledTxnsQuery(termid);
      }
    }
    boolean showAVS = (tinfo != null) ? tinfo.askforAvs() : false;
    // only void the txns in the current drawer (+++ fix this to rely on batch status!)
    Element utf = doDetailsWithSubs(stmt, title, true /* countLosses */, linfo, showAVS, archive);
    if(utf != null) {
      ec.addElement(utf);
      if(linfo.isaGod() && Drawerid.isValid(drawerid) && ! archive) {
        ec.addElement(RecordEditPage.editRecord(db.drawer, drawerid, false/*write*/, null, linfo));
      }
    } else {
      dbg.ERROR(history ? "Bookmark is trivial." : "Terminal not found");
      ec.addElement("Unable to locate "+(history?"bookmark":"terminal")+".  Please notify webmaster.");
    }
    fillBody(ec);
  }

}