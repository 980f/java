package net.paymate.connection;

/**
 * Title:        $Source: /cvs/src/net/paymate/connection/Transaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: Transaction.java,v 1.6 2001/11/17 06:16:58 mattm Exp $
 */

// handles a transaction.  Very much like TxnRow, with which it will eventually merge. +++

/*
//authorizername='MAVERICK' // in the future this will be set elsewhere
//batchnumber='' // to be set to a real number when the batch is closed
//storeid // using old id's until we get integer ones
//cardacceptortermid // from terminal.terminalname, until we redo the tables.
//cardholderaccount
//cashbackamount=0.00 // we are not doing this right now
//demomodeindicator='N' // we will probably scrap this field, or might do something "cooler" now
//employeenumber='PMSERVER001' // always
//expirationdate
//logtodisktimer // stamp it with NOW, I guess
//messagetype // 0100, 0200, 0400
//stoodinstan // used in standins; this would be the sistan, put it here
//modifyemployee // used in standins; this would be the swipetime, put it here
//originalstan // used with voids
//paymenttypecode // VS, MC, AE, etc.
//posconditioncode
//posentrymode
//processingcode // used to differentiate sales from returns, etc. (?)
//retrievalrefno
//stan // not our new ones, and not our standins
//track1data
//track2data
//transactionamount
//transactiondate // still used for possible dup detection
//transactiontime // and use this to be consistent
//transactiontype
//transmissiontime // might be used for voids
//transtarttime
//voidtransaction // whether a txn is voided
*/

import net.paymate.data.TerminalInfo;
import  net.paymate.database.*;
import  net.paymate.database.ours.query.*; // Tranjour
import  net.paymate.web.UserSession;
import  net.paymate.util.*;
import  java.util.Date;
import  net.paymate.ISO8583.data.*;
import  net.paymate.data.*;
import  net.paymate.jpos.data.*;
import  net.paymate.awtx.*;

public class Transaction extends TxnRow {
  private static final ErrorLogStream dbg = new ErrorLogStream(Transaction.class.getName());

  private UserSession session = null;
  private TransactionID rrn = null;
  private TerminalInfo termInfo = null;
  private String catermid = null;
  private Date now = Safe.Now();
  // for easy query generation (but takes more time and ram; however we are simulating anyway)
  private static final String rejected = "ReJeCtEd";

  public Transaction(UserSession session, TransactionID rrn,  TerminalInfo termInfo) {
    super();
    this.session = session;
    this.rrn = rrn;
    this.termInfo = termInfo;
    clearAll();
  }

  //+++ the full form exists under BinTable or some such name
  private static final String pmtTypeCodeCC(String number) {
    String ptc = "CR"; // whatever
    try {
      switch(number.charAt(0)) {
        case '3': {
          ptc = "AM";
        } break;
        case '4': {
          ptc = "VS";
        } break;
        case '5': {
          ptc = "MC";
        } break;
        case '6': {
          ptc = "DS";
        } break;
      }
    } catch (Exception e) {
      // --- who cares?
    }
    return ptc;
  }

  public int insertInto() {
    //return session.db.update(QueryString.Insert("tranjour", fields, values));
    return session.db.update(QueryString.Insert("tranjour", toProperties()));
  }

  /**
   * create a qurey to insert a financial record
   */
  public void genDummyQuery(FinancialRequest theRequest, String stanApproval,
      String messageType, String paymenttypecode, String transactiontype) {
    String amount = theRequest.Amount().Image("#0.00");
    String wideWhen = rrn.time;
    processingcode=PayMateDB.ProcessingCode(theRequest).Image();
    transactiontype=transactiontype;
    paymenttypecode=paymenttypecode;
    messagetype=messageType;
    storeid=""+rrn.caid;
    cardacceptortermid=termInfo.getNickName();
//    transmissiontime=rrn.noyear();
//    transactiondate=rrn.justdate();
//    transactiontime=rrn.justtime();
//    employeenumber="SIMULATOR001";
    stan=rrn.stan();
    transactionamount=amount;
    authidresponse=stanApproval;
//    posconditioncode="00";
//    retrievalrefno=rrn.RRN();
    voidtransaction="N";
    responsecode="00";
    actioncode="A";
    transtarttime=wideWhen;//utc#
    tranendtime=wideWhen;
//--separate query!    fields.add(session.db.CLIENTREFTIME);                 values.add(wideWhen);
    //add(stand-in indicator=Y);//successfully stoodin
  }

  private static final String successApprovalSICode(int stan) {
    // approval response is 6 wide:
    Fstring approval = new Fstring(6,'0');
    approval.righted(""+(stan+800000));  // set the approval code to the stan (until further notice)
    return approval.toString();
  }

