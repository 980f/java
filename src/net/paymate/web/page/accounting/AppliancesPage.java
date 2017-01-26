package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/AppliancesPage.java,v $</p>
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
import net.paymate.web.table.query.AppliancesFormat;
import net.paymate.database.ours.query.DrawerRow;
import net.paymate.data.sinet.hardware.*;

public class AppliancesPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AppliancesPage.class);

  public AppliancesPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    this(linfo, opcodeused, ezp, null);
  }

  private static final String ALL = "ALL";

  public AppliancesPage(LoginInfo linfo, AdminOpCode opcodeused,
                        EasyProperties ezp, String notes) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    if (StringX.NonTrivial(notes)) {
      ec.addElement(notes);
    }
    boolean all = false;
    if (linfo.isaGod()) {
      // get the parameter
      all = ezp.getBoolean(ALL);
      // display the parameter
      String url = fullURL(AdminOp.AppliancesAdminOp.url());
      TD td1 = new TD().addElement(new A(url + "&ALL=" + Bool.TRUE(), "ALL"));
      TD td2 = new TD().addElement(new A(url, "Store's"));
      TR tr1 = new TR().addElement(td1).addElement(td2);
      Table t = new Table().addElement(tr1);
      ec.addElement(t);
      ec.addElement(BRLF);
    }
    Applianceid [ ] appliances = null;
    if(all) {
      appliances = ApplianceHome.GetAllIds();
    } else {
      appliances = linfo.store.appliances.getAllIds();
    }
    ec.addElement(new AppliancesFormat(linfo, appliances, "Appliances"));
    fillBody(ec);
  }

}