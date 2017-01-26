package net.paymate.authorizer.paymentech;

import net.paymate.authorizer.*; // AuthRequest
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.*; // ErrorLogStream
import net.paymate.jpos.data.*; // TrackData
import net.paymate.data.*; // MerchantInfo
import net.paymate.awtx.RealMoney;
import net.paymate.lang.StringX;
import net.paymate.terminalClient.PosSocket.paymentech.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PaymentechRequest.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.70 $
 */

public class PaymentechRequest extends AuthRequest implements PaymentechConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PaymentechRequest.class);

  // this is all that is contained in here:
  private VisaBuffer req=makeRequest();

  private final VisaBuffer makeRequest() {
    return VisaBuffer.NewSender(maxRequestSize()).setClipLRC(); // paymentecdh doesn't use LRC!
  }

  private boolean hostCaptureOnly = false; // false is for taco-like customers

  private PaymentechAuth handler = null;

  public PaymentechRequest(boolean hostCaptureOnly, PaymentechAuth handler) {
    this.hostCaptureOnly = hostCaptureOnly;
    this.handler = handler;
  }


  // tacks on the header and returns the full message, header + body
  /**
   * @todo use bytearrayoutputstream and wordyoutputstream
   */
  public byte [] toBytes() {
    return handler.addHeader(req.packet());
  }

  protected int maxRequestSize() {
    return MAXREQUESTSIZE;
  }

  public int compareTo(Object o) {
    // nothing special to compare to. let the super handle it
    return 0;
  }

/*
Paymentech terminal-capture [TC] authorization responses only include
one field for matching it with its request  ... a 3-digit sequence
number.

I believe the sequence number has nothing to do with the batch.  We can
roll this number over within the socket [or actually within the
constructor for the PaymentechRequests]. Every txn in the socket will
have a different sequence number. This means that we can never have more
than 1000 items in our queue. Probably not a problem.
*/

  private String hostcode(PayType pt){
    switch (pt.Value()) {
      default:
      case PayType.Cash:
      case PayType.Check: {
        return "X"; //make a malformed message
      }
      case PayType.Credit: {
        return hostCaptureOnly ? HOSTCAPTUREINDICATOR : TERMINALCAPTUREINDICATOR;
      }
      case PayType.Debit:
      case PayType.GiftCard: {
        return HOSTCAPTUREINDICATOR;
      }
    }
  }

  //21 = VSC capable, chip card used, manually entered
  //22 = VSC capable, chip card used, track II data
  //23 = VSC capable, (future use)
  //24 = VSC capable, non-chip card used, manually entered
  //25 = VSC capable, non-chip card used, track I data
  //26 = VSC capable, non chip card used, track II data
  //27 = VSC capable, chip card used, chip failure, Track II presented. If it is undeterminable that the manually keyedtransaction is a chip card (21) or a non-chip card (24), submit all transactions as a 21.


  private void appendCardInfo(MSRData card){  // A13 Entry Data Source Pic 9 (2)
    String trackdata=card.track(card.T2).Data();
    if(TrackData.isProper(MSRData.T2, trackdata)){//is track2 present and a valid track 2?
      req.appendInt(2,3); // A13  entrysource.t2
      req.appendFrame(trackdata); // B1
    } else {
      trackdata=card.track(card.T1).Data();
      if(TrackData.isProper(MSRData.T1, trackdata)) {
        req.appendInt(2,4); // A13  entrysource.t1
        req.appendFrame(trackdata); // B1
      } else {//entrysource.manual
        req.appendInt(2,2); // A13
        req.appendFrame(card.accountNumber.Image()); // B1 & B2
        req.appendFrame(card.expirationDate.mmYY("1249")); // B3 & B4
      }
    }
  }

  private void appendCreditSpecificStuff(TxnRow record, MerchantInfo merch){   // F: industry stuff
    req.emptyFrames(2); // formerly inside pinCardMoney C5 FS Pic X (1) 1Ch,// C6 FS Pic X (1) 1Ch
    PaymentechAuth.industrySpecificData(req, record, merch, 0, 0);
    req.endFrame(); // 28
    // G: misc stuff
    req.emptyFrames(2); // G1 FS Pic X (1) 1Ch // G2 FS Pic X (1) 1Ch// G3 FS Pic X (1) 1Ch
    // H: Address verification
    Institution issuer=CardIssuer.getFrom2(record.institution); // only good for AE/VS/MC/DS
    boolean goodissuer =
        issuer.equals(CardIssuer.Visa) ||
        issuer.equals(CardIssuer.MasterCard) ||
        issuer.equals(CardIssuer.Discover) ||
        issuer.equals(CardIssuer.AmericanExpress);
    dbg.VERBOSE("goodissuer="+goodissuer);
    AVSInfo avsinfo = null;
    MSRData card = record.card();
    dbg.VERBOSE("card="+card);
    if((card != null) && goodissuer /* && JUSTFORSALETYPES (no return/void/etc) ?* --- NOT AN ISSUE, as we don't auth returns!*/) {
      avsinfo = card.avsInfo();
    }
    dbg.VERBOSE("avsinfo="+avsinfo);
    if(avsinfo != null) {
      dbg.VERBOSE("address="+avsinfo.address());
      req.append(StringX.left(avsinfo.address(), 20));
    }
    req.endFrame();
    if(avsinfo != null) {
      String zip = StringX.left(avsinfo.zip(), 9);
      int len = zip.length();
      dbg.VERBOSE("zip["+len+"]="+zip);
      if(len == 5 || len == 9) {
        req.append(zip); // should only be numbers, can only be 5 or 9 long
      }
    }
    req.endFrame();
    // I: Purchase card info
    req.emptyFrames(1); // I6 FS Pic X (1) - 1Ch This Field Separator identifies the start of
                        // token data, if token data is present and is the final
                        // Field Separator for ordinal data and should be
                        // followed by ETX if no token data is present.
  }

  private void appendDebitSpecificStuff(TxnRow record){
    req.appendNumeric(16,record.pindata().ksnImage());//1 DUKPT KSN Pic X (16) Ö Key Sequence Number (KSN) in the clear.
    req.appendNumeric(16,record.pindata().Image());//2 DUKPT PIN block Pic X (16) Ö DUKPT DES encrypted PIN block.
    req.endFrame();//3 FS Pic X (1) Ö 1Ch
    PaymentechAuth.appendMoney(req, RealMoney.Zero());//(tjr.rawamount().add(record.cashback));
  }

