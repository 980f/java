package net.paymate.web.table.query;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/table/query/EnterprisesFormat.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.17 $
 */

import net.paymate.util.ErrorLogStream;
import net.paymate.web.table.*;
import net.paymate.web.*;
import net.paymate.web.page.Login;
import net.paymate.web.page.Acct;
import org.apache.ecs.html.*; // various html elements
import org.apache.ecs.*; // various elements
import net.paymate.lang.StringX;
import net.paymate.data.sinet.business.*;
import net.paymate.data.UniqueId;
import java.util.Enumeration;

public class EnterprisesFormat extends UniqueIdArrayFormat implements TableGenRow {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(EnterprisesFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new EnterprisesFormatEnum()).numValues()];
  static {
    theHeaders[EnterprisesFormatEnum.EnterpriseNameCol] = new HeaderDef(AlignType.LEFT , "Enterprise");
    theHeaders[EnterprisesFormatEnum.StoreNameCol]      = new HeaderDef(AlignType.LEFT , "Store");
    theHeaders[EnterprisesFormatEnum.StorePhoneCol]     = new HeaderDef(AlignType.RIGHT, "Store Phone");
    theHeaders[EnterprisesFormatEnum.EditCol]           = new HeaderDef(AlignType.RIGHT, "Switch To");
  }

  private Enterpriseid myenterprise = null;
  public EnterprisesFormat(LoginInfo linfo, UniqueId [ ] enterprises, String title, String absoluteURL) {
    super(enterprises, title, linfo, absoluteURL, theHeaders);
    myenterprise = linfo.enterprise.enterpriseid();
  }

  public TableGenRow nextRow() {
    currentRow++;
    eid = (Enterpriseid)ids[currentRow];
    currentE = EnterpriseHome.Get(eid);
    ismine = eid.equals(myenterprise);
    Enumeration ennum = currentE.stores.entities();
    store = null;
    if(ennum.hasMoreElements()) {
      try {
        store = (Store) ennum.nextElement();
      } catch (Exception ex) {
        dbg.Caught(ex); // this means there was no store for this enterprise!  Bad!
      }
    } else {
      // ????
    }
    return this;
  }

  Enterprise currentE = null;
  boolean ismine = false;
  Enterpriseid eid = null;
  Store store = null;

  public Element column(int col) {
    Element str = null;
    switch(col) {
      case EnterprisesFormatEnum.EnterpriseNameCol: {
        str = ismine
            ? (Element) new A(editurlPrefix + eid, currentE.enterprisename)
            : new StringElement(currentE.enterprisename);
      } break;
      case EnterprisesFormatEnum.StoreNameCol: {
        str = new StringElement((store != null) ? store.storename : "");
      } break;
      case EnterprisesFormatEnum.StorePhoneCol: {
        str = new StringElement((store != null) ? store.phone : "");
      } break;
      case EnterprisesFormatEnum.EditCol: {
        str = ismine
            ? (Element) new StringElement("current")
            : new A(switchurlPrefix + eid, "switch to");
      } break;
    }
    return strikeText(str, !currentE.enabled);
  }

  private static String switchurlPrefix = Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.changeEnterprise)).Image() + "&"+Login.ENTID+"=";
  private static String editurlPrefix   = Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.enterprise)).Image()       + "&"+Login.ENTID+"=";

  protected int footerRows() {
    return 1;
  }

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case EnterprisesFormatEnum.EnterpriseNameCol: {
          ret = "TOTALS: "+ids.length;
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }


}
