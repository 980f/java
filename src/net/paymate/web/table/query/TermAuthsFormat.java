package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/TermAuthsFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 */

import  net.paymate.database.*; // db
import  net.paymate.data.*; // id's
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Txn
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.awtx.*;

public class TermAuthsFormat extends RecordFormat {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TermAuthsFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new TermAuthsFormatEnum()).numValues()];
  static {
    theHeaders[TermAuthsFormatEnum.AuthidCol]      = new HeaderDef(AlignType.RIGHT, "Authid");
    theHeaders[TermAuthsFormatEnum.AuthSeqCol]     = new HeaderDef(AlignType.RIGHT, "AuthSeq");
    theHeaders[TermAuthsFormatEnum.AuthTermidCol]  = new HeaderDef(AlignType.LEFT , "AuthTermid");
    theHeaders[TermAuthsFormatEnum.TermAuthidCol]  = new HeaderDef(AlignType.RIGHT, "Termauthid");
    theHeaders[TermAuthsFormatEnum.TermBatchnumCol]= new HeaderDef(AlignType.RIGHT, "TermBatchnum");
    theHeaders[TermAuthsFormatEnum.TerminalidCol]  = new HeaderDef(AlignType.RIGHT, "Terminalid");
  }

  private TermauthRow termauth = null;
  private boolean isagawd = false;
  public TermAuthsFormat(LoginInfo linfo, TermauthRow termauth, String title,
                         String absoluteURL) {
    super(linfo.colors(), title, termauth, absoluteURL, linfo.ltf());
    this.termauth = termauth;
    headers = theHeaders;
    isagawd = linfo.isaGod();
  }

  public static final String TERMAUTHID = "taid";

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      if(tgr != null) {
        String editurl = Acct.key() + "?adm=" +
            (new AdminOpCode(AdminOpCode.termauth)).Image() +
            "&"+TERMAUTHID+"=" + termauth.termauthid;
        setColumn(TermAuthsFormatEnum.TermAuthidCol, new A(editurl, termauth.termauthid));
        setColumn(TermAuthsFormatEnum.AuthidCol, termauth.authid);
        setColumn(TermAuthsFormatEnum.AuthSeqCol, termauth.authseq);
        setColumn(TermAuthsFormatEnum.AuthTermidCol, termauth.authtermid);
        setColumn(TermAuthsFormatEnum.TermBatchnumCol, termauth.termbatchnum);
        setColumn(TermAuthsFormatEnum.TerminalidCol, termauth.terminalid);
      } else {
        dbg.WARNING("RecordFormat.next() returned null!");
      }
    } catch (Exception t2) {
      dbg.Caught("generating next row content",t2);
    } finally {
      dbg.Exit();
      return (tgr == null) ? null : this;
    }
  }
}


//$Id: TermAuthsFormat.java,v 1.4 2003/10/30 21:05:18 mattm Exp $
