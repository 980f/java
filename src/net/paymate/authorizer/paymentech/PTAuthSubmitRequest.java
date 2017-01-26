package net.paymate.authorizer.paymentech;

import net.paymate.authorizer.*;
import net.paymate.data.*;
import net.paymate.util.*; // Safe
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.awtx.*; // RealMoney
import net.paymate.jpos.data.*; // MSRData
import net.paymate.lang.StringX;
import net.paymate.terminalClient.PosSocket.paymentech.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PTAuthSubmitRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.42 $
 * @todo relocate parseLong and parseInt filtering to database entry validation.
 *  if the usage here actually makes a change then we don't really know whose account the auth is being sent to.ent!
 */

public class PTAuthSubmitRequest extends AuthSubmitRequest implements PaymentechConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PTAuthSubmitRequest.class);

  private PaymentechAuth handler = null;

  public PTAuthSubmitRequest(Authid authid, Terminalid terminalid, MerchantInfo merch, PaymentechAuth handler) {
    super(authid, terminalid, merch);
    ptnctid = new PTTid(merch.authtermid);
    this.handler = handler;
    dbg.VERBOSE(ptnctid.spam());
  }

  private PTTid ptnctid = null;

  private AsciiBuffer req=makeRequest(); // +_+ this call to makeRequest() may be an extra

  private final AsciiBuffer makeRequest() {
//    return VisaBuffer.NewSender(maxRequestSize()).setClipLRC(); // paymentech doesn't use LRC!
    return AsciiBuffer.Newx(maxRequestSize());
  }

  protected int maxRequestSize() {
    return MAXREQUESTSIZE;
  }

  /* package */ byte [] header(int txncode) {
    // see the PaymentechRequest for more info on these, or above
    req=makeRequest();
//    req.start(maxRequestSize()); // really just 99 for this message
    req.append(TERMINALCAPTUREINDICATOR); // K
    req.append(SYSTEMSEPARATOR); // .
    req.append(ROUTINGINDICATOR); // A02000
    req.appendNumeric(4+12,""+StringX.parseLong(merch.authmerchid));
    req.appendNumeric(3,""+ptnctid.tidint); //StringX.parseInt(merch.authtermid));// req.append("002"); // A6 Terminal Number Pic 9 (3) Assigned by Paymentech
    req.appendSigned(1,1); // A7 Transaction Sequence Flag Pic 9 (1) 1 = Single batch upload
    req.appendSigned(3,0); // 000
    req.appendSigned(3,this.batchseq); // Sequence Number [1 - 998, only echoed back, not used]
    req.append(TRANSACTIONCLASS); // F
    req.appendSigned(2,txncode);
    req.appendSigned(6, this.termbatchnum);
    req.appendSigned(3, 0); // 000 (batch sequence number)
    req.appendSigned(1, 0); // 0 (batch offset: current)
    req.appendSigned(6, txncount());
dbg.ERROR("txntotal = "+txntotal());
    PaymentechAuth.appendTotalMoney(req, txntotal());
    SystemInfo(req);
    req.endFrame(); // end of this chunk
    req.appendNumeric(8, "0"); // field 20, req'd filler
//    req.end();
//    dbg.ERROR("isOk() returned " + req.isOk());
    return handler.addHeaderAndSTXETX(req.packet());
  }

  private static final void SystemInfo(AsciiBuffer req) { // field 18,
    req.appendAlpha(23, "SINET  012802VERSION1.0"); // Per Bev 1/25/2002
  }


  /* package */ byte [] detail(boolean isLast) {
    TxnRow tjr = records;

    // add up the count and total; Everything is positive except for returns and VOIDS, which are negative. add more?
    long amt =  tjr.netSettleAmountCents();
    dbg.ERROR("adding " + amt);
    amounts.add(amt);

    LocalTimeFormat ltf = LocalTimeFormat.New(merch.tz(), "MMddyyHHmmss");

    req=makeRequest();
//    req.start(maxRequestSize());
    req.appendSigned(3,0); // 000 2-filler
    req.appendNumeric(3,tjr.authseq); // 3-Sequence Number
    req.append(TRANSACTIONCLASS); // 4-Transaction Class
    req.appendSigned(8,0); // 5-Filler
    req.appendSigned(2, PTTransactionCodes.settleFrom(tjr.paytype(),tjr.transfertype()).code()); // 6
    req.append(tjr.amountsMatch() ? "A" : "C"); // 7

// +++ reconcile with PaymentechRequest.appendCardInfo() - this function does NOT list full tracks; only cardnum and expiry, and the latter without a framesep
    MSRData card = tjr.card();
//    card.setTrack(card.T1, tjr.track1data); // need these for testing the content
//    card.setTrack(card.T2, tjr.track2data); // need these for testing the content
    dbg.VERBOSE("card = " + card.toSpam());
    String trackdata=card.track(card.T2).Data();
    if(TrackData.isProper(MSRData.T2, trackdata)){//is track2 present and a valid track 2?
      req.appendSigned(2,3); // 8 - Entry data source NOTE!  This is the only workable option for debit!
    } else {
      trackdata=card.track(card.T1).Data();
      if(TrackData.isProper(MSRData.T1, trackdata)) {
        req.appendSigned(2,4); // 8 - Entry data source
      } else {
        req.appendSigned(2,2); // 8 - Entry data source
      }
    }
    req.appendFrame(card.accountNumber.Image()); // 9/10 - framed cc#

    // USE "1249" for manual GC expiry date !
    if(tjr.isGiftCard() && tjr.isManual()) { // +++ manual not allowed w/debit!
      req.append("1249");
    } else {
      req.append(card.expirationDate.mmYY("1249")); // 11 - expiry
    }

    // we HAVE to check for a valid settleamount BEFORE getting into this function!
    PaymentechAuth.appendMoney(req, tjr.rawSettleAmount()); //@alh 12+13 - amount
    if(tjr.isGiftCard() || tjr.isDebit()) { // +++ or is a void of a HC txn!  or is debit!
      req.appendAlpha(8, tjr.authrrn/*StringX.subString(tjr.authtracedata, 5)*/); // 13b this is the paymentech "trace", which is what we consider the RRN
    }
    req.emptyFrames(2);
    if(tjr.isDebit()) {
      // append cashback (not implemented yet)
//      req.appendAlpha(4, "0.00");
    }
    req.emptyFrames(1);
    req.appendNumeric(12, ltf.format(UTC.New(tjr.transtarttime))); // 17/18 - date/time
    req.appendAlpha(6, (tjr.isCredit() && tjr.isReturn()) ? "" : tjr.approvalcode); // 19 - approval code
    String cardType = tjr.institution; // 20: MUST have a card type here, and it MUST match what they want, so ignore what is stored in the table and generate it anyway

    switch (tjr.paytype().Value()) {
      case PayType.Debit:{
        cardType = "DB"; // check cards must be sent in as DB if they were used that way.
      } break;
      case PayType.GiftCard: {
        cardType = "SV";
      } break;
      default:{
        if (StringX.equalStrings(cardType, "AE")) { // SEE UTF-197 TCS, page 161, 13.1 STANDARD CARD TYPES SUPPORTED
          cardType = "AX"; // pain in the ass
        }
        else if (StringX.equalStrings(cardType, "VS")) {
          cardType = "VI"; // pain in the ass
        }
        break;
      }
    }
    req.appendAlpha(2, cardType);
    PTAuthTraceData authtracedata = new PTAuthTraceData(tjr.authtracedata);
    // offline txns must send 000 since nothing was returned from an auth.
    // all other txns must send what they got
    // +++ get these details from a function on the authorizer module?
    if(tjr.paytype().is(PayType.Credit) &&
       (tjr.transfertype().is(TransferType.Force) ||
        tjr.transfertype().is(TransferType.Return)) ) {
      req.appendSigned(3, 0); // 21 - 22: networkid + source; zero out if the txn wasn't authed
    } else {
      req.appendAlpha(3, authtracedata.networkAndSource); // 21 - 22: networkid + source; do NOT zero out if they are blanks.
    }
    // debit surcharge is required? Not supposed to be.
    req.emptyFrames(2); // 23-24 (empty)
    switch(tjr.paytype().Value()) {
      case PayType.GiftCard: {
        req.emptyFrames(1); // 25 - empty
        // DONE!
      } break;
      case PayType.Debit: {
        // since we are NOT sending token data at this time (CVV2, etc.), don't put anything here
      } break;
      default: {
        PaymentechAuth.industrySpecificData(req, tjr, merch, this.termbatchnum, tjr.authseq()); // 25 - 28
        if(!merch.type.is(MerchantType.Restaurant)) {
          req.emptyFrames(1); // 28 for retail (and maybe others, but we are just doing these 2)
          // skip 29 - 33
        }
      } break;
    }
    req.endRecord(); // 34
//    req.end();
//    dbg.ERROR("isOk() returned " + req.isOk());
    return req.packet();//PaymentechAuth.addHeaderAndSTXETX(req.packet());
  }

/*
1 STX Pic X (1) REQ'D 02h
2 Filler Pic 9 (3) REQ'D 000 = always
3 Filler - Seq # Pic 9 (3) REQ'D 000 = always
4 Transaction Class Pic X (1) REQ'D F = Financial Transaction
5 Filler - RR# Pic 9 (8) REQ'D 00000000 always
6 Transaction Code Pic X (2) REQ'D 55 = Batch Upload Trailer
7 ETX Pic X(1) REQ'D 03h
8 LRC Pic X(1) REQ'D Calculated XOR “00-FF”
*/
  /* package */ byte [] trailer() {
    // see the PaymentechRequest for more info on these, or above
    req=makeRequest();
//    req.start(maxRequestSize()); // really just ?? for this message
    req.appendSigned(3,0); // 000 2-filler
    req.appendSigned(3,0); // 000 3-filler
    req.append(TRANSACTIONCLASS); // 4-F
    req.appendSigned(8, 0); // 00000000 5-Filler
    req.appendSigned(2,PTTransactionCodes.BatchUploadTrailer);
//    req.end();
//    dbg.ERROR("isOk() returned " + req.isOk());
    return handler.addHeaderAndSTXETX(req.packet());
  }
}
