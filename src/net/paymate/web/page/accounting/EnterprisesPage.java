package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/EnterprisesPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.8 $
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
import net.paymate.web.table.query.EnterprisesFormat;
import net.paymate.data.sinet.business.EnterpriseHome;

public class EnterprisesPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EnterprisesPage.class);

  public EnterprisesPage(LoginInfo linfo, AdminOpCode opcodeused, String notes) {
    this(linfo, opcodeused, new StringElement(notes));
  }
  public EnterprisesPage(LoginInfo linfo, AdminOpCode opcodeused, Element elemNotes) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    if(elemNotes != null) {
      ec.addElement(elemNotes);
    }
    // enterprises list for gawds
    // add a button to create a new enterprise.
    if(linfo.isaGod()) {
      TD td1 = new TD().addElement("Enter Password to create a new enterprise: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, newentpw, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Create enterprise"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.newEnterprise.url()).addElement(t);
      ec.addElement(f);
    }
    // display the page
//    usession.db.getEnterprises()
    ec.addElement(new EnterprisesFormat(linfo, EnterpriseHome.GetAllEnabledNameOrder(),
                                        "Enterprises", null));

    fillBody(ec);
  }

}