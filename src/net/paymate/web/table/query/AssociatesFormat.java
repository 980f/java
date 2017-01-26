package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/AssociatesFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import  net.paymate.web.table.*;
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*;
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.page.*; // Acct
import  net.paymate.connection.*;
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import net.paymate.lang.*;
import net.paymate.data.sinet.business.*;
import net.paymate.data.UniqueId;

public class AssociatesFormat extends UniqueIdArrayFormat implements TableGenRow {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AssociatesFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders  = new HeaderDef[(new AssociatesFormatEnum()).numValues()];
  protected static final HeaderDef[] slimHeaders = new HeaderDef[3];
  static { // order is in the .Enum file, but name,loginname, and enterpriseacl whould always be the first 3
    theHeaders[AssociatesFormatEnum.LoginnameCol]     = new HeaderDef(AlignType.LEFT, "LoginName");
    theHeaders[AssociatesFormatEnum.NameCol]          = new HeaderDef(AlignType.LEFT, "Associate Name");
    theHeaders[AssociatesFormatEnum.EnterpriseACLCol] = new HeaderDef(AlignType.LEFT, "Web");
    theHeaders[AssociatesFormatEnum.AssociateidCol]   = new HeaderDef(AlignType.LEFT, "AssocID");
    theHeaders[AssociatesFormatEnum.ColorschemeidCol] = new HeaderDef(AlignType.LEFT, "ColorScheme");
    theHeaders[AssociatesFormatEnum.EnabledCol]       = new HeaderDef(AlignType.LEFT, "Enabled");
    theHeaders[AssociatesFormatEnum.EnauthmsgviewCol] = new HeaderDef(AlignType.LEFT, "EnAuthMsgView");
    theHeaders[AssociatesFormatEnum.EncodedpwCol]     = new HeaderDef(AlignType.LEFT, "Password");
    theHeaders[AssociatesFormatEnum.EndbCol]          = new HeaderDef(AlignType.LEFT, "Gawd");

    slimHeaders[AssociatesFormatEnum.NameCol]          = theHeaders[AssociatesFormatEnum.NameCol];
    slimHeaders[AssociatesFormatEnum.LoginnameCol]     = theHeaders[AssociatesFormatEnum.LoginnameCol];
    slimHeaders[AssociatesFormatEnum.EnterpriseACLCol] = theHeaders[AssociatesFormatEnum.EnterpriseACLCol];
  }

  private boolean isagawd = false;
  public AssociatesFormat(LoginInfo linfo, Associateid [ ] ids, String title) {
    super(ids, title, linfo, null, null);
    isagawd = linfo.isaGod();
    headers = isagawd ? theHeaders : slimHeaders;
  }

  private Associate associate = null;

  public TableGenRow nextRow() {
    currentRow++;
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    associate = AssociateHome.Get((Associateid)ids[currentRow]);
    return this;
  }

  public Element column(int col) {
    Element str = null;
    switch(col) {
      case AssociatesFormatEnum.NameCol: {
        String name = associate.lastCommaFirstMiddle();
        str = isagawd
            ? (Element)new A(Acct.key() + "?" +
                             AdminOp.associateAdminOp.url() + "&" +
                             Associate.ASSOCIATEID + "=" + associate.associateid(), name)
            : (Element)new StringElement(name);
      } break;
      case AssociatesFormatEnum.LoginnameCol: {
        str = new StringElement(associate.loginname);
      } break;
      case AssociatesFormatEnum.EnterpriseACLCol: {
        str = is(associate.eperms.canWeb);
      } break;
    }
    if(isagawd && (str == null)) {
      // the rest are for gawds only
      switch(col) {
        case AssociatesFormatEnum.AssociateidCol: {
          str = new StringElement(associate.associateid().toString());
        } break;
        case AssociatesFormatEnum.ColorschemeidCol: {
          str = new StringElement(associate.colorschemeid);
        } break;
        case AssociatesFormatEnum.EnabledCol: {
          str = is(associate.enabled);
        } break;
        case AssociatesFormatEnum.EnauthmsgviewCol: {
          str = is(associate.eperms.canViewAuthMsgs);
        } break;
        case AssociatesFormatEnum.EncodedpwCol: {
          str = new StringElement(associate.encodedpw);
        } break;
        case AssociatesFormatEnum.EndbCol: {
          str = is(associate.eperms.canDB);
        } break;
      }
    }
    return strikeText(str, !associate.enabled);
  }

  private Element is(boolean it) {
    return new StringElement(it ? "Y" : "");
  }
}


//$Id: AssociatesFormat.java,v 1.13 2003/11/08 07:55:37 mattm Exp $
