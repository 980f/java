package net.paymate.authorizer.paymentech;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/paymentech/PaymentechAuth.java,v $
 * Description:  Paymentech Authorization Service
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Revision: 1.73 $
 *
 * TODO:
 * +++ Until we have 99+ terminals using paymentech, use Socket-per-transaction.
 * They allow  up to 99 sockets [one per terminal].  No heartbeat.
 * +++ AFTER we have 99 terminals, we will have to rework it!
 */

import net.paymate.data.sinet.business.*;
import net.paymate.authorizer.*;
import net.paymate.database.ours.query.*;
import net.paymate.database.PayMateDB;
import net.paymate.connection.PaymentReply;
import net.paymate.util.*; // ErrorLogStream
import net.paymate.data.*; //Storeid
import net.paymate.awtx.RealMoney;
import net.paymate.connection.*; // ActionRequest
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import net.paymate.terminalClient.PosSocket.paymentech.*;

public class PaymentechAuth extends Authorizer implements PaymentechConstants {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(PaymentechAuth.class);
  public PaymentechAuth() {
    super();
  }

  // +++ this means that we need a separate authorizer for PTGateway customers
  private boolean hostCaptureOnly = false; // default for taco-like customers

  protected void loadProperties() {
    hostCaptureOnly = configger.getBooleanServiceParam(serviceName(), "hostCaptureOnly", false);
  }

  protected boolean processLocally(AuthTransaction authTran){//modifies flow of reversals
    return processLocally(authTran.record.transfertype(), authTran.record.paytype());
  }

  protected boolean processLocally(TransferType tt, PayType pt) {
    switch (tt.Value()) {//exclude these ones from going to the authorizer:
      case TransferType.Unknown:
        return true; // +++ what about host capture??
      case TransferType.Return:
      case TransferType.Reversal:
        return pt.is(PayType.Credit) && !hostCaptureOnly; // never locally void a host capture txn!
      default:
        return false;
    }
  }

  protected final AuthTerminalAgent genTermAgent(Terminalid termid) {
    return new VBAuthTermAgent(this, termid, bufferSize, sequencer(),
                               termbatchnum(termid), fgThreadPriority,
                               bgThreadPriority);
  }

  protected final GatewayTransaction genGWTransaction(byte[] bytes, String potentialGWTID) {
    return new PTGWTransaction(bytes, this, potentialGWTID);
  }

