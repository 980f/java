package net.paymate.web.table.query;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DrawerClosingFormat.java,v 1.12 2001/11/16 01:34:33 mattm Exp $
 */

import  net.paymate.util.*;  // ErrorLogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Drawer
import  net.paymate.web.page.*; // Acct
import  net.paymate.web.AdminOp;

public class DrawerClosingFormat extends RecordFormat implements DBConstants {
  private static final ErrorLogStream dbg = new ErrorLogStream(DrawerClosingFormat.class.getName());

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new DrawerClosingFormatEnum()).numValues()];
  static { // keeping these separate makes it easier to keep their order straight
    theHeaders[DrawerClosingFormatEnum.TimeCol]      = new HeaderDef(AlignType.LEFT, ""); // put a link on the contents, using the ID column
    theHeaders[DrawerClosingFormatEnum.StoreCol]     = new HeaderDef(AlignType.LEFT, "");
    theHeaders[DrawerClosingFormatEnum.TermCol]      = new HeaderDef(AlignType.LEFT, "");
    theHeaders[DrawerClosingFormatEnum.AssociateCol] = new HeaderDef(AlignType.LEFT, "");
//    theHeaders[DrawerClosingFormatEnum.SelectCol]    = new HeaderDef(AlignType.CENTER, "Report");
  }

  private DrawerRow drawer = null;
  public DrawerClosingFormat(LoginInfo linfo, DrawerRow drawer, String title, String absoluteURL, int howMany, String sessionid) {
    super(linfo.colors, title, drawer, absoluteURL, howMany, sessionid, linfo.ltf);
    this.drawer = drawer;
    HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
    System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
    myHeaders[DrawerClosingFormatEnum.TimeCol].title      = new StringElement("Times (" + linfo.ltf.getZone().getID() + ")");
    myHeaders[DrawerClosingFormatEnum.AssociateCol].title = headerLink(new AdminOp("Associate" ,new AdminOpCode(AdminOpCode.e)));
//    myHeaders[DrawerClosingFormatEnum.SelectCol].title    = headerLink("javascript:submit_dr()", "Report");  // +++ handle multiple selections. for now ...
    myHeaders[DrawerClosingFormatEnum.StoreCol].title     = headerLink(new AdminOp("Store"     ,new AdminOpCode(AdminOpCode.s)));
    myHeaders[DrawerClosingFormatEnum.TermCol].title      = headerLink(new AdminOp("Terminal"  ,new AdminOpCode(AdminOpCode.m)));
    headers = myHeaders;

  }

  public static final String BMID = "bmid";

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        // do the real data
        setColumn(DrawerClosingFormatEnum.TimeCol,
          new A(Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.c1)).Image() + "&" + BMID + "=" + new StringElement(drawer.drawerid),
          ltf.format(PayMateDB.tranUTC(drawer.transtarttime)))); // link via drawer.drawerid//utc#
        setColumn(DrawerClosingFormatEnum.StoreCol    , new StringElement(drawer.storeName));
        setColumn(DrawerClosingFormatEnum.TermCol     , new StringElement(drawer.terminalName));
        setColumn(DrawerClosingFormatEnum.AssociateCol, new StringElement(drawer.associateName));
//        setColumn(DrawerClosingFormatEnum.SelectCol   , new Input(Input.CHECKBOX, BMID, drawer.drawerid));
      }
    } catch (Exception t2) {
      dbg.Enter("nextRow");
      dbg.WARNING("Unknown and general exception generating next row content.");
      dbg.Exit();
    } finally {
      return (tgr == null) ? null : this;
    }
  }

}
//$Id: DrawerClosingFormat.java,v 1.12 2001/11/16 01:34:33 mattm Exp $
