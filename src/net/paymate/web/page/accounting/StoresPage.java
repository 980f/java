package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/StoresPage.java,v $</p>
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
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.StoreFormat;
import net.paymate.database.ours.query.DrawerRow;

public class StoresPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoresPage.class);

  public StoresPage(LoginInfo linfo, AdminOpCode opcodeused, String notes) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    if(StringX.NonTrivial(notes)) {
      ec.addElement(notes);
    }
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ec.addElement(new StoreFormat(linfo, db.storesInfoQuery(linfo.enterprise.enterpriseid()),
                                  "Stores", null)
                  );
    fillBody(ec);
  }

}