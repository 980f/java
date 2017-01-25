package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthManager.java,v $
 * Description:  Picks which auth to use, puts the txn into the tables, and passes the request
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.23 $
 */

/* an authorizer manager that handles all authorizers,
 * decides which ones to use, etc.
 * maybe combine with the list object? +++
 */

/*
8.  The AuthMgr handles retries & original txns by finding the appropriate authorizer and handing it the txn.
9.  If no Authorizer is found, the txn is handed to the NullAuth, which declines it by default (no authorizer found).
10. If the Authorizer is found, the txn is passed off to it for processing.
11. The response from the Authorizer is then converted into the HTTP format and returned back to the client which sent it.
*/

import net.paymate.database.*; // db
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.*; // ErrorLogStream
import net.paymate.connection.*; // FinancialRequest
import net.paymate.web.*; // UserSession
import net.paymate.ISO8583.data.*; // TransactionID +++ fix this; put into net.paymate.data ?
import net.paymate.jpos.data.*; // MSRData & MICRData +++ fix this; put into net.paymate.data ?
import net.paymate.authorizer.cardSystems.*;
import net.paymate.net.*;
import java.util.*;
import java.io.*;

public class AuthManager {
  private static final ErrorLogStream dbg = new ErrorLogStream(AuthManager.class.getName());
  private AuthorizerList list = new AuthorizerList();
  private PayMateDB db = null;
  private String hostname = "uninitialized";
  private PrintStream backup = null;
  private Authorizer nullAuthorizer = null; // hehe
  private boolean up = true;
  private SendMail mailer = null;

  public AuthManager(PayMateDB db, String hostname, PrintStream backup, SendMail mailer, boolean preloadAll) {
    this.db = db;
    this.hostname = hostname;
    this.backup = backup;
    this.mailer = mailer;
    // load the authorizers
    if(preloadAll) {
      String [] authids = db.getAuthIds(); // +++ maybe have it return "int []" instead of "String []"?
      for(int i = authids.length; i-->0;) {
        Authorizer tempAuth = prepAuth(Integer.valueOf(authids[i]).intValue());
        if((tempAuth != null) && (tempAuth instanceof NullAuthorizer)) {
          nullAuthorizer = tempAuth;
          Authorizer.nullauthorizer = nullAuthorizer; // in case the others need to use it, too, which they sometimes do
        }
      }
    } else {
      prepAuth(db.getAuthId("PAYMATE"));
    }
  }

  public Authorizer [] listAsArray() {
    return list.getArray();
  }

  public boolean bringup() {
    up = true;
    // +++ bring up the loaded authorizers?
    return up;
  }

  public boolean shutdown() {
    up = false;
    for(int i = list.size(); i-->0;) {
      Authorizer auth = (Authorizer) list.elementAt(i);
      auth.shutdown();
    }
    return !up;
  }

  // The ConnectionServer gets the auth name for a store when a ConnectionRequest comes in from one of its appliances
  // It then tries to start the auther for it.
  // If the auther is already started, this does nothing.
  Monitor authStarter = new Monitor(AuthManager.class.getName()+"AuthStarter"); // prevents auths from getting loaded more than once.
  public void prepAuther(String applianceId) {
    prepAuth(getAuthIdForAppliance(applianceId));
  }

