package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/StoreAuthPage.java,v $</p>
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
import net.paymate.data.sinet.business.*;
import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.lang.*;
import net.paymate.io.*;
import org.apache.ecs.html.*;
import org.apache.ecs.*;
import java.util.*;
import java.io.*;
import net.paymate.web.table.query.StoreAuthsFormat;

public class StoreAuthPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreAuthPage.class);

  public StoreAuthPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties req) {
    this(linfo, opcodeused, req, null, null, null);
  }
  public StoreAuthPage(LoginInfo linfo, AdminOpCode opcodeused, StoreAuthid saxid) {
    this(linfo, opcodeused, null, saxid, null, null);
  }
  public StoreAuthPage(LoginInfo linfo, AdminOpCode opcodeused, Storeid storeid,
                       Authid associd) {
    this(linfo, opcodeused, null, null, storeid, associd);
  }

  public StoreAuthPage(LoginInfo linfo,
                       AdminOpCode opcodeused, EasyProperties req,
                       StoreAuthid storeauthid, Storeid storeid,
                       Authid associd) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(req == null) {
      req = new EasyProperties();
    }
    if(!StoreAuthid.isValid(storeauthid)) {
      storeauthid = new StoreAuthid(req.getInt(StoreAuthsFormat.STOREAUTHID));
      if(!StoreAuthid.isValid(storeauthid)) {
//        storeauthid = usession.db.getstStoreaccess(associd, storeid);
      }
    }
    fillBody(RecordEditPage.editRecord(db.storeauth, storeauthid, false/*write*/, null, linfo));
  }

}
