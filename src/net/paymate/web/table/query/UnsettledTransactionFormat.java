package net.paymate.web.table.query;
/**
* Title:        UnsettledTransactionFormat<p>
* Description:  Listing of transactions<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: UnsettledTransactionFormat.java,v 1.132 2004/04/12 21:58:31 mattm Exp $
*/

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
import net.paymate.lang.StringX;
import net.paymate.lang.Fstring;
import net.paymate.io.NullOutputStream;

public class UnsettledTransactionFormat extends RecordFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(UnsettledTransactionFormat.class, ErrorLogStream.WARNING);

  public static PerTxnListener PTL = null; // !!! Only use this in a standalone environment !!!

  // +++ make a static function to generate a page of all of the possible "status"es, and maybe make an enumeration of them.
  // This page would describe each status and what it means.
  // There would be a link on the "status" header to the page.
  // The page would come up in a separate, smaller window.

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new UnsettledTransactionFormatEnum()).numValues()];
  static {
    theHeaders[UnsettledTransactionFormatEnum.TimeCol]       = new HeaderDef(AlignType.LEFT  , "Time");
    theHeaders[UnsettledTransactionFormatEnum.StanCol]       = new HeaderDef(AlignType.RIGHT , "Txn #");
    theHeaders[UnsettledTransactionFormatEnum.TraceCol]      = new HeaderDef(AlignType.CENTER, "Trace");
    theHeaders[UnsettledTransactionFormatEnum.StatusCol]     = new HeaderDef(AlignType.CENTER, "Status");
    theHeaders[UnsettledTransactionFormatEnum.ApprovalCol]   = new HeaderDef(AlignType.RIGHT , "Approval");
    theHeaders[UnsettledTransactionFormatEnum.PayTypeCol]    = new HeaderDef(AlignType.RIGHT , "Type");
    theHeaders[UnsettledTransactionFormatEnum.InstitutionCol]= new HeaderDef(AlignType.RIGHT , "Card");
    theHeaders[UnsettledTransactionFormatEnum.AcctNumCol]    = new HeaderDef(AlignType.RIGHT , "Acct");
    theHeaders[UnsettledTransactionFormatEnum.SaleCol]       = new HeaderDef(AlignType.RIGHT , "Sale");
    theHeaders[UnsettledTransactionFormatEnum.ReturnCol]     = new HeaderDef(AlignType.RIGHT , "Return");
    theHeaders[UnsettledTransactionFormatEnum.NetCol]        = new HeaderDef(AlignType.RIGHT , "Net");
    theHeaders[UnsettledTransactionFormatEnum.MerchRefCol]   = new HeaderDef(AlignType.RIGHT , "Ref");
    theHeaders[UnsettledTransactionFormatEnum.VoidChgCol]    = new HeaderDef(AlignType.RIGHT , "Chg");
    theHeaders[UnsettledTransactionFormatEnum.AVSCodeCol]    = new HeaderDef(AlignType.RIGHT , "AVS");
  }

  private boolean countLosses = false;
  private TxnRow txn = null;
  private SubTotaller totaller = null;
  private SubTotaller avstotaller = null;
  private Txnid [ ] skiprows = null;
  private boolean isagawd = false;
  private boolean archive = false;

  public UnsettledTransactionFormat(LoginInfo linfo, TxnRow txn,
                                    SubTotaller totaller,
                                    SubTotaller avstotaller,
                                    String title, String absoluteURL,
                                    boolean countLosses, boolean archive) {
    this(linfo, txn, totaller, avstotaller, title, absoluteURL, countLosses, null, archive);
  }
  public UnsettledTransactionFormat(LoginInfo linfo, TxnRow txn,
                                    SubTotaller totaller,
                                    SubTotaller avstotaller,
                                    String title, String absoluteURL,
                                    boolean countLosses,
                                    Txnid [ ] skiprows,
                                    boolean archive) {
    super(linfo.colors(), title, txn, absoluteURL, linfo.ltf());
    this.txn = txn;
    this.skiprows = skiprows;
    this.countLosses = countLosses;
    this.totaller = (totaller == null) ? (new SubTotaller()) : totaller;// for instances where we are not going to do a subtotal table.
    this.avstotaller = (avstotaller == null) ? (new SubTotaller()) : avstotaller;
    this.archive = archive;
    isagawd = linfo.isaGod();
    HeaderDef[] myHeaders = new HeaderDef[theHeaders.length];
    System.arraycopy(theHeaders, 0, myHeaders, 0, theHeaders.length);
    headers = myHeaders;
    dbg.ERROR("MerchRefHeader = "+linfo.store.merchreflabel);
    headers[UnsettledTransactionFormatEnum.MerchRefCol] = new HeaderDef(AlignType.RIGHT, StringX.TrivialDefault(linfo.store.merchreflabel, ""));
  }

  public static final String moneyformat = "#0.00";

  private LedgerValue saleTotal = new LedgerValue(moneyformat);
  private int  saleCount    = 0; // qty
  private LedgerValue returnTotal  = new LedgerValue(moneyformat);
  private int  returnCount  = 0; // qty
  private LedgerValue otherTotal   = new LedgerValue(moneyformat);
  private int  otherCount   = 0; // qty
  private int  count        = 0; // number of txns (qty)
  private LedgerValue nett   = new LedgerValue(moneyformat);
  private int  sumCount     = 0; // qty in net.

