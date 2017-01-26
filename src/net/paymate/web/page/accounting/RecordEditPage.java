package net.paymate.web.page.accounting;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/page/accounting/RecordEditPage.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import net.paymate.web.page.*;
import net.paymate.web.color.*;
import net.paymate.web.*;
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
import net.paymate.web.table.query.RecordEditFormat;

public class RecordEditPage extends Acct {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(RecordEditPage.class);

  public RecordEditPage(LoginInfo linfo, AdminOpCode opcodeused, EasyProperties ezc) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(editRecord(ezc, linfo));
  }

  public RecordEditPage(LoginInfo linfo, AdminOpCode opcodeused,
                        EasyProperties ezc, TableProfile tp, UniqueId id) {
    super(linfo, opcodeused, false /*we will never create this page in archive mode*/);
    fillBody(editRecord(ezc, linfo, tp, id));
  }

  // for when we don't already know what table (etc):
  private static Element editRecord(EasyProperties ezc, LoginInfo linfo) {
    return editRecord(ezc, linfo, null, null);
  }

  // for when we do already know the table (etc)
  private static Element editRecord(EasyProperties ezc, LoginInfo linfo,
                                    TableProfile tp, UniqueId id) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    ElementContainer ec = new ElementContainer();
    if(linfo.isaGod()) {
      String erpw = ezc.getString(ERPW);
      boolean write = false;
      if (StringX.NonTrivial(erpw)) {
        write = linfo.assoc.passes(erpw);
        if (!write) {
          ec.addElement("Invalid password!  Modifications lost.");
        }
      }
      String tablename = ezc.getString(RecordEditFormat.TABLENAME);
      if(tp == null) {
        tp = db.tableProfileFromName(tablename);
        if (tp == null) {
          ec.addElement("Invalid table!");
          return ec;
        }
      }
      if(!UniqueId.isValid(id)) {
        id = new UniqueId(ezc.getString(RecordEditFormat.ID));
        if(!id.isValid()) {
          ec.addElement("Invalid id!");
          return ec;
        }
      }
      ec.addElement(editRecord(tp, id, write, ezc, linfo));
    } else {
      ec.addElement("Access denied.");
    }
    return ec;
  }

  private static final String ERPW = "erpw";
  public static ElementContainer editRecord(
      TableProfile tp, UniqueId id, boolean write, EasyProperties ezc, LoginInfo linfo) {
    ElementContainer ec = new ElementContainer();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    // show and edit the record's details!
    if(linfo.isaGod()) { // only we have the right
      if (write) {
        if ( (ezc == null) || (ezc.allKeys().size() < 1)) {
          ec.addElement("No values to set!");
        } else {
          dbg.WARNING("Setting values: \n" + ezc);
          int seti = db.setRecordProperties(tp, id, ezc);
          if(seti != 1) {
            ec.addElement("Error changing record [i="+seti+"].  Possibly nothing was changed.");
          } else {
            ec.addElement("Successfully changed record settings.");
          }
          // +++ go back to somewhere.  Where?  Where did we come from when we got here?  Need to pass that info through!
        }
      }
      String title = "" + tp.name() + " # " + id;
      EasyProperties fields = db.getRecordProperties(tp, id, tp.primaryKey.field);
      Form f = NewPostForm("./" + Acct.key() + "?" + AdminOp.editRecordAdminOp.url());
      f.addElement(new RecordEditFormat(linfo.colors(), "Edit " + title, fields, id, tp));
      TD td1 = new TD().addElement("Enter Password to save changes: ");
      TD td2 = new TD().addElement(new Input(Input.PASSWORD, ERPW, ""));
      TD td3 = new TD().addElement(
        new Input().setType(Input.SUBMIT).setValue("Save " + title + " modifications"));
      TD td4 = new TD().addElement(new Input().setType(Input.RESET).setValue("Reset"));
      TR tr1 = new TR().addElement(td1).addElement(td2).addElement(td3).addElement(td4);
      Table t = new Table().addElement(tr1);
      f.addElement(t).
        addElement(new Input(Input.HIDDEN, RecordEditFormat.TABLENAME, tp.name())).
        addElement(new Input(Input.HIDDEN, RecordEditFormat.ID, id.toString()));
      ec.addElement(f);
    }
    return ec;
  }
}