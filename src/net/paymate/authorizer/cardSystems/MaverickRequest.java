package net.paymate.authorizer.cardSystems;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/MaverickRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.31 $
 */

import net.paymate.authorizer.*;
import net.paymate.util.*;
import net.paymate.database.ours.query.TxnRow;
import net.paymate.jpos.data.*;
import net.paymate.data.*;

// Note that track1 does not go to maverick AT ALL. Only track2 is transmitted.

/* Mastercard Compliance data:
A request must be made using a new Enhanced Response Indicator of "M" which
will return CVV2/CVC2 responses and Cardsystem's BatchID in the freeform
field, and will add the 9 digit alphanumeric Banknet ReferenceID in Field
16, the Transaction Identifier field, and add the 4 digit numeric BankNet
MMDD in Field 18, the Validation code.

Questions for Barbara:
Can she send us a modified M-format spec?
Do we always use the "M" enhanced response indicator, or only on MC txns that we think will return CPS data?
Does the "M" go just after the "YA"/"YN" characters in the "Field 15 - Transaction Processing Indicators"?
What is the format of the freeform field data that will be returned [we have no example/spec]?
*/

public class MaverickRequest extends AuthRequest implements CardSystemConstants {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(MaverickRequest.class);

  // this is all that is contained in here:
  private VisaBuffer req=makeBuffer();

  private final VisaBuffer makeBuffer() {
    return VisaBuffer.NewSender(maxRequestSize()); // +++ does maverick use the LRC?
  }

  // for transporting over IP
  public byte [] toBytes() {
    byte [] toret = req.packet();
    dbg.VERBOSE("toBytes:"+Ascii.bracket(toret));
    return toret;
  }

  protected int maxRequestSize() {
    return MAXREQUEST;
  }

  public int compareTo(Object o) {
    // stub; nothing special to compare in this class [comparator will handle further comparisons, so 0 will not be returned in the end]:
    return 0;
  }

  // according to Steve Adcock, they don't use AcquirerBin, and we should zero-fill it.
  private static final int dummyAcquirerBin = 0;

  private static String TransactionCode(TxnRow tr, TxnRow trold){
    if(tr.isReversal()){
      if((trold != null) && trold.isReturn()){//was original operation a refund==credit?
        return "V5";
      } else {
        return "V1";//other options not encoded yet.
      }
    }
    if(tr.isReturn()){
      return "05";
    } else {
      return "54";//other options not encoded yet.
    }
  }

  public AuthRequest fromRequest(TxnRow tjr, TxnRow original, MerchantInfo merch){
//screw it. just make a new one everytime you extract
    req=makeBuffer();
//    req.start(maxRequestSize());
    //format
    req.append("M0");
    req.append(".");
    //4.4 bin
    req.appendInt(6,dummyAcquirerBin);// see above
    //terminal id for auth
    req.appendNumeric(10,merch.authtermid);//mainsail used char where an integer is defined by CS
    req.endFrame();
    req.appendNumeric(4,tjr.authseq);//### new 4 digit sequence#/terminal
    req.appendAlpha(2,TransactionCode(tjr, original));//54=pruchase, V1=void,04 force, 56=moto
    req.append('@');//4.9 ==signature, ### other options apply!!!
    dbg.ERROR("record="+tjr); // ---
    if(tjr.isReversal()){
      //card number is always manually entered regardless of whether track data is available
      CardSystemsAuth.source(req, "", "", true, tjr.card());
      req.endFrame();
      req.appendFrame(original.approvalcode);//response to original txn!!!
      transactionProcessingIndicators();
      CardSystemsAuth.AmountInfo(req,original.rawAuthAmount());// +_+ should chase down and demote this guy to int.
      req.emptyFrames(3);
      req.appendFrame(original.authrrn);//original reference number
      req.emptyFrames(2);
    } else if(tjr.isDebit()){//very restrictive about track usage
      //CardRequest creq=(CardRequest) freq;
      MSRData card = tjr.card();
      TrackData t2 = card.track(MSRData.T2);
      if(t2.isProper(MSRData.T2)){//is track2 present and a valid track 2?
        CardSystemsAuth.source(req, "", t2.Data(), false, card);//#false ok
        req.endFrame();
      } else {
        return null;//can't do debit without track 2
      }
      //+++ much is still missing for debit
      return null;
    } else if(/*freq instanceof CardRequest*/tjr.isCredit()){ // is "card" specific enough?
      boolean notSwiped= ! tjr.SaleInfo().wasSwiped();//can't trust card object itself for this +_+
      MSRData card = tjr.card();
      CardSystemsAuth.source(req, card.track(MSRData.T1).Data(),
                             card.track(MSRData.T2).Data(), notSwiped, tjr.card());
      req.endFrame();
      req.endFrame();//no address verification info
      transactionProcessingIndicators();
      CardSystemsAuth.AmountInfo(req,tjr.rawAuthAmount());//@alh
      req.emptyFrames(6);
    } else {
      dbg.ERROR("Unknown request variant [PT:"+tjr.paytype+"/TT:"+tjr.transfertype+"]!  Returning null!");
      return null;
    }
    req.end();
    dbg.ERROR("isOk() returned " + req.isOk());
    return this;
  }

