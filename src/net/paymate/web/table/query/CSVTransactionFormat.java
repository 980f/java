package net.paymate.web.table.query;
/**
* Title:        CSVTransactionFormat<p>
* Description:  The canned query for the Unsettled Transactions screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: CSVTransactionFormat.java,v 1.6 2004/03/03 23:11:25 mattm Exp $
*/

import net.paymate.data.*; // ActionCode
import net.paymate.database.PayMateDB;
import net.paymate.database.ours.query.*; // Txn
import net.paymate.util.*; // ErrorlogStream
import net.paymate.lang.StringX;
import net.paymate.data.sinet.business.*;

public class CSVTransactionFormat {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(CSVTransactionFormat.class, ErrorLogStream.WARNING);

  private boolean countLosses = false;
  private TxnRow txn = null;
  private String merchreflabel = "";
  private String [ ] runCols = null;
  private LocalTimeFormat ltf = null;

  public CSVTransactionFormat(String merchreflabel, TxnRow txn,
                              boolean countLosses, LocalTimeFormat ltf) {
    this.txn = txn;
    this.countLosses = countLosses;
    this.ltf = ltf;
    this.merchreflabel = StringX.TrivialDefault(merchreflabel, "");
    runCols = new String [CSVTransactionFormatEnum.Prop.numValues()];
    clearFields();
  }
  String moneyformat = UnsettledTransactionFormat.moneyformat;

  private byte [ ] byter = null;
  public synchronized byte [ ] getBytes() {
    if(byter == null) {
      byter = getString().getBytes();
    }
    return byter;
  }

  private StringBuffer sb = null;
  public synchronized String getString() {
    if(sb == null) {
      sb = new StringBuffer(10000);
      fillBuffer(sb);
    }
    return sb.toString();
  }

  private synchronized void fillBuffer(StringBuffer sbL) {
    if(txn == null) {
      // +++ bitch
    } else {
      // headers
      clearFields();
      for(int i = runCols.length; i-->0;){
        String label = "";
        switch(i) {
          case CSVTransactionFormatEnum.MerchRefCol: {
            label = merchreflabel;
          } break;
          case CSVTransactionFormatEnum.AcctNumCol : {
            label = "Acct";
          } break;
          case CSVTransactionFormatEnum.ApprovalCol : {
            label = "Approval";
          } break;
          case CSVTransactionFormatEnum.AssociateCol : {
            label = "Associate";
          } break;
          case CSVTransactionFormatEnum.InstitutionCol : {
            label = "Card";
          } break;
          case CSVTransactionFormatEnum.NetCol : {
            label = "Net";
          } break;
          case CSVTransactionFormatEnum.PayTypeCol : {
            label = "Type";
          } break;
          case CSVTransactionFormatEnum.ReturnCol : {
            label = "Return";
          } break;
          case CSVTransactionFormatEnum.SaleCol : {
            label = "Sale";
          } break;
          case CSVTransactionFormatEnum.StanCol : {
            label = "Txn #";
          } break;
          case CSVTransactionFormatEnum.StatusCol : {
            label = "Status";
          } break;
          case CSVTransactionFormatEnum.TimeCol : {
            label = "Time";
          } break;
          case CSVTransactionFormatEnum.TraceCol : {
            label = "Trace";
          } break;
          case CSVTransactionFormatEnum.VoidChgCol : {
            label = "Chg";
          } break;
        }
        runCols[i] = label;
      }
      allRowsOut(sbL);
      // details
      while(txn.next()) {
        clearFields();
        recToFields();
        allRowsOut(sbL);
      }
    }
  }

  private final void clearFields() {
    for(int i = runCols.length; i-->0;) {
      runCols[i] = "";
    }
  }

  private final void allRowsOut(StringBuffer sbL) {
    int count = runCols.length;
    for(int i = 0; i < count; i++) {
      if(i != 0) {
        sbL.append(TAB);
      }
      sbL.append("\"");
      sbL.append(notabs(runCols[i]));
      sbL.append("\"");
    }
    sbL.append(Ascii.CRLF);
  }

  private static final String TAB = "\t";
  private static final String SPACES4 = "    ";
  private static final String notabs(String unquoted) {
    return StringX.replace(unquoted, TAB, SPACES4);
  }