/**
 * the industry specific data for GiftCard is nominally constant as we do not bother
 * with multiple issuances (series of cards all with same value) in one message.
*/
  private void appendGiftCardSpecificStuff(TxnRow record){
    req.emptyFrames(2); // formerly inside pinCardMoney C5 FS Pic X (1) 1Ch,// C6 FS Pic X (1) 1Ch
  //if query add two more empty frames.
    if(record.transfertype().is(TransferType.Query)){
      req.emptyFrames(2);
    }
    req.appendInt(3,14);//1 Industry Code Pic 9(3) Ö 014 = Stored Value
    //stuff useless fields with tracer info
    if(true){
      req.appendAlpha(15,"");//2 External Transaction Identifier Pic X(15) Ö Cash Register transaction identifier
      req.appendAlpha(10,"");//3 Employee Number Pic X(10) Ö LJSF
    } else {
      req.appendAlpha(15,record.txnid);//2 External Transaction Identifier Pic X(15) Ö Cash Register transaction identifier
      req.appendAlpha(10,record.refNum());//3 Employee Number Pic X(10) Ö LJSF
    }
    //we reuqire that clerk does "cashout" operation by balance inquiry followed by normal sale
    req.appendAlpha(1,"N");//4 Cash Out Indicator Pic X(1) Ö Y or N – Y is only allowed when the Transaction Code is 73.
//the next two must be zero for query, and other transfer types seem happy with that
//    req.appendInt(2,0); //5 Sequence number of card being issued/activated. Pic 9(2)
//    req.appendInt(2,0); //6 Total number of cards being issued/activated. Pic 9(2)
    // 1 of 1 (1/1), per Beverly 2002.01.30
    int thisof = record.isReturn() ? 1 : 0;
    req.appendInt(2,thisof); //5 Sequence number of card being issued/activated. Pic 9(2)
    req.appendInt(2,thisof); //6 Total number of cards being issued/activated. Pic 9(2)
  }

  private void pinCardMoney(TxnRow tjr){
    req.appendInt(1,1); // A12 12 PIN Capability Code Pic 9 (1) 1 = Terminal device accepts PIN entry, 2 = Terminal device does not accept PIN entry
    appendCardInfo(tjr.card());
      //the following would be simplified if we make it someone else's problem to deal with a zero transaction amount.
    if(tjr.isGiftCard()&&tjr.isQuery()){
      PaymentechAuth.appendMoneyZok(req, tjr.rawAuthAmount());
    } else { // reversals aren't in here, so this is fine!
      PaymentechAuth.appendMoney(req, tjr.rawAuthAmount()); // C1 Transaction Amount Pic X (8) Ö Variable, minimum value is 0.01 with real decimal point. +++ needs work! & C2 FS Pic X (1) 1Ch
    }
    req.appendInt(8,0); // C3 Filler LRR# Pic 9 (8) 00000000,
    req.emptyFrames(1); //  C4 FS Pic X (1) 1Ch,
  }

  public AuthRequest fromRequest(TxnRow tjr, TxnRow original, MerchantInfo merch) {
    // to handle recycling:
    req=makeRequest();
    boolean isgateway = tjr.hasAuthRequest();
//    Terminalid tid = tjr.terminalid();
//    TerminalInfo tinfo = net.paymate.database.PayMateDBDispenser.getPayMateDB().getTerminalInfo(tid);
//    isgateway = (tinfo != null) && tinfo.isGateway();
    if(isgateway) {
      // +_+ check to see that the request has enough info to do this ???
      req.append(tjr.authattempt.authrequest.rawValue().getBytes());
    } else {
      req.append(hostcode(tjr.paytype()));// A2 System Indicator Pic X (2) K. = Terminal Capture Indicator
      req.append(SYSTEMSEPARATOR);// A2 System Indicator Pic X (2) K. = Terminal Capture Indicator
      req.append(ROUTINGINDICATOR); // A3 Routing Indicator Pic X (6) A02000
      // the following code handles converting to integer, then back to string, so that formatting *should* be perfect ...
      // this is the client number + merchant number (4+12=16)
      req.appendNumeric(PaymentechAuth.CLIENTLEN+PaymentechAuth.MIDLENGTH,""+StringX.parseLong(merch.authmerchid)); // req.append("0002"); // A4 Client Number Pic 9 (4) Assigned by Paymentech//    req.append("700000000394"); // A5 Merchant Number Pic 9 (12) Assigned by Paymentech
      PTTid ptnctid = new PTTid(merch.authtermid);
      dbg.VERBOSE(ptnctid.spam());
      req.appendNumeric(3,""+ptnctid.tidint); //""+StringX.parseInt(merch.authtermid));// req.append("002"); // A6 Terminal Number Pic 9 (3) Assigned by Paymentech
      req.appendInt(1,1); // A7 Transaction Sequence Flag Pic 9 (1) 1 = Single transaction or last authorization, 2 = Multi transaction, not last authorization
      //one variation quotes "000"+3 digits, the other 6 digits in range 1..999
      dbg.VERBOSE("seqnum:"+tjr.authseq);
      req.appendNumeric(6,tjr.authseq); // A9 Sequence Number Pic 9 (3) 001 through 998, This is the sequence (reference) number of the transaction as it appears in the batch. Once 998 has been reached the sequence number must start over at 001.
      req.append(TRANSACTIONCLASS); // A10 Transaction Class Pic X (1) F = Financial Transaction
      req.appendInt(2, PTTransactionCodes.authFrom(tjr.paytype(), tjr.transfertype()).code());
      if(tjr.isReversal()){
        if(tjr.isGiftCard()){
          req.appendInt(8,PaymentechLastRRN.LASTretrievalReferenceNumber);//11 Last Retrieval Reference Number Pic 9 (8) This is the last retrieval reference number the host sent back in previous transaction.  +++ Still might be an issue with whether or not this is per-terminal or global.
          req.endFrame();//12 FS Pic X (1) Ö 1Ch
          //B MISCELLANEOUS INFORMATION
//  PTAuthTraceData authtracedata = new PTAuthTraceData(original.authtracedata);
//  dbg.ERROR("original.authtracedata="+original.authtracedata/*+", authtracedata.retrievalReferenceNumber="+authtracedata.retrievalReferenceNumber*/+", authtracedata="+authtracedata);
          req.appendNumeric(8,tjr.authrrn);//1 Retrieval Reference Number Pic X (8) Ö Must match the original transaction. This number is sent back by the host in every transaction response.
          req.endFrame();//2 FS Pic X (1) Ö 1Ch
          MSRData card = tjr.card();
          String cardholderaccount = card.accountNumber.Image();
          req.appendFrame(cardholderaccount);//3 Account Number Pic 9 (19) Ö Variable, Must match the original transaction
        } else if (tjr.isDebit()) {//gets done as a return
          //we depend upon the original amount already being copied into the current record
  //        pinCardMoney(tjr);
          //+++ generate an error @@@
        } else {
          //+++ this should not happen! we don't do Credit VOIDS with Paymentech.

        }
      } else {//sale info common to all payment modes, including GiftCard balance inquiry
        pinCardMoney(tjr);
      }

      switch (tjr.paytype().Value()) {
        case PayType.GiftCard: {
          appendGiftCardSpecificStuff(tjr);
        } break;
        case PayType.Debit: {
          appendDebitSpecificStuff(tjr);
        } break;
        case PayType.Credit: {
          appendCreditSpecificStuff(tjr, merch);
        } break;
      }
    }
    req.end();
    dbg.ERROR("isOk() returned " + req.isOk());
    return this;
  }

  public TextList toSpam(){
    return toSpam(this.req);
  }

  public TextList toSpam(VisaBuffer vb){
    UTFrequest utf= UTFrequest.From(vb);
    return utf.toSpam(null);
  }

