package net.paymate.authorizer.npc;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCAuthSubmitRequest.java,v $
 * Description:  Settlement messaging spec for NPC per "NPC Point of Sale Batch Upload Specs" doc dated July, 2001
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * +++ TODO:     Finish the debit parts -- LATER!
 * @author PayMate.net
 * @version $Revision: 1.20 $
 */

import net.paymate.authorizer.*;
import net.paymate.data.*;
import net.paymate.util.*; // Safe
import net.paymate.database.ours.query.TxnRow;
import net.paymate.awtx.*; // RealMoney
import net.paymate.jpos.data.*; // MSRData
import java.text.*; // DecimalFormat
import net.paymate.lang.StringX;

public class NPCAuthSubmitRequest extends AuthSubmitRequest implements NPCConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(NPCAuthSubmitResponse.class);

  private boolean test = false;
  private String compuserveRoutingIndicator="";
  public boolean justSimulate = false;

  private Authorizer handler = null;
  private String MerchantNumber = "";
  private String StoreNumber    = "";
  private String MerchantSIC    = "";

  public NPCAuthSubmitRequest(Authid authid, Terminalid terminalid, MerchantInfo merch, Authorizer handler, boolean test, String compuserveRoutingIndicator, boolean justSimulate) {
    super(authid, terminalid, merch);
    this.test = test;
    this.handler = handler;
    this.justSimulate = justSimulate;
    this.compuserveRoutingIndicator = compuserveRoutingIndicator;
    splitMerchInfo();
    NPCAuth auther = (NPCAuth) handler;
    authseqSimulator = new Counter(auther.sequenceRange.low(), auther.sequenceRange.high());
  }

  private void splitMerchInfo() {
    String [] split = splitMerchantInfo(merch.authmerchid);
    MerchantNumber = split[0];
    StoreNumber    = split[1];
    MerchantSIC    = split[2];
  }
  // this is so that we can test this function without having everything else present
  private static final String [] splitMerchantInfo(String merchantInfo) {
    String [] ret = new String [3];
dbg.ERROR("Splitting merchantInfo: " + merchantInfo);
    ret[0] = StringX.subString(merchantInfo, 0, 4);   // 4 digits
    ret[1] = StringX.subString(merchantInfo, 4, 11);  // 7 digits
    ret[2] = StringX.subString(merchantInfo, 11, 15); // 4 digits
dbg.ERROR("MerchantNumber="+ret[0]);
dbg.ERROR("StoreNumber="+ret[1]);
dbg.ERROR("MerchantSIC="+ret[2]);
    return ret;
  }

  // --- this doesn't work.  buffer is always empty
  private final AsciiBuffer makeRequest() {
    return AsciiBuffer.Newx(maxRequestSize());
  }

  protected int maxRequestSize() {
    return MAXREQUESTSIZE;
  }

  // +++ move this into the VisaBuffer (and other similar classes)
  private final String truncInt(long amount, int maxDigits) {
    String amt = String.valueOf(amount);
    if(amt.length() > maxDigits) {
      handler.PANIC("Message misformatted since the amount ["+amt+"] is too long ["+maxDigits+"]!");
    }
    return amt;
  }

  public Accumulator posamounts = new Accumulator();
  public int postxncount() {
    return (int)posamounts.getCount();
  }
  public int postxntotal() {
    return (int)posamounts.getTotal();
  }

  public Accumulator negamounts = new Accumulator();
  public int negtxncount() {
    return (int)negamounts.getCount();
  }
  public int negtxntotal() {
    return (int)negamounts.getTotal();
  }

  // NOTE: I don't like to use append() functions on the VisaBuffer that automatically put the FS on the end.
  // Too hard to see during debug, and they don't match the spex that way [line per item]!

  /* package */ byte [] header() {
    AsciiBuffer req=makeRequest();
    req.append(TERMTYPE); // F.
    req.appendNumeric(6, compuserveRoutingIndicator); // MUST be 6 digits!, probably "095600"

/*
MerchantInfo.authmerchid = merchantNumber+storeNumber

LEN Position Description
2   1-2      Term type F.
6   3-8      CompuServe routing indicator
4   9-12     Merchant number
4   13-16    Terminal number
7   17-23    Store number

IGNORING "F." and routing # [configed per authorizer, I hope],
Ttl store info = 4 + 7 = 11
Ttl term  info = 4

Our current authmerchid is CHAR(16).
Our current authtermid  is CHAR(10).

+++ @@@ %%% REALLY IMPORTANT !!!
The compuserve routing indicator may or may not be a per-store configurable item.
I am coding as though it isn't.
If we find it is, the store info will be long enough to handle it.
If it needs to be per-terminal, the terminal info is long enough to handle it.
However, code will need to change.
*/
    req.appendNumeric(4, MerchantNumber);
    req.appendNumeric(4, merch.authtermid);
    req.appendNumeric(7, StoreNumber);
    req.endFrame();// FS
    req.append(test ? UploadTypeInquiryWithoutBatchClear : UploadTypeImmediateWithBatchClear);
    req.endFrame();// FS
    req.append(TemplateTypeRetail); // for now
    // Batch Number [3] 001-999
    req.appendNumber(3, this.termbatchnum);
    // Transaction Count [0-6] 0-999999; 0 or null only if batch is empty [this should never hit a limit, unless we are settling more than $9999.99.]
    req.append(truncInt(posamounts.getCount() + negamounts.getCount(), 6));
    req.endFrame();// FS
    // Total Positive $ amount 0-9 Numeric [0-999999999]
    req.append(truncInt(posamounts.getTotal(), 9));
    req.endFrame();// FS
    // Total Negative $ amount 0-9 Numeric [0-999999999]
    req.append(truncInt(negamounts.getTotal(), 9));
    req.endFrame();// FS
    // Total Net $ amount 0-10 Alphanumeric [-9999999999 to 9999999999], decimal implied
    req.append(truncInt(posamounts.getTotal() - negamounts.getTotal(), 9));
//    // GS
//    req.append(Ascii.GS);
//    // GS
//    req.append(Ascii.GS);
//    // Debit Header Segment 0-29...
//    // Batch Number [same]
//
//    // Zero Filler
//    req.append("0");
//    // Debit purchase count 3 numeric [000-999]
//
//    // Debit purchase $ amount 0-8 numeric [0-99999999], decimal implied
//
//    req.endFrame();// FS
//    // Debit return count 3 numeric [000-999]
//
//    // Debit return $ amount 0-8 numeric [0-99999999], decimal implied
//
//
//    req.endFrame();// FS
//    // Gateway ID
//    req.append(GatewayID);
//
    return req.packet();
  }

  private Counter authseqSimulator = null; // set in the constructor

  /* package */ byte [] detail() {
    TxnRow tjr = records;
    // +++ @NPC WE NEED TO FIX THIS:
    // +++ use this and generate the numbers anew with every batch instead of using the numbers from CS:
    // update(QS.genSetTxnSequence(tjr.txnid, counter.incr()));
    // renumber here and now.
    int authseq = (int)authseqSimulator.incr();//((NPCAuth)handler).squeezeAuthseq(StringX.parseInt(tjr.authseq));

    RealMoney rawamount = tjr.rawSettleAmount();
    long longAmount = rawamount.Value();
    String amt7 = truncInt(longAmount, 7);
    AsciiBuffer req=makeRequest();
    req.endRecord(); // all records separated by this
    req.appendNumber(3,authseq); // numbers them 1, 2, 3, etc.
    MSRData card = tjr.card();
    req.append(card.accountNumber.Image()); // Account Number
    req.endFrame(); // FS
    req.append(card.expirationDate.YYmm()); // Account Expiration Date
    String transactionType = "  ";
    // +++ Use Enumeration or some other class for this (maybe extend a TransactionType class)
    // req.appendInt(2, PTTransactionCodes.From(tjr.paytype(),tjr.transfertype()).code()); // 6
    if(tjr.isCredit()) {
      if(tjr.isReturn()) {
        // credit return
        transactionType = TransactionTypeCredit;
        negamounts.add(longAmount);
      } else {
        // credit sale
        transactionType = TransactionTypeSale;
        posamounts.add(longAmount);
      }
//    } else if(tjr.isDebit()) {
//      if(tjr.isReturn()) {
//        // debit return
//        transactionType = "H ";
//      } else {
//        // debit sale
//        transactionType = "E ";
//      }
    } else {
      // NOT supported!
      // +++ need to mark the txn as a LOSS!!!! +++ and/or need to remove it from the batch !!!
      String msg = "DETAIL,TxnType not supported:"+tjr.txnid();
      handler.PANIC(msg);
      dbg.ERROR(msg);
      return null;
    }
    req.appendAlpha(2, transactionType);
    req.append(amt7);
    req.endFrame();
    // +++ until we do voids ...
    req.append('A'); // Transaction status
    req.appendAlpha(2, tjr.institution); // Card Description, valid: AE, CB, DB, DC, DS, EB, JC, MC, PL, VS
    req.appendAlpha(6, tjr.approvalcode); // Authorizer Number:  is this the approval code?  What i'm going to use for now
    req.append('1'); // authorizer; since we don't do force yet ...
    req.append(tjr.isManual() ? '2' : '1'); // Data Source
    LocalTimeFormat ltf = LocalTimeFormat.New(merch.tz(), "HHmmssMMddyy");
    req.appendNumeric(12, ltf.format(UTC.New(tjr.transtarttime))); // Transaction Time & Transaction Date

    CPSdata cpsdata = tjr.getCPSdata();
    boolean cpsdatavalid = CPSdata.isValid(cpsdata);
    req.appendNumeric(4, MerchantSIC);

    if(/*template data || */ cpsdatavalid || tjr.isDebit()) {
      req.append(Ascii.GS); // GS
      // [NO] Template Data
      if(cpsdatavalid || tjr.isDebit()) {
        req.append(Ascii.GS); // GS
        if(cpsdatavalid) {
          // Compliance Data
          // +++ @@@ %%% If the fact that this data is ALWAYS this size was a parameter of each piece of data,
          // +++ we could just call req.append(cpsdata.whatever).append(..., or maybe later, just req.append(cpsdata)!
          if(cpsdata instanceof VisaCPSdata) {
            VisaCPSdata visaCPSdata = (VisaCPSdata)cpsdata;
            // ACI [1, alpha]
            req.appendAlpha(1, visaCPSdata.cpsaci);
            // AuthResponseCd [2, alpha]
            req.appendAlpha(2, visaCPSdata.cpsrespcode);
            // TransactionID [15, numeric]
            req.appendNumeric(15, visaCPSdata.cpstxnid);
            // Validation Code [4, numeric]
            req.appendNumeric(4, visaCPSdata.cpsvalcode);
          } else if(cpsdata instanceof MastercardCPSdata) {
            MastercardCPSdata MCCPSdata = (MastercardCPSdata)cpsdata;
            //Filler                - 1 - Alphanumeric - Space
            req.appendAlpha(1, "");
            //Downgrade Indicator   - 2 - Alphanumeric - SPACES (per Rob, 20020717)
            req.appendAlpha(2, "");
            //BankNet Reference No. - 9 - Alphanumeric
            req.appendAlpha(9, MCCPSdata.referenceNumber);
            //BankNet Date          - 4 - Numeric - MMDD Format
            req.appendAlpha(4, MCCPSdata.date);
            //Filler                - 6 - Alphanumeric - Spaces
            req.appendAlpha(6, "");
          }
          // Authorized $ Amount [0-7, numeric, implied decimal]
          req.append(amt7); // +++ eventually this may have to come out of the auth response!!!
        }
        if(tjr.isDebit()) {
          req.append(Ascii.GS); // GS
          // --- This is prevented via other means.
          // +++ Be sure that when debit is enabled, this code is filled in.
          // +++ and also be sure to include the debit totals in the header !!!
          String msg = "DEBIT,CannotGen,txn="+tjr.txnid;
          handler.PANIC(msg);
          dbg.ERROR(msg);
          return null;
        } else {
          dbg.VERBOSE("tjr is not debit.");
        }
        // [NO] Commercial Card Data
      }
    }
    byte [] ret = req.packet();
    dbg.VERBOSE("returning packet: " + Ascii.bracket(ret));
    return ret;
  }

  /* package */ byte [] trailer() {
    AsciiBuffer req=makeRequest();
    req.append(Ascii.US);
    return req.packet();
  }

  public static final void main(String [] args) {
    net.paymate.Main app=new net.paymate.Main(NPCAuthSubmitRequest.class);
    app.stdStart(args);
    String [] split = splitMerchantInfo("073700200025814");
  }

}
