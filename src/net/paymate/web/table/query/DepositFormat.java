package net.paymate.web.table.query;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DepositFormat.java,v 1.14 2004/03/25 08:14:40 mattm Exp $
 */

import  net.paymate.util.*;  // ErrorLogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.data.*; // batchid
import  net.paymate.database.*; // db
import  net.paymate.database.ours.query.*; // Drawer
import  net.paymate.web.page.*; // Acct
import  net.paymate.web.AdminOp;

public class DepositFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(DepositFormat.class);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new DepositFormatEnum()).numValues()];
  static { // keeping these separate makes it easier to keep their order straight // order is set in the ennum, nowhere else
    theHeaders[DepositFormatEnum.terminalNameCol]    = new HeaderDef(AlignType.LEFT , "Terminal");
    theHeaders[DepositFormatEnum.lastClosedTimeCol]  = new HeaderDef(AlignType.LEFT , "Time");
    theHeaders[DepositFormatEnum.authorizerNameCol]  = new HeaderDef(AlignType.LEFT , "Settler");
    theHeaders[DepositFormatEnum.txnCountCol]        = new HeaderDef(AlignType.RIGHT, "Count");
    theHeaders[DepositFormatEnum.txnTotalCol]        = new HeaderDef(AlignType.RIGHT, "Total"); // +++ click for details
    theHeaders[DepositFormatEnum.tadCol]             = new HeaderDef(AlignType.RIGHT, "TAD#");
    theHeaders[DepositFormatEnum.authtermCol]        = new HeaderDef(AlignType.LEFT , "Settler Term");
    theHeaders[DepositFormatEnum.CSVCol]             = new HeaderDef(AlignType.RIGHT,"Export");
  };

  private DepositRow deposit;
  public DepositFormat(DepositRow deposit, String title, LoginInfo linfo) {
    super(linfo.colors(), title, deposit, null, linfo.ltf());
    this.deposit = deposit;
    headers = theHeaders;
  }

  public static final String TAID = "taid";

  public static final String URL(TermAuthid termauthid) {
    return URL(String.valueOf(termauthid));
  }

  public static final String URL(String termauthid) {
    return Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.deposit)).Image() + "&" + TAID + "=" + termauthid;
  }

  TableGenRow tgr = null;
  private LedgerValue amount = new LedgerValue(UnsettledTransactionFormat.moneyformat);

  public TableGenRow nextRow() {
    try {
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        numrows++;
        PayMateDBDispenser.getPayMateDB().getOpenBatchPendingTotals(deposit);
        // do the real data
        setColumn(DepositFormatEnum.lastClosedTimeCol, utcdb2web(deposit.getLastBatchtime()));
        setColumn(DepositFormatEnum.authtermCol      , deposit.authtermid);
        setColumn(DepositFormatEnum.terminalNameCol  , deposit.terminalname);
        long count = deposit.apprCount();
        apprCount += count;
        setColumn(DepositFormatEnum.txnCountCol      , ""+count);
        long amt = deposit.apprAmount();
        total += amt;
        amount.setto(amt);
        setColumn(DepositFormatEnum.txnTotalCol      , new A(URL(deposit.termauthid), amount.Image())); // link to details of the deposit
        setColumn(DepositFormatEnum.authorizerNameCol, deposit.authname);
        setColumn(DepositFormatEnum.tadCol           , deposit.termauthid);
        setColumn(DepositFormatEnum.CSVCol           , Acct.TSVTermAuthLink(deposit.termauthid));
      }
    } catch (Exception t2) {
      dbg.Enter("nextRow");
      dbg.WARNING("Unknown and general exception generating next row content.");
      dbg.Exit();
    } finally {
      return (tgr == null) ? null : this;
    }
  }

  protected int footerRows() {
    return 1;
  }

  private long apprCount = 0;
  private long total = 0;
  private int numrows = 0;

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case DepositFormatEnum.terminalNameCol: {
          ret = "TOTALS:";
        } break;
        case DepositFormatEnum.authtermCol: {
          ret = String.valueOf(numrows);
        } break;
        case DepositFormatEnum.txnCountCol: {
          ret = Long.toString(apprCount);
        } break;
        case DepositFormatEnum.txnTotalCol: {
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
//$Id: DepositFormat.java,v 1.14 2004/03/25 08:14:40 mattm Exp $
