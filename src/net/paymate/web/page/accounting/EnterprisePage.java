package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/EnterprisePage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
import net.paymate.web.table.*;
import net.paymate.data.*;
import net.paymate.data.sinet.business.*;
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

public class EnterprisePage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EnterprisePage.class);

  public EnterprisePage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    this(linfo, opcodeused, ezp, null);
  }
  public EnterprisePage(LoginInfo linfo, AdminOpCode opcodeused, Enterpriseid applid) {
    this(linfo, opcodeused, null, applid);
  }

  public EnterprisePage(LoginInfo linfo, AdminOpCode opcodeused,
                        EasyProperties ezp, Enterpriseid entid) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    if(!Enterpriseid.isValid(entid)) {
      entid = new Enterpriseid(ezp.getInt(Login.ENTID));
    }
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    fillBody(RecordEditPage.editRecord(db.enterprise, entid,
                                       false/*write*/, null, linfo));
  }

}