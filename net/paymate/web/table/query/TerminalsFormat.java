package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/TerminalsFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.7 $
 */

import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Tranjour
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.awtx.*;
import  net.paymate.ISO8583.data.*;

public class TerminalsFormat extends RecordFormat {

  private static final ErrorLogStream dbg = new ErrorLogStream(TerminalsFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new TerminalsFormatEnum()).numValues()];
  static {
    theHeaders[TerminalsFormatEnum.TerminalNameCol]  = new HeaderDef(AlignType.LEFT , "Terminal");
    theHeaders[TerminalsFormatEnum.ModelCodeCol]     = new HeaderDef(AlignType.LEFT , "Model");
    theHeaders[TerminalsFormatEnum.LastCloseTimeCol] = new HeaderDef(AlignType.LEFT , "Last Closed");
    theHeaders[TerminalsFormatEnum.ApprCountCol]     = new HeaderDef(AlignType.RIGHT, "#");
    theHeaders[TerminalsFormatEnum.ApprAmountCol]    = new HeaderDef(AlignType.RIGHT, "Pending");
  }

  private TerminalPendingRow terminal = null;

  public TerminalsFormat(LoginInfo linfo, TerminalPendingRow terminal, String title, String absoluteURL, int howMany, String sessionid, PayMateDB db) {
    super(linfo.colors, title, terminal, absoluteURL, howMany, sessionid, linfo.ltf);
    this.terminal = terminal;
    headers = theHeaders;
    this.db = db;
  }

  private PayMateDB db = null; // this is so we can get the rest of the stuff.  It can't be packed into one nice query.  :(  We can fix it better when we have our own tables.

  public static final String MONEYFORMAT = "#0.00";
  private int  apprCount    = 0; // qty
  private LedgerValue approvedTtl = new LedgerValue(MONEYFORMAT);

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      LedgerValue amount = new LedgerValue(MONEYFORMAT); //+_+ to keep the drawer report and this report looking the same
      if(tgr != null) {
        db.getTerminalTotals(terminal);

        setColumn(TerminalsFormatEnum.TerminalNameCol, terminal.terminalName); // +_+ link to terminal config entry using terminal.terminalid
        setColumn(TerminalsFormatEnum.ModelCodeCol, terminal.modelCode);
        setColumn(TerminalsFormatEnum.LastCloseTimeCol, ltf.format(PayMateDB.tranUTC(terminal.lastCloseTime())));

        apprCount+=terminal.apprCount();
        approvedTtl.add(new RealMoney(terminal.apprAmount()));
        setColumn(TerminalsFormatEnum.ApprCountCol, ""+terminal.apprCount());
        amount.setto(terminal.apprAmount());//unsigned amount
        setColumn(TerminalsFormatEnum.ApprAmountCol, new A(Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.t)).Image() + "&t=" + (new TerminalID(Safe.parseInt(terminal.terminalid), Safe.parseInt(terminal.storeid))).fullname(), amount.Image()));
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

  protected int footerRows() {
    return 1;
  }

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case TerminalsFormatEnum.TerminalNameCol: {
          ret = "TOTALS:";
        } break;
        case TerminalsFormatEnum.ApprCountCol: {
          ret = Long.toString(apprCount);
        } break;
        case TerminalsFormatEnum.ApprAmountCol: {
          ret = approvedTtl.Image();
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

}


//$Id: TerminalsFormat.java,v 1.7 2001/11/17 06:17:00 mattm Exp $
