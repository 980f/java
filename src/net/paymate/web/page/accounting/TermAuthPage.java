package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/TermAuthPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.5 $
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
import net.paymate.web.table.query.TermAuthsFormat;

public class TermAuthPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TermAuthPage.class);

  public TermAuthPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties req) {
    this(linfo, opcodeused, req, null);
  }
  public TermAuthPage(LoginInfo linfo, AdminOpCode opcodeused, TermAuthid termauthid) {
    this(linfo, opcodeused, null, termauthid);
  }

  public TermAuthPage(LoginInfo linfo, AdminOpCode opcodeused,
                      EasyProperties req, TermAuthid termauthid) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(!TermAuthid.isValid(termauthid)) {
      termauthid = new TermAuthid(req.getInt(TermAuthsFormat.TERMAUTHID));
    }
    ec.addElement(RecordEditPage.editRecord(db.termauth, termauthid, false/*write*/, null, linfo));
    fillBody(ec);
  }

}