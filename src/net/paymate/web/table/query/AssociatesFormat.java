package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/AssociatesFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import  net.paymate.web.table.*;
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*;
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.page.*; // Acct
import  net.paymate.connection.*;
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements

public class AssociatesFormat extends RecordFormat {
  private static final ErrorLogStream dbg = new ErrorLogStream(AssociatesFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AssociatesFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[AssociatesFormatEnum.LoginnameCol]   = new HeaderDef(AlignType.LEFT, "LoginName");
    theHeaders[AssociatesFormatEnum.LastNameCol]      = new HeaderDef(AlignType.LEFT, "Last Name");
    theHeaders[AssociatesFormatEnum.FirstNameCol]     = new HeaderDef(AlignType.LEFT, "First Name");
    theHeaders[AssociatesFormatEnum.MICol]            = new HeaderDef(AlignType.LEFT, "MI");
    theHeaders[AssociatesFormatEnum.EnterpriseACLCol] = new HeaderDef(AlignType.LEFT, "EnterpriseACL");
    theHeaders[AssociatesFormatEnum.ColorSchemeCol]   = new HeaderDef(AlignType.LEFT, "ColorScheme");
  }

  private AssociateRow row = null;
  public AssociatesFormat(LoginInfo linfo, AssociateRow row, String title, String absoluteURL, int howMany, String sessionid) {
    super(linfo.colors, title, row, absoluteURL, howMany, sessionid, linfo.ltf);
    headers = theHeaders;
    this.row = row;
  }

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        // do the real data
        setColumn(AssociatesFormatEnum.LoginnameCol, row.loginname);
        setColumn(AssociatesFormatEnum.LastNameCol, row.lastname);
        setColumn(AssociatesFormatEnum.FirstNameCol, row.firstname);
        setColumn(AssociatesFormatEnum.MICol, row.middleinitial);
        setColumn(AssociatesFormatEnum.EnterpriseACLCol, row.enterpriseacl);
        setColumn(AssociatesFormatEnum.ColorSchemeCol, row.colorschemeid);
      } else {
        dbg.WARNING("RecordFormat.next() returned null!");
      }
    } catch (Exception t2) {
      dbg.WARNING("Unknown and general exception generating next row content.");
    } finally {
      dbg.Exit();
      return (tgr == null) ? null : this;
    }
  }
}


//$Id: AssociatesFormat.java,v 1.3 2001/11/16 01:34:33 mattm Exp $
