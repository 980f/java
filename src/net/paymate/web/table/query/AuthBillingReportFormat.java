package net.paymate.web.table.query;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.database.ours.query.*; // Txn
import  net.paymate.jpos.data.*; // CardNumber
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.table.*; //DBTableGen
import  net.paymate.web.page.*; // Acct
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.web.color.*; //
import net.paymate.lang.StringX;
import net.paymate.data.sinet.business.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/AuthBillingReportFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.14 $
 */

public class AuthBillingReportFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthBillingReportFormat.class, ErrorLogStream.WARNING);

/*
    * Before running the BIG report, find out which transfertype + paytype + institutions are needed for that auth:
    * For every entry in the resultset above, create a counter.
    * Then, run this query to get the authtermid's associated with the terminals.
          * SELECT termauth.terminalid, terminalname, authtermid, termauthid from termauth, appliance, terminal where termauth.terminalid = terminal.terminalid and terminal.applianceid = appliance.applianceid and appliance.storeid = [storeid]
          * EG: SELECT termauth.terminalid, terminalname, authtermid, termauthid from termauth, appliance, terminal where termauth.terminalid = terminal.terminalid and terminal.applianceid = appliance.applianceid and appliance.storeid = 5
          * Take the results from the query and build a properties list from it where terminalid is the key and the other fields are in the value somehow.
          * Also, build an "in" list of termauthid's for the next query: termauthidinlist (eg: '006', '005', '004')
    * Then, Run this batch listing query:
          * SELECT batch.termbatchnum, batch.batchtime, batch.batchid from batch where batch.termauthid in ([termauthidinlist]) and actioncode = 'A' order by batchid
          * eg: SELECT batch.termbatchnum, batch.batchtime, batch.batchid from batch where batch.termauthid in ('006', '005', '004') and actioncode = 'A'  order by batchid
          * NOTE: CANNOT ignore batches with txncount = 0, as those could still have valid txns in them ?!?
          * Skip through this list, referencing the other information when needed.
          * For every record in the list, run this query:
                * SELECT batchid, paytype, institution, transfertype, count(txnid) from txn where txn.batchid = [batchid] and authendtime > '2'  group by batchid, paytype, institution, transfertype order by batchid, paytype, institution, transfertype
                * eg: SELECT batchid, paytype, institution, transfertype , count(txnid) from txn where txn.batchid = 1379 and authendtime > '2'  group by batchid, paytype, institution, transfertype order by batchid, paytype, institution, transfertype
                * Skip through it, creating rows+columns and adding to the counters.
*/

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AuthBillingReportFormatEnum()).numValues()];
  static {
    theHeaders[AuthBillingReportFormatEnum.TerminalNameCol] = new HeaderDef(AlignType.LEFT , "TermName");
    theHeaders[AuthBillingReportFormatEnum.AuthTermidCol]   = new HeaderDef(AlignType.LEFT , "TermId"); // this is the auth's terminalid
    theHeaders[AuthBillingReportFormatEnum.BatchNumberCol]  = new HeaderDef(AlignType.RIGHT, "Batch#"); // this is NOT OUR batch number, but the one for the terminal that is in the batch record
    theHeaders[AuthBillingReportFormatEnum.BatchTimeCol]    = new HeaderDef(AlignType.LEFT , "Time"); // from the batch record
    theHeaders[AuthBillingReportFormatEnum.AuthrespmsgCol]  = new HeaderDef(AlignType.LEFT , "Resp"); // from the batch record
    theHeaders[AuthBillingReportFormatEnum.TotalCol]        = new HeaderDef(AlignType.RIGHT, "Total");
  }

  public static final String SETTLEMENTKEY = "Settlements";

  private TermBatchReportRow txn = null;
  private SubTotaller grandtotaller = null;
  private Authid authid = null;
  private Storeid storeid = null;
  private TimeRange daterange = null;
  private TermBatchReportTermInfoList termInfoList = new TermBatchReportTermInfoList();

  public AuthBillingReportFormat(ColorScheme colors, SubTotaller grandtotaller,
                                 String title, LocalTimeFormat ltf, Authid authid,
                                 Storeid storeid, TimeRange daterange) {
    super(colors, title, null, "", ltf);
    this.authid = authid;
    this.storeid = storeid;
    this.daterange = daterange;
    this.grandtotaller = grandtotaller; // fill this so the caller can use it in the footer of the page (but not in this table)
    prep();
  }

  /**
   * +++ move some of this stuff to PayMateDB?
   * +++ need classes that can be used to translate between PayMateDB and here for TransferType, PayType, Institution, a wrapper for all three, etc.
   */
  private void prep() {
    try {
      PayMateDB db = PayMateDBDispenser.getPayMateDB();
      // prepare the columns!
      TextList tl = db.getUsedTtPtIn(authid, storeid, daterange);
      grandtotaller.prime(tl);
      // build the terminals list:
      tl.clear();
      db.getTermsInfoForStores(storeid, authid, termInfoList, tl);
      // now, use tl as the input for the inlist
      if(tl.size() > 0) {
        txn = db.getTermBatchReport(tl, daterange);
      }
      // setup the headers
      HeaderDef[] myHeaders = new HeaderDef[theHeaders.length + grandtotaller.subtotalNames().size()];
      System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length-1);
      myHeaders[myHeaders.length-1] = theHeaders[theHeaders.length-1];
      // make the headers for the subtotals
      tl = grandtotaller.subtotalNames().sort();
      PayInfo pi = new PayInfo();
      for(int i = 0; i < tl.size(); i++) {
        pi.clear();
        pi.parse(tl.itemAt(i));
        myHeaders[theHeaders.length-1 + i] = new HeaderDef(AlignType.RIGHT , pi.tt + BRLF + pi.pt + BRLF + pi.in);
      }
      myHeaders[AuthBillingReportFormatEnum.BatchTimeCol].title = new StringElement("Time (" + ltf.getZone().getID() + ")");
      headers = myHeaders;
dbg.ERROR("headers.length = " + headers.length + ", myHeaders.length = " + myHeaders.length + ", theHeaders.length = " + theHeaders.length);
      // set the Query object
      q = txn;
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  Counter settlements = new Counter();

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      SubTotaller totaller = new SubTotaller();
      totaller.prime(grandtotaller.subtotalNames());
      tgr = super.nextRow();//returns either null or 'this' // +++ and do this again for every record you want.  Exit when you get a new batchid
      zeroValues();
      if(tgr != null) {
        settlements.incr();
        // clear everything and start the totals all over again
        // find the terminalinfo:
        Terminalid terminalid = txn.terminalid();
        TermBatchReportTermInfo terminfo = termInfoList.find(terminalid);
        if(terminfo != null) {
          setColumn(AuthBillingReportFormatEnum.TerminalNameCol, terminfo.terminalname);
          setColumn(AuthBillingReportFormatEnum.AuthTermidCol, terminfo.authtermid);
        }
        setColumn(AuthBillingReportFormatEnum.BatchNumberCol, txn.termbatchnum);
        setColumn(AuthBillingReportFormatEnum.AuthrespmsgCol, txn.authrespmsg);
        UTC time = UTC.New(txn.batchtime);
        String localDTime = ltf.format(time); //UTC#
        setColumn(AuthBillingReportFormatEnum.BatchTimeCol, localDTime);
        PayMateDBDispenser.getPayMateDB().getBatchTxnCounts(txn.batchid(), totaller);
        // 3 shove the totals into the grandtotaller
        grandtotaller.add(totaller);
        // 4 output those totals to the appropriate columns
        TextList tl = totaller.subtotalNames().sort();
        for(int i = 0; i < tl.size(); i++) {
          Accumulator acc = totaller.getAccumulator(tl.itemAt(i));
          setColumn(theHeaders.length-1 + i, String.valueOf(acc.getTotal()));
        }
        setColumn(headers.length-1, String.valueOf(totaller.Total())); // last one is the total for the row
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
    String ret = " ";
    // All of these come from the grandtotaller
    try {
      switch(col) {
        case AuthBillingReportFormatEnum.AuthTermidCol: {
          ret = "Total ["+settlements.value()+"]:";
        } break;
        case AuthBillingReportFormatEnum.BatchNumberCol: {
        } break;
        case AuthBillingReportFormatEnum.TerminalNameCol: {
        } break;
        case AuthBillingReportFormatEnum.BatchTimeCol: {
        } break;
        case AuthBillingReportFormatEnum.AuthrespmsgCol: {
        } break;
        default: {
          if(col == headers.length-1) {
            ret = String.valueOf(grandtotaller.Total()); // last one is the total for the row
            grandtotaller.add(SETTLEMENTKEY, settlements.value()); // put this in the grand totaller so that it can be reported in the billing section !!!
          } else {
            TextList tl = grandtotaller.subtotalNames().sort();
            Accumulator acc = grandtotaller.getAccumulator(tl.itemAt(col - theHeaders.length + 1));
            ret = String.valueOf(acc.getTotal());
          }
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }
}
//$Id: AuthBillingReportFormat.java,v 1.14 2003/10/30 21:05:17 mattm Exp $
