package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/TerminalsPage.java,v $</p>
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
import net.paymate.web.table.query.TerminalsFormat;

public class TerminalsPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TerminalsPage.class);

  public TerminalsPage(LoginInfo linfo, AdminOpCode opcodeused) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    fillBody(new TerminalsFormat(linfo, db.getTerminalsForStore(linfo.store.storeId()),"Terminals", null));
  }

}