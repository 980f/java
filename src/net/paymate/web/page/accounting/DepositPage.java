package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/DepositPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.9 $
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
import net.paymate.web.table.query.DepositFormat;
import net.paymate.web.table.query.TerminalsFormat;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.database.ours.query.TerminalPendingRow;
import net.paymate.connection.*;

public class DepositPage extends TxnListingPage {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DepositPage.class);

  public DepositPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    this(linfo, opcodeused, ezp, null);
  }
  public DepositPage(LoginInfo linfo, AdminOpCode opcodeused, TermAuthid termauthid) {
    this(linfo, opcodeused, null, termauthid);
  }

  public DepositPage(LoginInfo linfo, AdminOpCode opcodeused,
                     EasyProperties ezp, TermAuthid termauthid) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    if(!TermAuthid.isValid(termauthid)) {
      termauthid = new TermAuthid(ezp.getInt(DepositFormat.TAID));
    }
    if(termauthid.isValid()) {
      Authid authid = db.getAuthForTermauth(termauthid);
      Terminalid terminalid = db.getTerminalForTermauth(termauthid);
      TerminalInfo tinfo = db.getTerminalInfo(terminalid);
      String termname = "";
      boolean showAVS = false;
      if(tinfo != null) {
        termname = tinfo.getNickName();
        showAVS = tinfo.askforAvs();
      }
      Authorizer auth = ConnectionServer.THE().authmgr.findAuthById(authid);
      String authname = auth.serviceName();//usession.db.getAuthName(authid);
      String title = "Deposit from Terminal " + termname + " to authorizer " + authname;
      fillBody(doDetailsWithSubs(db.unsettledTxnsQuery(termauthid),
                                 title, true /* countLosses */, linfo, showAVS, false /*we will never create this page in archive mode*/));
    }
  }

}