  private LedgerValue authamount = new LedgerValue(moneyformat); //+_+ to keep the drawer report and this report looking the same
  private LedgerValue settleamount = new LedgerValue(moneyformat); //+_+ to keep the drawer report and this report looking the same
  private LedgerValue diff = new LedgerValue(moneyformat);

  // +++ combine parts of this with UnsettledTransactionFormat, and push others of it into TxnRow!!! +++
  private synchronized void recToFields() {
    boolean wasReversed = txn.isVoided();
    boolean isReturn    = txn.isReturn();
    boolean isForce     = txn.isForce();
    boolean isSale      = txn.isSale() || txn.isForce() || (txn.isAuthOnly() && txn.settle());
    boolean isAuthonly  = txn.isAuthOnly() && !isSale;
    boolean isVoid      = txn.isReversal();
    boolean isModify    = txn.isModify();
    boolean isStoodin  = txn.wasStoodin();
    boolean inProgress = !txn.responded();//this method ignores stoodin
    boolean isDeclined = !(ActionCode.Approved.equals(txn.actioncode)) && !inProgress;  // +++ put on TranjorRow
    boolean didTransfer= !isVoid && !isModify && !wasReversed && !isAuthonly &&
        ((!isDeclined && !inProgress) || (isStoodin && countLosses));  // +++ put on TranjorRow
    UTC time = StringX.NonTrivial(txn.clientreftime) ? txn.refTime() : UTC.New(txn.transtarttime);//UTC#
    authamount.setto(txn.rawAuthAmount());// unsigned amount
    settleamount.setto(txn.rawSettleAmount());
    String status = "";
    String original = StringX.TrivialDefault(txn.origtxnid, "NOT FOUND");
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
      status = "SI";
    } else if(isAuthonly) {
      status = "AUTHONLY";
    } else if(isForce) {
      status = "FORCE";
    } else {
      // everything else list nothing (APPROVED); leave it as the default
    }
    if(!isVoid && !isModify) {
      setColumn(CSVTransactionFormatEnum.StanCol, txn.refNum());
      setColumn(CSVTransactionFormatEnum.PayTypeCol, String.valueOf(txn.paytype().Image()));
      setColumn(CSVTransactionFormatEnum.InstitutionCol, txn.cardType());
      setColumn(CSVTransactionFormatEnum.AcctNumCol, txn.last4());
      setColumn(CSVTransactionFormatEnum.MerchRefCol, txn.merchref);
      setColumn(CSVTransactionFormatEnum.ApprovalCol, txn.approvalcode);
    }
    if(isReturn) {
      setColumn(CSVTransactionFormatEnum.ReturnCol, settleamount.Image());
      settleamount.changeSign(); //negative for all other uses
    } else if(isSale){
      setColumn(CSVTransactionFormatEnum.SaleCol, settleamount.Image());
    } else if(isAuthonly) {
      // don't add it into totals, though, as it doesn't count
      setColumn(CSVTransactionFormatEnum.SaleCol, authamount.Image());
    } else if(isModify) {
      // don't do voids here, only modifies (voids have an obvious amount)
      diff.setto(0); // clear it
      diff.plus(settleamount); // add the settleamount
      diff.subtract(authamount);// subtract the authamount for the net change
      setColumn(CSVTransactionFormatEnum.VoidChgCol , isModify ? diff.Image() : "");
    }
    if(didTransfer) {
      setColumn(CSVTransactionFormatEnum.NetCol, settleamount.Image());
    }
    setColumn(CSVTransactionFormatEnum.TimeCol  , ltf.format(time));
    setColumn(CSVTransactionFormatEnum.StatusCol, status);
    setColumn(CSVTransactionFormatEnum.TraceCol , txn.txnid);
    Associate assoc = AssociateHome.Get(txn.associateid());
    if(assoc != null) {
      setColumn(CSVTransactionFormatEnum.AssociateCol, assoc.firstMiddleLast());
    }
  }

  private void setColumn(int coli, String value) {
    if((coli >=0) && (coli < runCols.length)) {
      runCols[coli] = value;
    }
  }

}
//$Id: CSVTransactionFormat.java,v 1.6 2004/03/03 23:11:25 mattm Exp $