  // 'Y' means that we are CPS capable
  // ' ' means that this is NOT a MOTO
  // 'C' means HOST CAPTURE and close right now, where 'N' means that we will close ourselves later, 'A' means auth only; no settlement.
  // 'M' means to return the MAstercard Compliance Data
  private void transactionProcessingIndicators() {
    req.appendFrame("Y " + (authNoSettle ? "A" : "N") + "M");
  }

  private boolean authNoSettle = false; // auth only

  public MaverickRequest(boolean authNoSettle) {
    this.authNoSettle = authNoSettle;
  }

  /**
   * test request formatter, somewhat
   */
  static public void main(String[] args) {
    MaverickRequest testreq=new MaverickRequest(false);
    testreq.test(args);
  }

}

/*
Request Format [Maverick M]:
## FieldName                          DataFormat DataLength Section Example
1  Record Format                      A          1          4.1     M
2  Application Type                   A          1          4.2     0
3  Message Delimiter                  A          1          4.3     "."
4  Acquirer Bin or Terminal Batch Nbr N or A     6          4.4     "123456" or "1234"
5  Terminal Number                    N          10         4.5     "9999999901"
6  Field Separator                    A          1          4.6     <FS> or HEX 1C
7  Transaction Sequence Number        N          4          4.7     0001
8  Transaction Code                   A          2          4.8     "54"
9  Cardholder ID Code                 A          1          4.9     "@"
10 Account Data Source Code           A          1          4.10    D
11 Customer Data Field                A          1-80       4.11    Track1, track 2, or manually entered customer data
12 Field Separator                    A          1          4.6     <FS> or HEX 1C
13 Address Verification OR Auth Code  See 4.12   See 4.12   4.12    1234MAINSTREET123456789 OR 123456 (Auth Code)
14 Field Separator                    A          1          4.6     <FS> or HEX 1C
15 Transaction Processing Indicators  A          0 to 4     4.13
16 Field Separator                    A          1          4.6     <FS> or HEX 1C
17 Transaction Amount                 N          0 to 12    4.14
18 Field Separator                    A          1          4.6     <FS> or HEX 1C
19 Secondary Amount                   N          0 to 12    4.15
20 Field Separator                    A          1          4.6     <FS> or HEX 1C
21 Market Specific Data               A          0 or 4     4.16
22 Field Separator                    A          1          4.6     <FS> or HEX 1C
23 Informational Data                 A          0-60       4.17
24 Field Separator                    A          1          4.6     <FS> or HEX 1C
25 Original Reference Number          N          0 or 12    4.18
26 Field Separator                    A          1          4.6     <FS> or HEX 1C
27 Purchasing Card Data               A          0-17       4.19
28 Field Separator                    A          1          4.6     <FS> or HEX 1C
28 Free Form Data                     A          0-45       4.20
30 Field Separator                    A          1          4.6     <FS> or HEX 1C
*/

/*
... this seems to be the tail endof discussing batch settlenment:
After checking the LRC on the packet returned by CardSystems, the terminal should look for the packet to
begin with "APP" or "D." If the packet returned begins with an "APP," the next transaction should be sent.
Upon receiving a packet beginning with "D", the terminal should display the message received and hang up.
After sending the last transaction, the terminal should check for the final "APP" message. If the batch
transmission is successful, the terminal should clear the batch or mark it as sent so that the transactions are
not settled again.
*/

// $Id: MaverickRequest.java,v 1.31 2003/12/10 02:16:46 mattm Exp $
