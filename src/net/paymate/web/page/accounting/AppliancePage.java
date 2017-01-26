package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/AppliancePage.java,v $</p>
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
import net.paymate.data.sinet.hardware.*;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.AppliancesFormat;

public class AppliancePage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AppliancePage.class);

  public AppliancePage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    this(linfo, opcodeused, ezp, null);
  }
  public AppliancePage(LoginInfo linfo, AdminOpCode opcodeused, Applianceid applid) {
    this(linfo, opcodeused, null, applid);
  }

  public AppliancePage(LoginInfo linfo,
                       AdminOpCode opcodeused, EasyProperties ezp, Applianceid applid) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    if(!Applianceid.isValid(applid)) {
      applid = new Applianceid(ezp.getInt(AppliancesFormat.APPLID));
    }
    Applianceid [ ] appliances = {
        applid,
    };
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ec.addElement(new AppliancesFormat(linfo, appliances, "Appliances"));
    if(linfo.isaGod()) {
      TD td1 = new TD().addElement("Enter Password to create a new terminal: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, newtermpw, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Create terminal"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.newTerminal.url()).
          addElement(t);
      f.addElement(new Input(Input.HIDDEN, AppliancesFormat.APPLID, String.valueOf(applid)));
      ec.addElement(f);
      ec.addElement(RecordEditPage.editRecord(db.appliance, applid, false/*write*/, null, linfo));
    }
    fillBody(ec);
  }

}