  public Authorizer prepAuth(int authid) {
    Authorizer auth = null;
    try {
      authStarter.getMonitor();
      if(findAuthById(authid) == null) {
        // not started
        if(up) {
          // get the authclass from authorizer & instantiate one
          String authname  = db.getAuthName(authid);
          String authclass = db.getAuthClass(authid);
          Object o = Safe.loadClass(authclass);
          // then, init it
          if(o == null) {
            // +++ bitch
          } else {
            if(!(o instanceof Authorizer)) {
              // +++ bitch
            } else {
              auth = (Authorizer)o;
              auth.init(authid, authname, db, hostname, backup, mailer);
              list.add(auth);
            }
          }
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      authStarter.freeMonitor();
      return auth;
    }
  }

  // finding authorizers ...

  private int getAuthIdForAppliance(String applianceId) {
    return db.getAuthIdForApplianceName(applianceId);
  }

  private Authorizer getAuthForStore(int storeid) {
    int authid = db.getAuthIdForStore(storeid);
    return findAuthById(authid);
  }

  private Authorizer getAuthForAppliance(String applianceId) {
    int authname = getAuthIdForAppliance(applianceId);
    return findAuthById(authname);
  }

  public Authorizer findAuthByName(String authName) {
    return list.findByName(authName);
  }

  public Authorizer findAuthById(int authid) {
    return list.findById(authid);
  }

  // handling txns ...

//  public ActionReply doFinancial(FinancialRequest request, UserSession session) {
//    return doFinancial(request, session, null, null);
//  }

  public static final TransactionType CR = new TransactionType(TransactionType.CR);
  public static final TransactionType DB = new TransactionType(TransactionType.DB);
  public static final TransactionType CK = new TransactionType(TransactionType.CK);

  public ActionReply doFinancial(FinancialRequest request, UserSession session, FinancialReply reply, ReversalReply voidme) {
    ActionReply ar= ActionReply.For(request);
    dbg.Enter("doFinancial");
    try {
      // first, make sure the authorizeris loaded for this appliance
      prepAuther(request.applianceId);

      // Is it a valid request?
      int storeid = session.linfo.storeid;
      if(storeid < 1) {
        dbg.WARNING("Could not get store from UserSession.linfo.");
        ar.setState(ActionReplyStatus.InvalidTerminal);
        ar.Errors.add("DatabaseError: could not find store from terminalID");
        return ar;
      }

      boolean wasStoodin = (reply != null);
      boolean wasVoided = (voidme != null);
      boolean wasDeclined = (wasStoodin ? (!reply.Succeeded()) : false);
      boolean needsAuth = true;

      // See if this is a duplicate transaction.
      ar=doRetry(request, session);
      if(ActionReply.Successful(ar)){
        return ar;
      }
      if (ar==null){
        ar= ActionReply.For(request);
      }

      // create a Tranjour object and insert it into tranjour, +++ retreiving the txnid. MUTEX?!?
      TransactionID tid = db.newtransaction(storeid,request.requestInitiationTime);
      if(!tid.isComplete()) {
        dbg.WARNING("Could not getNextStanForStore(\"" + storeid + "\").");
        ar.setState(ActionReplyStatus.DatabaseQueryError);
        ar.Errors.add("DatabaseQueryError: could not find STAN from storeid");
        return ar;
      }

      // for a void, get the original txn info first
      TxnRow original = null;
      if(request.isReversal()) {
        original = db.getTranjourRecordfromTID(((ReversalRequest)request).toBeReversed, request.terminalId); // +++ also try to get it from history if it isn't in tranjour!
        if(original == null) {
          // @@@ need to log it +++
          ar.setState(true);
          ar.setResponse("M0");
        } else {
          // +++ check to see if it was ALREADY voided (in other words, is it voidable?)
          // put card info into ar. %%% untested attempt to make void receipts contain original txn data.
          ReversalReply pun=(ReversalReply)ar;
          pun.card=new MSRData(original.card());
          pun.originalAmount=original.rawamount();
        }
      }

      TxnRow record = new TxnRow(); // get authtermid from session.linfo.ti.authid(),

      Authorizer auth = null;
      // is it a standin that is already voided or was it declined?
      if((wasStoodin && wasVoided) || wasDeclined) {
        // just log it and don't authorize it +++ @@@
        // log it with PAYMATE as the authorizer?
        needsAuth = false;
      } else {
        // Who is the auther (simulating == we are the auth)?
        auth = getAuthForStore(session.linfo.storeid); // +++ will eventually have to give it more info; like txn info
      }
      // if no auther available for this store + paytype, decline it
      if(auth == null) {
        dbg.ERROR("Could not find authorizer to use!");
        auth = nullAuthorizer;
      }

      // fill the fields and insert into tranjour
      record.actioncode = null; // these are all null ... just to be sure
      //record.authendtime = null;
      record.authidresponse = null;
      record.authid = ""+auth.id;
      //record.authstarttime = db.forTrantime(Safe.Now());
      TerminalInfo tinfo = db.getTerminalInfo(Safe.parseInt(request.terminalId));
      record.authtermid = session.linfo.authTermId;
      record.storeid = ""+tid.caid;
      record.cardacceptortermid = session.linfo.terminalName;
      if(original != null) {
        record.messagetype = "0400";
        record.cardholderaccount = original.cardholderaccount;
        record.expirationdate = original.expirationdate;
        record.track1data = original.track1data;
        record.track2data = original.track2data;
        record.transactionamount = original.transactionamount;
        record.processingcode = original.processingcode;
        record.transactiontype = original.transactiontype;
        record.stan = original.stan;
      } else {
        if(request instanceof CardRequest) {
          CardRequest creq = (CardRequest)request;
          record.cardholderaccount = creq.card.accountNumber.Image();
          record.expirationdate = creq.card.expirationDate.YYmm();
          record.track1data = creq.card.track(0).Data();
          record.track2data = creq.card.track(1).Data();
        }
        record.transactionamount = request.Amount().dollars();
        record.originalstan = null;
        switch(request.Type().Value()) {
          case ActionType.check: {
            // sale
            record.transactiontype = CK.Image();
            record.processingcode = "040000";
            record.messagetype = "0100";
          } break;
          case ActionType.debit: {
            record.messagetype = "0200";
            record.transactiontype = DB.Image();
            record.processingcode = (request.isReturn()) ? "200000" : "000000";
          } break;
          default: /* although this really ought to blow */
            dbg.ERROR("Unknown actiontype in Financial Request processing: " + request.Type().Image());
          case ActionType.credit: {
            record.messagetype = "0200";
            record.transactiontype = CR.Image();
            record.processingcode = (request.isReturn()) ? "200030" : "003000";
          } break;
        }
      }
      record.hostresponsecode = null;
      record.hosttracedata = null;
String sistan = (!wasStoodin? null :  ActionReply.Stan(reply)); // STOODINSTAN (generate one for server-side here?) +++
dbg.ERROR("Setting STOODINSTAN for wasStoodin="+wasStoodin+" to " + sistan);
      record.stoodinstan = sistan;
//String clientRefTime = (!wasStoodin? null :  reply.tid.time);
String clientRefTime = reply.tid.time; // CLIENTREFTIME (is this right?) +++
dbg.ERROR("Setting CLIENTREFTIME for wasStoodin="+wasStoodin+" to " + clientRefTime);
      record.clientreftime = clientRefTime;
      record.responsecode = null;
      record.stan = tid.stan();// db.newtransaction(session.linfo.storeid).stan();
      record.tranendtime = null;
      record.transtarttime = tid.time;// db.forTrantime(Safe.Now());
      record.voidtransaction = "N";
      // last stuff
      record.paymenttypecode = db.getPaymentTypeFromCardNo(record); // can do this since we already set values in it
      if(!Safe.NonTrivial(record.paymenttypecode)) {
        // ERROR ?!?!  We can't handle this card type!
        dbg.ERROR("Card type not found for card ["+record.cardholderaccount+"]!");
        // going to let it go ahead and try for now, though +++ fix later.
      }
      auth.setNextSequence(record); // instead of  "record.authseq = ..."
      // now insert the record
      int rows = db.startTxn(record);
      if(rows != 1) {
        // then, check the return value and if it is bad (not 1), then barf
        dbg.ERROR("ERROR inserting a new record into tranjour !!!! Follows: "+record.toString());
        ar.setState(ActionReplyStatus.DatabaseQueryError);
        ar.Errors.add("DatabaseQueryError: could not insert record");
      } else {
        // send it to the auther; sleep or wait happens in here
        if(needsAuth) {
          ar = auth.authorize(record, original, wasStoodin); //what if this is a web void of a different txn?  Need to use the original txns terminal info.
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return ar;
    }
  }

  /**
  * try to find a recent record that matches a retried financial request
  * @return the reply if one is found.
  */
  private final ActionReply doRetry(FinancialRequest request, UserSession session){
    dbg.Enter("doRetry");
    try {
      TxnRow tj=session.db.getTransactionForRetry(session.linfo, request, true);
      if(tj!=null){ // now that we have the latest record compare it to the request
        dbg.VERBOSE("got a match");
        if(request instanceof CardRequest){
          // check to see if we failed to talk to authorizer
          if(tj.neverAuthed()) {
            // @@@ check to see if stoodin
            // if so, reply with stoodin info @@@
            // if not, reply with ServerFailuer & put it into the problems log @@@
          }
          CardRequest cr = (CardRequest) request;
          if(cr.card.equals(tj.card())){//query is missing some fields...
            //...query will accept same card with different expiration dates.
            return card4Tranjour(tj,cr.card,session);
          }
          dbg.WARNING("imperfect card match");
        } else if(request instanceof CheckRequest){
          CheckRequest cr=(CheckRequest)request;
          return check4Tranjour(tj,cr.check,session);///later...
        }
      }
      return null;
    }
    finally {
      dbg.Exit();
    }
  }

  // next 3 *Tranjour functions needed by doRetry() ...

  /**
  * @return a CardReply extracted from a tranjour record.
  */
  private final ActionReply card4Tranjour(TxnRow tj, MSRData card, UserSession session){
    //was it debit or credit? 'til we have debit presume credit
    CardReply reply = new CardReply();
    insertFinancialsFromTranjour(tj,reply);
    return reply;
  }

  /**
  * @return  a CheckReply extracted from a tranjour record.
  */
  private final ActionReply check4Tranjour(TxnRow tj, MICRData check, UserSession session){
    CheckReply reply=new CheckReply();
    reply.ManagerOverrideData="";//field 116. some compendium that ensures the resent request is allowed to exceed limits.
    insertFinancialsFromTranjour(tj,reply);
    return reply;
  }

  private final void insertFinancialsFromTranjour(TxnRow tj,FinancialReply frep){
    //    ezp.setString(FinancialReply.ApprovalKey,tj.authidresponse);
    frep.tid= tj.tid();
    frep.setApproval(tj.authidresponse);
    frep.setResponse(tj.responsecode);
    frep.CATermID=tj.cardacceptortermid;
    frep.refTime= tj.refTime();
    //successful PROCESS regardless of approval.
    frep.setState(ActionReplyStatus.Success);//+_+ checkserver name and maybe say successfullyfaked
  }

}

