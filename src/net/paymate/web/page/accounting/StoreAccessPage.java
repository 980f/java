package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/StoreAccessPage.java,v $</p>
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
import net.paymate.data.sinet.business.*;

public class StoreAccessPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreAccessPage.class);

  public StoreAccessPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezp) {
    this(linfo, opcodeused, ezp, null, null, null);
  }
  public StoreAccessPage(LoginInfo linfo, AdminOpCode opcodeused, StoreAccessid saxid) {
    this(linfo, opcodeused, null, saxid, null, null);
  }
  public StoreAccessPage(LoginInfo linfo, AdminOpCode opcodeused, Storeid storeid,
                       Associateid associd) {
    this(linfo, opcodeused, null, null, storeid, associd);
  }

  public StoreAccessPage(LoginInfo linfo,
                       AdminOpCode opcodeused, EasyProperties req,
                       StoreAccessid saxid, Storeid storeid,
                       Associateid associd) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(req == null) {
      req = new EasyProperties();
    }
    if(!StoreAccessid.isValid(saxid)) {
      saxid = new StoreAccessid(req.getInt(StoreaccessesFormat.STOREACCESSID))    ;
      if(!StoreAccessid.isValid(saxid)) {
        saxid = db.getStoreaccess(associd, storeid);
      }
    }
    fillBody(RecordEditPage.editRecord(db.storeaccess, saxid, false/*write*/, null, linfo));
  }

}