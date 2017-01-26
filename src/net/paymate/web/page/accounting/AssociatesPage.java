package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/AssociatesPage.java,v $</p>
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
import net.paymate.web.table.query.AssociatesFormat;
import net.paymate.data.sinet.business.*;

public class AssociatesPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AssociatesPage.class);

  public AssociatesPage(LoginInfo linfo, AdminOpCode opcodeused, String notes) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    // add a button to create a new associate for gawds
    ElementContainer ec = new ElementContainer();
    if(StringX.NonTrivial(notes)) {
      ec.addElement(notes);
    }
    if(linfo.isaGod()) {
      TD td1 = new TD().addElement("Enter Password to create a new associate: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, newassocpw, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Create associate"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.newAssociate.url()).
          addElement(t);
      ec.addElement(f);
    }
    Associate [ ] assocs = linfo.enterprise.associates.getAll();
    Associateid[] associds = new Associateid[(assocs != null) ? assocs.length : 0];
    for(int i = assocs.length; i-->0;) {
      associds[i] = assocs[i].associateid();
    }
    ec.addElement(new AssociatesFormat(linfo, associds, "Associates"));
    fillBody(ec);
  }

}