  public ActionReply sim(CreditRequest theRequest){
    //for now we approve all
    String approval = successApprovalSICode(rrn.stanValue());
    // add any special fields early
    genDummyQuery(theRequest, approval, "0200",
        pmtTypeCodeCC(theRequest.card.accountNumber.Image()), "CR");
    cardholderaccount=theRequest.card.accountNumber.Image();
    expirationdate=theRequest.card.expirationDate.YYmm();
    if(theRequest.card.Cleanup()){
      track1data=theRequest.card.track(0).Data();
      track2data=theRequest.card.track(1).Data();
    }
    // then run the query
    if(insertInto()>0) {
      return (new CreditReply()).simulate("00", approval, ""+termInfo.id(), rrn);
    } else {
      // +++ post as error
      return (new CreditReply()).simulate("05", rejected, ""+termInfo.id(), rrn);
    }
  }

  public ActionReply sim(CheckRequest theRequest){
    // +++ do some tests first (for now we approve all)
    String approval = successApprovalSICode(rrn.stanValue());
    // add any special fields early
    // +++ put this next line in the CheckRequest class?
    // bear in mind that this will come at the END of some stuff:
    genDummyQuery(theRequest, approval, "0100", "C1", "CK");
    cardholderaccount=theRequest.check.Transit+theRequest.check.Account+theRequest.check.Serial;
    if(theRequest.checkId.isPresent()){
      if(DriversLicense.NonTrivial(theRequest.checkId.license)){
        track2data = theRequest.checkId.license.Image();
      }
    }
    // then run the query
    if(insertInto() > 0) {
      return (new CheckReply()).simulate("00", approval, ""+termInfo.id(), rrn);
    } else {
      // +++ post as error
      return (new CheckReply()).simulate("05", rejected, ""+termInfo.id(), rrn);
    }
  }

  public ActionReply sim(DebitRequest theRequest){
    // +_+ do this when we need to demo debit (just a hack now; always returns "Bank Not Supported")
    return (new DebitReply()).simulate("92", rejected, ""+termInfo.id(), rrn);
  }

  public ActionReply sim(ReversalRequest theRequest){
    ReversalReply reply = null;
    String bad = "25";
    // can only reverse tranjour records
    TxnRow rec = session.db.getTranjourRecordfromTID(((ReversalRequest)theRequest).toBeReversed, theRequest.terminalId);
    // +++ check to see that the transaction is voidable +++
    reply = new ReversalReply();
    if((rec == null) || rec.voidtransaction.equals("Y")) {
      return reply.simulate(bad, rejected, ""+termInfo.id(), rrn);
    } else {
// +++ do some tests first
// first, generate the void (+++ do this later)
      // then, void the transaction (tranjour.voidtransaction= "Y")
      String respCode = "00";
      String approval = null; // also good; sets it to stan
      QueryString qs = QueryString.
        Update("tranjour").
        SetJust("voidtransaction", "Y").
        where(). nvPair("storeid", rec.storeid).
        and().   nvPair("stan", rec.stan);
      if(session.db.update(qs) > 0) {
        // good
      } else {
        respCode = bad;
        approval = rejected; // leaves it blank
      }
      MSRData card=new MSRData();
      card.accountNumber = new CardNumber(rec.cardholderaccount);
      card.expirationDate = new ExpirationDate(rec.expirationdate);
      card.person = new Person(); // blah
      RealMoney originalAmount=new RealMoney(rec.transactionamount);
      return reply.simulate(respCode, approval, ""+termInfo.id(), rrn, card, originalAmount);
    }
  }

  public ActionReply process(ActionRequest request) {
    ActionReply reply = null;
    TextList msgs = new TextList(2);
    try {
      dbg.Enter("process");
      if(request == null) {
        dbg.ERROR("Someone attempted to process a null request!");
      } else {
        try{
          switch(request.Type().Value()){
//            case ActionType.check        :reply = sim((CheckRequest)    request); break;
            case ActionType.credit       :reply = sim((CreditRequest)   request); break;
//            case ActionType.debit        :reply = sim((DebitRequest)    request); break;
            case ActionType.reversal     :reply = sim((ReversalRequest) request); break;
          }
        } catch (Exception e) {
          String msg = "Exception occurred attempting to process a financial transaction.";
          dbg.ERROR(msg);
          dbg.Caught(e);
          msgs.add(msg + "-" + e);
        }
      }
    } catch (Exception e) {
      String msg = "Exception attempting to simulate reply: ";
      msgs.add(msg + e);
      dbg.ERROR(msg);
    } finally {
      if(reply == null) {
        reply = new ActionReply(ActionReplyStatus.ServerError);
      }
      reply.Errors.appendMore(msgs);
      dbg.Exit();
    }
    return reply;
  }

}

//$Id: Transaction.java,v 1.6 2001/11/17 06:16:58 mattm Exp $
