package net.paymate.web.table.query;

/**
* Title:        StoreFormat<p>
* Description:  The canned query for the Stores screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: StoreFormat.java,v 1.28 2004/03/06 04:42:36 mattm Exp $
*/

import  net.paymate.database.ours.query.*; // StoreInfoRow
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //HeaderDef
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
// for the enum:
import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;
// SS2
import net.paymate.data.sinet.business.*;

public class StoreFormat extends UniqueIdArrayFormat implements TableGenRow {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(StoreFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new StoreFormatEnum()).numValues()];
  static {
    theHeaders[StoreFormatEnum.NameCol]           = new HeaderDef(AlignType.LEFT, "Name");
    theHeaders[StoreFormatEnum.Address1Col]       = new HeaderDef(AlignType.LEFT, "Address1");
    theHeaders[StoreFormatEnum.Address2Col]       = new HeaderDef(AlignType.LEFT, "Address2");
    theHeaders[StoreFormatEnum.CityCol]           = new HeaderDef(AlignType.LEFT, "City");
    theHeaders[StoreFormatEnum.StateCol]          = new HeaderDef(AlignType.LEFT, "State");
    theHeaders[StoreFormatEnum.ZipcodeCol]        = new HeaderDef(AlignType.LEFT, "Zipcode");
    theHeaders[StoreFormatEnum.CountryCol]        = new HeaderDef(AlignType.LEFT, "Country");
    theHeaders[StoreFormatEnum.TimeZoneCol]       = new HeaderDef(AlignType.LEFT, "TimeZone");
    theHeaders[StoreFormatEnum.PhoneCol]          = new HeaderDef(AlignType.LEFT, "Phone");
    theHeaders[StoreFormatEnum.TypeCol]           = new HeaderDef(AlignType.LEFT, "Industry");
  }

  private boolean isagawd = false;
  private Store mystore = null;
  public StoreFormat(LoginInfo linfo, Storeid [ ] stores, String title,
                     String absoluteURL) {
    super(stores, title, linfo, absoluteURL, null /*headers*/);
    this.isagawd = linfo.isaGod();
    mystore = linfo.store;
    HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
    System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
    // to give any headers unique info or links, do it here ...
    headers = myHeaders;
  }

  public TableGenRow nextRow() {
    currentRow++;
    eid = (Storeid)ids[currentRow];
    currentE = StoreHome.Get(eid);
    ismine = currentE.equals(mystore);
    return this;
  }

  Store currentE = null;
  boolean ismine = false;
  Storeid eid = null;

  public Element column(int col) {
    Element str = null;
    String s = null;

    switch(col) {
      case StoreFormatEnum.NameCol: {
        if(isagawd) {
          str = new A(Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.store)).Image() + "&"+Login.STOREID+"=" + currentE.storeId(), currentE.storename);
        } else {
          str = new StringElement(currentE.storename);
        }
      } break;
      case StoreFormatEnum.Address1Col: {
        s = currentE.address1;
      } break;
      case StoreFormatEnum.Address2Col: {
        s = currentE.address2;
      } break;
      case StoreFormatEnum.CityCol: {
        s = currentE.city;
      } break;
      case StoreFormatEnum.StateCol: {
        s = currentE.state;
      } break;
      case StoreFormatEnum.ZipcodeCol: {
        s = currentE.zipcode;
      } break;
      case StoreFormatEnum.CountryCol: {
        s = currentE.country;
      } break;
      case StoreFormatEnum.TimeZoneCol: {
        s = currentE.timeZoneStr();
      } break;
      case StoreFormatEnum.PhoneCol: {
        s = currentE.phone;
      } break;
      case StoreFormatEnum.TypeCol: {
        s = currentE.merchanttype.Image();
      } break;
    }
    if((str == null) && (s != null)) {
      str = new StringElement(s);
    }
    return strikeText(str, !currentE.enabled);
  }

  protected int footerRows() {
    return 1;
  }

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case StoreFormatEnum.NameCol: {
          ret = "TOTALS: "+ids.length;
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }
}