  protected final AuthTransaction genTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    return new PTTransaction(record, original, storeid, slim, merch, hostCaptureOnly, this);
  }

  public AuthSocketAgent genSubmittalAgent() {
    VisaBuffer vb = VisaBuffer.NewReceiver(bufferSize).setClipLRC(); // +++ @@@ check the max on this and use it !!!
    return new PTSubmittalSocketAgent(vb, this, hostCaptureOnly); // an extension of AuthSocketAgent
  }

  protected AuthSubmitTransaction genSubmitTxn(Authid authid, Terminalid terminalid, MerchantInfo merch) {
    AuthSubmitTransaction submittal = new AuthSubmitTransaction(); // +++ extend?
    submittal.request = new PTAuthSubmitRequest(authid, terminalid, merch, this);
    submittal.response = new PTAuthSubmitResponse();
    return submittal;
  }

  protected boolean accepts(TransferType tt) {
    switch(tt.Value()) {
      case TransferType.Authonly:
      case TransferType.Force:
      case TransferType.Modify:
      case TransferType.Query:
      case TransferType.Return:
      case TransferType.Reversal:
      case TransferType.Sale: {
        return true;
      } // break;
      case TransferType.Unknown:
      default:{
        return false;
      } // break;
    }
  }

  // +++ @@@ %%% Determine a better way to determine if a txn was sent off. [need an AuthTransaction table with request/reply pairs in it that can be queried for "DONE/SUCCESS/etc".]
  // +++ @@@ %%% Right now, we are setting authendtime even if the txn is not sent off (eg: CC voids) !!!!! BAD !!!!
  public int calculateTxnRate(TransferType tt, PayType pt, Institution in) {
    if((tt == null) || (pt == null) || hostCaptureOnly /* means gateway */) {
      return 200; // this is to cover gateways?
    }
    if((tt.is(TransferType.Reversal) && pt.is(PayType.Credit))
       || tt.is(TransferType.Force)
       || tt.is(TransferType.Modify)
       ) {
      return 0; // none of these are sent off
    }
    return 200; // 2 cents
  }

  public int calculateSubmittalRate() {
    return 200; // +++ no rates set yet, and no place to get them (what things do we NOT send off?)
  }

  /////////////////////////////////
  // Here down is shared by PaymentechRequest and PTAuthSubmitRequest -- when get time, can put into base of the two (maybe).

  /* package */ final byte [] addHeader(byte [] packet) {
    int length = packet.length;
    if((length+HEADERLENGTH) > bufferSize) {
      dbg.ERROR("toBytes(): Message length exceeds maximum length supported by the protocol!  Message will be truncated!");
      length = bufferSize;
    }
    //wordyOutputstream fed by ByteFifo does the following quite nicely. gotta make them easier to use.
    byte [] bytes = new byte[HEADERLENGTH+length];
    bytes[0] = (byte)(length >>8); // the upper byte of the integer
    bytes[1] = (byte)(length &255); // the lower byte of the integer
    for(int i = HEADERLENGTH; i-->2; ) {
      bytes[i] = 0; // this just fills the filler part of the header
    }
    // copy the body of the message into the remainder of the array
    System.arraycopy(packet, 0, bytes, HEADERLENGTH, length);
    return bytes;  // done
  }

  /* package */ final byte [] addHeaderAndSTXETX(byte [] packet) {
    int length = packet.length;
    int asciiLength = length+2; // for STX and ETX
    int fulllength = asciiLength+HEADERLENGTH;
    if(fulllength > bufferSize) {
      dbg.ERROR("toBytes(): Message length exceeds maximum length supported by the protocol!  Message will be truncated!");
      length = bufferSize;
    }
    //wordyOutputstream fed by ByteFifo does the following quite nicely. gotta make them easier to use.
    byte [] bytes = new byte[fulllength];
    // deal with the header
    bytes[0] = (byte)(asciiLength >>8); // the upper byte of the integer
    bytes[1] = (byte)(asciiLength &255); // the lower byte of the integer
    // 4 bytes of zero reserved
//    for(int i = HEADERLENGTH; i-->2; ) {
//      bytes[where++] = 0; // this just fills the filler part of the header
//    }
    // write STX+message+ETX
    bytes[HEADERLENGTH] = Ascii.STX;
    // copy the body of the message into the remainder of the array
    System.arraycopy(packet, 0, bytes, HEADERLENGTH+1, length);
    bytes[fulllength-1] = Ascii.ETX;
    return bytes;  // done
  }

  /**
   * most money cannot be zero. if zero just leave the frame empty
   */
  /* package */ static final void appendMoney(VisaBuffer req, RealMoney rawamount) {
    if(rawamount.Value()!=0){
      appendMoneyZok(req, rawamount);
    } else {
      req.endFrame();
    }
  }
  /* package */ static final void appendMoney(AsciiBuffer req, RealMoney rawamount) {
    if(rawamount.Value()!=0){
      appendMoneyZok(req, rawamount);
    } else {
      req.endFrame();
    }
  }

  /**
   * @todo look into using VisaBuffer.appendWithDecimal(cents,4,2)
   */
  /* package */ static final void appendTotalMoney(AsciiBuffer req, long cents) {
    LedgerValue rawamount = new LedgerValue(MONEYIMAGE).setto(cents);
    String amt = rawamount.Image();
    dbg.ERROR("setting amt to " + amt);
    req.appendFrame(amt);
  }

  /**
   * for the ONE instnace where 0.00 is allowed:
   * @todo use VisaBuffer.appendWithDecimal(,4,2);
   */
  /* package */ static final void appendMoneyZok(VisaBuffer req, RealMoney rawamount){
    req.appendFrame(rawamount.Image(MONEYIMAGE));
  }
  /* package */ static final void appendMoneyZok(AsciiBuffer req, RealMoney rawamount){
    req.appendFrame(rawamount.Image(MONEYIMAGE));
  }

  /* package */ static final void industrySpecificData(VisaBuffer req, TxnRow record, MerchantInfo mi, int termbatchnum, int authseq) { // 25 - 28
    AsciiBuffer buf = AsciiBuffer.Newx(30);
    industrySpecificData(buf, record, mi, termbatchnum, authseq);
    req.append(buf.packet());
  }

  /* package */ static final void industrySpecificData(AsciiBuffer req, TxnRow record, MerchantInfo mi, int termbatchnum, int authseq) { // 25 - 28
    switch(mi.type.Value()) {
      case MerchantType.Restaurant: {
        req.appendSigned(3, 2); // F1 Industry Code Pic 9 (3) 002 = Restaurant

        // don't know the batch number for auth, but do for submission
//        req.appendInt(6, 0 + 0); // F2 Reference Code Pic 9 (6) batchLast3+txnReferenceNumberLast3
        req.appendSigned(3,termbatchnum%1000);
        req.appendSigned(3,authseq%1000);

        req.appendSigned(2, 2); // F3 Charge Description 02=Food & Beverage
//        long samt = record.rawSettleAmount().absValue();
//        long aamt = record.rawAuthAmount().absValue();

//        req.appendWithDecimalPoint(record.probablyTipAmount().Value(), 8, 2); // F4 tip Pic X(8), 00000.00 minimum
        int decimals = 2;
        int width = 8;
        int num = (int) record.probablyTipAmount().Value();
        int base = IntegralPower.raise(10, decimals).power;
        // +++ @todo use width to determine if we should use int or long
        req.appendSigned(width-1-decimals,(int)(num)/base);//len-2 chars from left of string
        req.append(".");
        req.appendSigned(decimals,(int)(num)%base);//last two chars of string#2== "0.".length

        req.appendSigned(8, Math.max(record.associateid().value(), 1)); // F5 Server# Pic 9(8) Zero fill, right justify, CANNOT be zero for Amex txns!
      } break;
      default: // don't know what to do otherwise!
      case MerchantType.Retail: {
        req.appendSigned(3, 4); // F1 Industry Code Pic 9 (3) 004 = Terminal Capture Retail
        req.appendSigned(6, 0); // F2 Invoice Number Pic 9 (6) Zero fill, right justify
        req.appendSigned(20, 0); // F3 Item Code Pic 9 (20) This field can be hard coded to all Zero's.
      } break;
    }
  }

  // we do forces with PT:
  protected PaymentReply doForce(PayMateDB db, TxnRow record) {
    try {
      AuthTerminalAgent agent = getAgent(record.terminalid());
      agent.setNextSequence(db, record);
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return doneLocally(db, record, AuthResponse.mkApproved(record.approvalcode));
    }
  }

  protected void logGatewayTransaction(PayMateDB db, AuthAttempt attempt) {
    if(EasyUrlString.NonTrivial(attempt.authrequest)) {
      byte [] gwreqbytes = attempt.authrequest.rawValue().getBytes();
      byte [] gwrspbytes = attempt.authresponse.rawValue().getBytes();
      Terminalid terminalid = attempt.terminalid;
      // here is where we do different things based on what kind of gw msg it is
      if(ByteArray.NonTrivial(gwreqbytes)) {
        PaymentechUTFormatter ptf = new PaymentechUTFormatter();
        ActionRequest ar = ptf.requestFrom(gwreqbytes);
        ar.setTerminalInfo(terminalid);
        if(ar != null) {
          switch(ar.Type().Value()) {
            case ActionType.store: // currently just issuing a deposit
            case ActionType.batch: { // probably just printing a batch
              BatchRequest br = (BatchRequest)ar;
              // make sure the parameters are correct ...
              db.closeDrawer(terminalid, null, false /*auto*/); // close the drawer
              // since this authorizer should be configured for host capture only, submitting is a snap (won't send it off) ...
              // should we check the success of gwrspbytes first?  Nah.  for now ...
              this.submit(db, terminalid, false /*auto*/); // +++ check the return value of this function ???
            } break;
            case ActionType.payment: {
              PaymentRequest request = (PaymentRequest)ar;
              Storeid storeid = db.getStoreForTerminal(terminalid);

              VisaBuffer vb = VisaBuffer.FrameThis(gwrspbytes).setClipLRC();

              PaymentechResponse pr = PaymentechResponse.From(vb, true /*fullHC*/);
              ActionReply artemp = PaymentReply.For(request);
              PaymentReply freply = null;
              if(artemp instanceof PaymentReply) { // +++ where is the isfinancial function?
                freply = (PaymentReply) artemp;
              } else { // then try to fake it
                freply = new PaymentReply();
              }
              TxnReference tref = request.TxnReference();
              tref.setReferenceTime(UTC.Now()); // +_+ review if this is a bad time to set the time
              freply.setAuth(pr);
              freply.setReference(tref);
              // !!! NOTE: The above line MUST GENERATE SOME ActionReply,
              // even if it is one that is nothing but FAILURE,
              // else following code will croak ...
              TerminalInfo tinfo = db.getTerminalInfo(terminalid);
              // configure the TxnRow
              TxnRow record = new TxnRow();
              if(request.isVoider()) {
                // find the original txn using the stan + current drawer
                dbg.ERROR(" reversal request=\n"+request);
              //  ReversalRequest rr = (ReversalRequest)request;
                Txnid origtxnid = db.getTxnidFromAuthrrn(terminalid, request.stan2modify().toString());
                // stamp the new void with the original's txnid
                if(Txnid.isValid(origtxnid)) {
                  record.origtxnid = origtxnid.toString();
                  // if we got an approval on this void, try to void the original
                  if(pr.isApproved()) {
                    db.setVoidFlag(origtxnid, true); // +++ check result?
                  }
                } else {
                  dbg.ERROR("Unable to find original txnid for gateway! authattemptid="+attempt.id);
                }
              }
              record.voided = Bool.FALSE();
              record.setClientReferences(tref);
              record.setSaleInfo(request.sale);
              if(request.hasSomeCardInfo()) {
                record.setCard(request.card);
              }
              record.associateid = db.Safeid(null);
              db.getPaymentTypeFromCardNo(record);/// will probably set to UK/UK
              MerchantInfo merch = new MerchantInfo(); // throw away
              db.getAuthInfo(storeid, record, merch); // will probably fall back to default, which will work
              record.authattempt = attempt;
              record.incorporate(freply.auth());
              record.tranendtime=record.authstarttime=record.transtarttime=record.authendtime = db.Now();
              record.authseq = record.authrrn = record.stan = pr.authrrn();
              record.setStoodin(false);
              record.setCPSdata(pr.getCPSdata());
              record.authid = record.settleid = id.toString(); // set them absolutely to MY authid
              record.setSettle(!record.modifiesTxn());
              record.setSettleop(record.transfertype());
              // do the database work
              Txnid txnid = db.startTxn(record);  //db record actually created here.
              db.stampAuthAttemptTxnidOnly(attempt.id, txnid);
              // +++ check the txnid to see if it is valid.  if not, BITCH (PANIC)
            } break;
            default : {
              // sigh.  i'm sick and too tired to deal with this ...
              // this whole function is a hack
              // move all of this code, plus the code in authorizer,
              // plus the code in authmgr
              // into a place where it can be called when needed
              dbg.WARNING("Ignoring unmanaged action type " + ar.Type() + " for authattempt " + attempt);
            }
          }
        } else {
          dbg.ERROR("Could not parse the GatewayRequest for authattempt "+attempt.id+"!"); // +++ PANIC ???
        }
      }
    } else {
      // don't know what to do, so do nothing; should never happen
      dbg.ERROR("Processing GatewayTransaction with no request! "+attempt.id); // +++ PANIC ???
    }
  }

  public static final int MIDLENGTH = 12;
  public static final int CLIENTLEN = 4;
}
// $Id: PaymentechAuth.java,v 1.73 2004/04/15 04:31:13 mattm Exp $
