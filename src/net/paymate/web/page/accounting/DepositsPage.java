package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/DepositsPage.java,v $</p>
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

public class DepositsPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DepositsPage.class);

  public DepositsPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    String DEPPW = "deppw";
    boolean canissue = linfo.permits(AssociatePermissions.PermitClose);
    if(canissue) {
      String deppw = ezp.getString(DEPPW);
      if(StringX.NonTrivial(deppw)) {
        // this means they wanted to issue a deposit
        if(linfo.assoc.passes(deppw)) {
          boolean succeeded = ConnectionServer.THE().issueDeposit(linfo.store.storeId(), false /*auto*/);
          ec.addElement(new StringElement("Deposit  " + (succeeded ? "" : "NOT ") + "issued!"));
        } else {
          String res = "Deposit not issued due to invalid password!";
          ec.addElement(res);
          dbg.WARNING(res+" linfo="+linfo);
        }
      } else {
        dbg.VERBOSE("no deposit password entered");
      }
      TD td1 = new TD().addElement("Enter Password to issue deposit: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, DEPPW, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Issue deposit"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./"+key()+"?"+AdminOp.DepositsAdminOp.url()).addElement(t);
      ec.addElement(f);
    } else {
      dbg.VERBOSE("No deposit issue provided due to insufficient priveleges! linfo="+linfo);
    }
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ec.addElement(new DepositFormat(db.getPendingTermAuths(linfo.store.storeId()), "Deposit", linfo));
    fillBody(ec);
  }

}