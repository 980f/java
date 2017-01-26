package net.paymate.web.table.query;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: BatchesFormat.java,v 1.19 2004/04/08 09:09:55 mattm Exp $
 */

import  net.paymate.util.*;  // ErrorLogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.data.*; // batchid
import  net.paymate.database.*; // db
import  net.paymate.data.sinet.business.*;
import  net.paymate.database.ours.query.*; // Drawer
import  net.paymate.web.page.*; // Acct
import  net.paymate.web.AdminOp;
import net.paymate.lang.StringX;

public class BatchesFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(BatchesFormat.class);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new BatchesFormatEnum()).numValues()];
  static { // keeping these separate makes it easier to keep their order straight // order is set in the ennum, nowhere else
    theHeaders[BatchesFormatEnum.TimeCol]        = new HeaderDef(AlignType.LEFT , "Time"); // put a link on the contents, using the ID column
    theHeaders[BatchesFormatEnum.TermCol]        = new HeaderDef(AlignType.LEFT , "Terminal");
    theHeaders[BatchesFormatEnum.AuthCol]        = new HeaderDef(AlignType.LEFT , "Authorizer");
    theHeaders[BatchesFormatEnum.CountCol]       = new HeaderDef(AlignType.RIGHT, "Count");
    theHeaders[BatchesFormatEnum.AmountCol]      = new HeaderDef(AlignType.RIGHT, "Total");
    theHeaders[BatchesFormatEnum.AuthrespmsgCol] = new HeaderDef(AlignType.LEFT , "Message");
    theHeaders[BatchesFormatEnum.BatchseqCol]    = new HeaderDef(AlignType.RIGHT, "Seq#");
    theHeaders[BatchesFormatEnum.TermbatchnumCol]= new HeaderDef(AlignType.RIGHT, "Terminal Batch#");
    theHeaders[BatchesFormatEnum.CSVCol]         = new HeaderDef(AlignType.RIGHT, "Export");
  }

  private BatchesRow batches = null;
  private boolean archive = false;
  public BatchesFormat(BatchesRow batches, String title, LoginInfo linfo, SubTotaller totaller, boolean archive) {
    super(linfo.colors(), title, batches, null, linfo.ltf());
    this.batches = batches;
    headers = theHeaders;
    // new stuff
    this.linfo = linfo;
    this.totaller = totaller;
    this.archive = archive;
  }

  // new stuff
  private SubTotaller totaller = null;
  private LoginInfo linfo = null;

  public static final String BID = "bid";

  public static final String URL(Batchid batchid) {
    return URL(""+batchid);
  }

  public static final String URL(String batchid) {
    return URL(batchid, false);
  }

  public static final String URL(String batchid, boolean archive) {
    return archive
        ? ReceiptArchiver.batchForBatches(batchid)
        : Acct.key() + "?adm=" + (new AdminOpCode(AdminOpCode.batch)).Image() + "&" + BID + "=" + batchid;
  }

  TableGenRow tgr = null;
  private LedgerValue amount = new LedgerValue(UnsettledTransactionFormat.moneyformat); //+_+ to keep the drawer report and this report looking the same

  public TableGenRow nextRow() {
    try {
      zeroValues();
      tgr = super.nextRow();
      if(tgr != null) {
        boolean strike = batches.failed();
        // do the real data
        setColumn(BatchesFormatEnum.TimeCol        , strikeText(ltf.format(PayMateDBQueryString.tranUTC(batches.batchtime)), strike)); // link via drawer.drawerid//utc#
        setColumn(BatchesFormatEnum.TermCol        , strikeText(batches.terminalname, strike)); // +++ eventually link to terminal via terminalid
        setColumn(BatchesFormatEnum.CountCol       , strikeText(batches.txncount, strike));
        long amt = StringX.parseLong(batches.txntotal);
        if(!strike) {
          long count = StringX.parseLong(batches.txncount);
          apprCount += count;
          total += amt;
          goodBatches++;
        }
        amount.setto(amt);
        setColumn(BatchesFormatEnum.AmountCol      , new A(URL(batches.batchid, archive), strikeText(amount.Image(), strike)));
        setColumn(BatchesFormatEnum.AuthCol        , strikeText(batches.authname, strike)); // +++ for gawds, link to auth eventually
        setColumn(BatchesFormatEnum.AuthrespmsgCol , strikeText(batches.authrespmsg, strike));
        setColumn(BatchesFormatEnum.BatchseqCol    , strikeText(batches.batchseq, strike));
        setColumn(BatchesFormatEnum.TermbatchnumCol, strikeText(batches.termbatchnum, strike));
        if(archive) {
          // don't put anything in this column
        } else {
          setColumn(BatchesFormatEnum.CSVCol, Acct.TSVBatchLink(batches.batchid));
        }
        if(linfo.store.enlistsummary && ! archive) {
          UnsettledTransactionFormat.getSubsNoDetail(
              PayMateDBDispenser.getPayMateDB().getBatch(new Batchid(batches.batchid)),
              false /*countLosses*/, linfo, totaller, archive); // new stuff
        }
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
  private int goodBatches = 0;

  protected Element footer(int row, int col) {
    String ret = "";
    try {
      switch(col) {
        case BatchesFormatEnum.TimeCol: {
          ret = "TOTALS:";
        } break;
        case BatchesFormatEnum.AuthrespmsgCol: {
          ret = String.valueOf(goodBatches);
        } break;
        case BatchesFormatEnum.CountCol: {
          ret = Long.toString(apprCount);
        } break;
        case BatchesFormatEnum.AmountCol: {
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
//$Id: BatchesFormat.java,v 1.19 2004/04/08 09:09:55 mattm Exp $