////////////////////////////
// module tester
  public static final void main(String [] args) {
//    ErrorLogStream.stdLogging("PaymentechRequest.CertificationAndDebug");
//    EasyCursor ezp = new EasyCursor();
//    int txncount = 0;
//    if(args.length > 0) {
//      // args[0] is a filename ...
//      // +++ load the file
//      ezp = EasyCursor.FromDisk(args[0]);
//      // see how many txns there are ...
//      txncount = ezp.getInt("txncount");
//    } else {
//      dbg.ERROR("PaymentechRequest.main() arg[0] is the filename of a properties-to-file with the following structure ... [TBD]");
//      txncount = 1;
//      ezp.setString("txncount", ""+txncount);
//      ezp.push("0");
//      ezp.setString("authseq", "050");
//      ezp.setString("cardholderaccount", "6011000995500000");
//      ezp.setString("expirationdate", "0202");
//      ezp.setString("amount", "150");
//      ezp.setString("authmerchid", "0002700000000394");
//      ezp.setString("authtermid", "002");
//      ezp.setString("paytype", "CR");
//      ezp.setString("transfertype", "SA");
//      ezp.pop();
//      // +++ show an example by dumping to disk !!!
//    }
//    for(int txni = 0; txni < txncount; txni++) {
//      ezp.push(""+txni);
//      java.net.Socket s = null;
//      try {
//        TxnRow tjr = TxnRow.NewOne(ezp);
//        MerchantInfo merch = new MerchantInfo();
//        merch.authmerchid = ezp.getString("authmerchid");
//        merch.authtermid  = ezp.getString("authtermid");
//        PaymentechRequest req = new PaymentechRequest(false);
//        req.fromRequest(tjr, null, merch);
//        byte [] bytes = req.toBytes();
//        dbg.ERROR(req.toSpam().asParagraph("\n"));
//
//        String tosend = Ascii.bracket(bytes);
//        dbg.ERROR("Preparing to write: " + tosend);
//        s = new java.net.Socket("127.0.0.1"/*"208.237.46.199"*/, 12000);
//        dbg.ERROR("writing: [" + tosend + "]");
//        net.paymate.util.timer.StopWatch sw = new net.paymate.util.timer.StopWatch();
//        s.getOutputStream().write(bytes);
//        s.getOutputStream().flush();
//        dbg.ERROR("writing & flushing took " + sw.Stop() + " ms.");
//        int i = 0;
//        java.io.DataInputStream istream = new java.io.DataInputStream(s.getInputStream());
//        dbg.ERROR("reading: ...");
//        sw.Start();
//        VisaBuffer vb = VisaBuffer.NewReceiver(req.maxRequestSize());
//        while((i = istream.read()) != -1) {
//  // per beverly: on test server, can use a regular card, but it will not settle, just like maverick +++ document in pt docs
//          vb.append(i);
//          if(vb.isComplete()){ //you have a properly 'shaped' packet
//            dbg.ERROR("Rcvr is complete!");
//            break;
//          }
//        }
//        dbg.ERROR("reading took " + sw.Stop() + " ms.");
//        dbg.ERROR("read: " + Ascii.bracket(vb.packet()) );
//        PaymentechResponse resp = new PaymentechResponse(false);
//        resp.process(vb);
//        dbg.ERROR("response: \n" + resp.toSpam(null));
//      } catch (Exception e) {
//        dbg.ERROR("Exception: " + e);
//        dbg.Caught(e);
//      } finally {
//        try {
//          s.close();
//        } catch (Exception e2) {
//          // stub
//        }
//      }
//      ezp.pop();
//    }
//    LogFile.ExitAll();
  }

}
//$Id: PaymentechRequest.java,v 1.70 2004/04/15 04:31:14 mattm Exp $
