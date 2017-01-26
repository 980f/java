package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/AssociatePage.java,v $</p>
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
import net.paymate.web.table.query.StoreaccessesFormat;
import net.paymate.web.table.query.AssociatesFormat;
import net.paymate.data.sinet.business.*;

public class AssociatePage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AssociatePage.class);

  public AssociatePage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    this(linfo, opcodeused, ezp, null);
  }
  public AssociatePage(LoginInfo linfo, AdminOpCode opcodeused, Associateid applid) {
    this(linfo, opcodeused, null, applid);
  }

  public AssociatePage(LoginInfo linfo, AdminOpCode opcodeused,
                       EasyProperties ezp, Associateid associd) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    if(!Associateid.isValid(associd)) {
      associd = new Associateid(ezp.getInt(Associate.ASSOCIATEID));
    }
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ec.addElement(RecordEditPage.editRecord(db.associate, associd, false/*write*/, null, linfo));
    // +++ eventually do this just for the current store of the current user
    ec.addElement(new StoreaccessesFormat(linfo, db.getStoreaccesses(associd), "StoreAccess", null));
    fillBody(ec);
  }

}