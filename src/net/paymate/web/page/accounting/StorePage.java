package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/StorePage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.11 $
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
import net.paymate.web.table.query.StoreAuthsFormat;
import net.paymate.web.table.query.StoreaccessesFormat;
import net.paymate.web.table.query.AppliancesFormat;
import net.paymate.data.sinet.business.*;
import net.paymate.data.sinet.hardware.Applianceid;

public class StorePage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StorePage.class);

  public StorePage(LoginInfo linfo, AdminOpCode opcodeused,
                   EasyProperties reqezp, String notes) {
    this(linfo, opcodeused, reqezp, null, notes);
  }
  public StorePage(LoginInfo linfo, AdminOpCode opcodeused, Storeid storeid) {
    this(linfo, opcodeused, null, storeid, null);
  }
  public StorePage(LoginInfo linfo, AdminOpCode opcodeused, Storeid storeid, String notes) {
    this(linfo, opcodeused, null, storeid, notes);
  }

  public StorePage(LoginInfo linfo, AdminOpCode opcodeused,
                   EasyProperties reqezp, Storeid storeid, String notes) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    if(StringX.NonTrivial(notes)) {
      ec.addElement(notes);
    }
    if(!Storeid.isValid(storeid)) {
      int storeIdInt = reqezp.getInt(Login.STOREID);
      storeid = new Storeid(storeIdInt);
    }
    // a button to create a new appliance for gawds
    if(linfo.isaGod()) {
      TD td1 = new TD().addElement("Enter Password to create a new appliance: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, newapplpw, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Create appliance"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.newAppliance.url()).
          addElement(t);
      ec.addElement(f);
    }
    ec.addElement(RecordEditPage.editRecord(db.store, storeid, false/*write*/, null, linfo));
    // a button to create a new storeauth for gawds
    if(linfo.isaGod()) {
      TD td1 = new TD().addElement("Enter Password to create a new storeauth: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, newstoreauthpw, ""));
      TD td3 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Create storeauth"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3);
      Table t = new Table().addElement(tr1);
      Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.newStoreauth.url()).
          addElement(t);
      f.addElement(new Input(Input.HIDDEN, Login.STOREID, String.valueOf(storeid)));
      ec.addElement(f);
    }
    // need the other misc info on the store ...
    Store store = StoreHome.Get(storeid);
    EasyProperties ezp = new EasyProperties();
    ezp.setString("lastAutoDrawer", linfo.ltf().format(store.lastAutoDrawer()));
    ezp.setString("lastAutoDeposit", linfo.ltf().format(store.lastAutoDeposit()));
    ec.addElement(new EasyCursorTableGen(
        "More Store data (in your timezone)", linfo.colors(), ezp, null, "", -1));
    // list all of the storeauths so that each can be edited separately
    ec.addElement(new StoreAuthsFormat(linfo, db.getStoreAuths(storeid), "StoreAuths", null));
    ec.addElement(new StoreaccessesFormat(linfo, db.getStoreaccesses(storeid), "StoreAccess", null));
    // list this store's appliances
    Applianceid [ ] appliances = linfo.store.appliances.getAllIds();
    ec.addElement(new AppliancesFormat(linfo, appliances, "Appliances"));
    fillBody(ec);
  }

}