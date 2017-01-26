package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/TerminalsFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.29 $
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

public class TerminalsFormat extends RecordFormat {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TerminalsFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new TerminalsFormatEnum()).numValues()];
  static {
    theHeaders[TerminalsFormatEnum.TerminalNameCol]  = new HeaderDef(AlignType.LEFT , "Terminal");
    theHeaders[TerminalsFormatEnum.ModelCodeCol]     = new HeaderDef(AlignType.LEFT , "Model");
    theHeaders[TerminalsFormatEnum.LastCloseTimeCol] = new HeaderDef(AlignType.LEFT , "Last Closed");
    theHeaders[TerminalsFormatEnum.LastTxnTimeCol]   = new HeaderDef(AlignType.LEFT , "Last Txn");
    theHeaders[TerminalsFormatEnum.ApprCountCol]     = new HeaderDef(AlignType.RIGHT, "#");
    theHeaders[TerminalsFormatEnum.ApprAmountCol]    = new HeaderDef(AlignType.RIGHT, "Pending");
    theHeaders[TerminalsFormatEnum.ApplianceCol]     = new HeaderDef(AlignType.LEFT , "Appliance");
    theHeaders[TerminalsFormatEnum.CSVCol]           = new HeaderDef(AlignType.RIGHT,"Export");
  }

  private TerminalPendingRow terminal = null;
  private boolean isagawd = false;
  public TerminalsFormat(LoginInfo linfo, TerminalPendingRow terminal,
                         String title, String absoluteURL) {
    super(linfo.colors(), title, terminal, absoluteURL, linfo.ltf());
    this.terminal = terminal;
    headers = theHeaders;
    isagawd = linfo.isaGod();
  }

  private long apprCount    = 0; // qty
  private long total = 0;
  private int termcount = 0;
  private LedgerValue amount = new LedgerValue(UnsettledTransactionFormat.moneyformat); //+_+ to keep the drawer report and this report looking the same
  public static final String TERMID = "t";

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      if(tgr != null) {
        termcount++;
        PayMateDBDispenser.getPayMateDB().getTerminalPendingTotals(terminal);
        Element nameL = null;
        Element appl = null;
        String applurl = Acct.key() + "?adm=" +
            (new AdminOpCode(AdminOpCode.appliance)).Image() +
            "&"+AppliancesFormat.APPLID+"=" + terminal.applianceid;
        if(isagawd) {
          String configurl = Acct.key() + "?" + AdminOp.t1pg.url() + "&" +
              TERMID + "=" + terminal.terminalid;
          nameL = new A(configurl, terminal.terminalName);
        } else {
          nameL = new StringElement(terminal.terminalName);
        }
        appl = new A(applurl, terminal.applianceid);
        setColumn(TerminalsFormatEnum.TerminalNameCol, nameL);
        setColumn(TerminalsFormatEnum.ModelCodeCol, terminal.modelCode);
dbg.ERROR("terminal.lastCloseTime="+terminal.lastCloseTime()+", terminal.lastTxnTime="+terminal.lastTxnTime());
        setColumn(TerminalsFormatEnum.LastCloseTimeCol, utcdb2web(terminal.lastCloseTime()));
        setColumn(TerminalsFormatEnum.LastTxnTimeCol  , utcdb2web(terminal.lastTxnTime()));
        apprCount+=terminal.apprCount();
        total    +=terminal.apprAmount();
        setColumn(TerminalsFormatEnum.ApprCountCol, ""+terminal.apprCount());
        amount.setto(terminal.apprAmount());//unsigned amount
        setColumn(TerminalsFormatEnum.ApprAmountCol, new A(Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.drawer)).Image() + "&t=" + terminal.terminalid, amount.Image()));
        setColumn(TerminalsFormatEnum.ApplianceCol, appl);
        setColumn(TerminalsFormatEnum.CSVCol, Acct.TSVTerminalLink(terminal.terminalid));
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
        case TerminalsFormatEnum.ModelCodeCol: {
          ret = String.valueOf(termcount);
        } break;
        case TerminalsFormatEnum.ApprCountCol: {
          ret = Long.toString(apprCount);
        } break;
        case TerminalsFormatEnum.ApprAmountCol: {
          amount.setto(total);
          ret = amount.Image();
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

}


//$Id: TerminalsFormat.java,v 1.29 2004/03/25 08:14:41 mattm Exp $