/**
 * these record the range of the query. Seems like a job for ... TimeRanger!
 */
  private TimeRange span=TimeRange.Create();

  private LedgerValue authamount = new LedgerValue(moneyformat); //+_+ to keep the drawer report and this report looking the same
  private LedgerValue settleamount = new LedgerValue(moneyformat); //+_+ to keep the drawer report and this report looking the same
  private LedgerValue diff = new LedgerValue(moneyformat);

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      zeroValues();
      tgr = super.nextRow();//returns either null or 'this'
      if(skiprows != null) { // check to see if we are supposed to skip this one
        while (tgr != null) {
          Txnid that = txn.txnid();
          boolean skipthis = false;
          for (int i = skiprows.length; i-- > 0; ) {
            if (skiprows[i].equals(that)) {
              skipthis = true;
            }
          }
          if (skipthis) {
            tgr = super.nextRow(); //returns either null or 'this'
          } else {
            break;
          }
        }
      }
      Fstring stanstr = new Fstring(5,'0');
      if(tgr != null) {
        // do the real data//+_+ move the following into Txn
        // +++ use the settleop instead of transfertype for these next questions?
        boolean wasReversed = txn.isVoided();
        boolean isReturn    = txn.isReturn();
        boolean isForce     = txn.isForce();
        boolean isSale      = txn.isSale() || txn.isForce() || (txn.isAuthOnly() && txn.settle());
        boolean isAuthonly  = txn.isAuthOnly() && !isSale;
        boolean isVoid      = txn.isReversal();
        boolean isModify    = txn.isModify();
//dbg.ERROR("isVOID is " + isVoid + " for txn # " + txn.txnid);
//        boolean isStoodin  = txn.wasStoodinApproved();
        boolean isStoodin  = txn.wasStoodin();
        boolean inProgress = !txn.responded();//this method ignores stoodin
        boolean isDeclined = !(ActionCode.Approved.equals(txn.actioncode)) && !inProgress;  // +++ put on TranjorRow
        boolean didTransfer= !isVoid && !isModify && !wasReversed && !isAuthonly &&
            ((!isDeclined && !inProgress) || (isStoodin && countLosses));  // +++ put on TranjorRow
        boolean isStrike   = !didTransfer;
        count++;
        UTC time = StringX.NonTrivial(txn.clientreftime) ? txn.refTime() : UTC.New(txn.transtarttime);//UTC#
        span.include(time);
        String localDTime = ltf.format(time); //UTC#
//        MSRData card=txn.card();
        stanstr.righted(txn.refNum());
        authamount.setto(txn.rawAuthAmount());// unsigned amount
        settleamount.setto(txn.rawSettleAmount());
        String status = "";
        String original = "";
        boolean shouldavs = false;
        if(StringX.NonTrivial(txn.origtxnid)) {
          String dest = archive ? ReceiptArchiver.transactionForDrawerDateOrBatch(txn.origtxnid) : net.paymate.web.page.Acct.txnUrl(txn.origtxnid);
          original = (StringX.NonTrivial(txn.origtxnid)
                                 ? "<a href=\"" + dest + "\">" + txn.origtxnid + "</a>"
                                 : "NOT FOUND");
        }
        // the order of this sequence is important !
        if(isVoid) {
          if(isDeclined) {
            status = "DECLINED VOID";
          } else {
            status = "VOID of " + original;
          }
        } else if(isModify) {
          if(isDeclined) {
            status = "DECLINED MODIFY";
          } else {
            status = "MODIFY of " + original;
          }
        } else if(wasReversed) {
          status = "VOIDED";
        } else if(isDeclined) {
          status = isStoodin ? "LOSS" : "DECLINED"; //#audit "Declined" web page only
        } else if(inProgress) {
          status = isStoodin ? "PEND/SI" : "PENDING";
        } else if(isStoodin) {
//          if(isagawd) {
            status = "SI";
//          }
        } else if(isAuthonly) {
          status = "AUTHONLY";
        } else if(isForce) {
          status = "FORCE";
        } else {
          // everything else list nothing (APPROVED); leave it as the default
        }
        // +++ switch on transfertype instead?
        // start setting columns and adding subtotals
        if(!isVoid && !isModify) {
          setColumn(UnsettledTransactionFormatEnum.StanCol, strikeText(txn.refNum(), isStrike));
          setColumn(UnsettledTransactionFormatEnum.PayTypeCol, strikeText(String.valueOf(txn.paytype().Image()), isStrike));
          setColumn(UnsettledTransactionFormatEnum.InstitutionCol, strikeText(txn.cardType(), isStrike));
          setColumn(UnsettledTransactionFormatEnum.AcctNumCol, strikeText(txn.last4(), isStrike));
          setColumn(UnsettledTransactionFormatEnum.MerchRefCol, strikeText(txn.merchref, isStrike));
          setColumn(UnsettledTransactionFormatEnum.ApprovalCol, strikeText(txn.approvalcode, isStrike));
        }
        if(isReturn) {
          setColumn(UnsettledTransactionFormatEnum.ReturnCol, strikeText(settleamount.Image(), isStrike));
          if(didTransfer) {
            returnTotal.add(settleamount);
            returnCount++;
            String key = txn.paytype+txn.institution;
            long cents = -1 * settleamount.Value();
            dbg.VERBOSE("adding to subtotaller:"+key+"="+cents);
            totaller.add(key, cents);
          }
          settleamount.changeSign(); //negative for all other uses
        } else if(isSale){
          setColumn(UnsettledTransactionFormatEnum.SaleCol, strikeText(settleamount.Image(), isStrike));
          if(didTransfer) {
            saleTotal.add(settleamount);
            saleCount++;
            String key = txn.paytype+txn.institution;
            long cents = settleamount.Value();
            dbg.VERBOSE("adding to subtotaller:"+key+"="+cents);
            totaller.add(key, cents);
            if(txn.isCredit()) {
              shouldavs = true;
            }
          }
        } else if(isAuthonly) { // only credit here!
          // don't add it into totals, though, as it doesn't count
          setColumn(UnsettledTransactionFormatEnum.SaleCol,
                    strikeText(authamount.Image(), isStrike));
          shouldavs = true;
        } else if(isModify) {
          // don't do voids here, only modifies (voids have an obvious amount)
          diff.setto(0); // clear it
          diff.plus(settleamount); // add the settleamount
          diff.subtract(authamount);// subtract the authamount for the net change
          setColumn(UnsettledTransactionFormatEnum.VoidChgCol , strikeText(isModify ? diff.Image() : "", isStrike));
        }
        if(didTransfer) {
          setColumn(UnsettledTransactionFormatEnum.NetCol, settleamount.Image());
          nett.add(settleamount);
          sumCount++;
        } else {
          otherTotal.add(settleamount);
          otherCount++;
        }
        setColumn(UnsettledTransactionFormatEnum.TimeCol  , strikeText(localDTime, isStrike));
        setColumn(UnsettledTransactionFormatEnum.StatusCol, status);
        String dest = archive ? ReceiptArchiver.transactionForDrawerDateOrBatch(txn.txnid) : net.paymate.web.page.Acct.txnUrl(txn.txnid);
        setColumn(UnsettledTransactionFormatEnum.TraceCol , new A(dest, strikeText(txn.txnid, isStrike)));
        setColumn(UnsettledTransactionFormatEnum.AVSCodeCol, strikeText(txn.avsrespcode, isStrike));
        if(shouldavs) {
          String key = txn.institution + txn.avsrespcode;
          long cents = authamount.Value();
          if(StringX.NonTrivial(txn.avsrespcode)) {
            avstotaller.add(key, cents); // should this be the auth amount or the settle amount?
            dbg.VERBOSE("adding avs for [" + key + "]=[" + cents + "] cents");
          } else {
            dbg.VERBOSE("NOT adding avs for [" + key + "]=[" + cents + "] cents");
          }
        } else {
          dbg.VERBOSE("NOT adding avs since shouldavs="+shouldavs);
        }
        if(PTL != null) {
          PTL.loadedTxn(txn);
        }
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
        case UnsettledTransactionFormatEnum.TimeCol: {
          ret = hasMore() ? "SUBTOTALS:" : "TOTALS:";
          if(count > 1) {
            ret += " <i>(" + DateX.millisToTime(span.milliSpan())+")</i>";
          }
        } break;
        case UnsettledTransactionFormatEnum.StanCol: {
          ret = Long.toString(count);
        } break;
        case UnsettledTransactionFormatEnum.ApprovalCol: {
          ret = " = " + Long.toString(count-otherCount);
        } break;
        case UnsettledTransactionFormatEnum.ReturnCol: {
          ret = "- ["+returnCount+"] " + returnTotal.Image();
        } break;
        case UnsettledTransactionFormatEnum.SaleCol: {
          ret = "["+saleCount+"] " + saleTotal.Image();
        } break;
        case UnsettledTransactionFormatEnum.NetCol: {
          ret = " = ["+sumCount+"] " + nett.Image();//compare to sale+return
        } break;
        case UnsettledTransactionFormatEnum.StatusCol: {
          ret = "- " + Long.toString(otherCount);
        } break;
      }
    } catch (Exception t) {
      dbg.Caught("footer(): column [" + col + "]: Exception generating data element; using empty element.",t);
    }
    return  new B(ret);
  }

  public static void getSubsNoDetail(TxnRow stmt, boolean countLosses, LoginInfo linfo, SubTotaller totaller, boolean archive) {
    totaller.prime(PayMateDBDispenser.getPayMateDB().getStorePayInst(linfo.store.storeId()));
    new UnsettledTransactionFormat(linfo, stmt, totaller, null, "", null, countLosses, archive).output(new NullOutputStream());
  }
}
//$Id: UnsettledTransactionFormat.java,v 1.132 2004/04/12 21:58:31 mattm Exp $
