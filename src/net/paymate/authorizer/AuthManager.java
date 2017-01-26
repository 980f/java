package net.paymate.authorizer;

/**
* Title:        $Source: /cvs/src/net/paymate/authorizer/AuthManager.java,v $
* Description:  Picks which auth to use, puts the txn into the tables, and passes the request
* Copyright:    Copyright (c) 2001
* Company:      PayMate.net
* @author PayMate.net
* @version $Revision: 1.156 $
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
import net.paymate.util.timer.*; // stopwatch
import net.paymate.connection.*; // PaymentRequest
import net.paymate.data.*; // id's
import net.paymate.awtx.*; //misplaced data classes
import net.paymate.jpos.data.*; // MSRData & MICRData +++ fix this; put into net.paymate.data ?
import net.paymate.net.*;
import net.paymate.lang.Bool;
import net.paymate.lang.StringX;
import java.util.*;
import java.io.*;
import net.paymate.lang.ReflectX;
import net.paymate.lang.ObjectX;
import net.paymate.data.sinet.business.*;

public class AuthManager extends Service {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthManager.class);
  private AuthorizerList list = new AuthorizerList();
  private PayMateDBDispenser dbd = null;

  // service stuff
  public boolean isUp() {
    return true; // +++
  }
  public void down() {
    // +++ shutdown all the authorizers and unload them ???
  }
  public void up() {
    // load ones that aren't loaded yet (new ones, or try to load ones that failed again) +++ @@@ %%%
  }

  // number of authorizer modules
  public String svcCnxns() {
    return ""+list.size();
  }
  // Txns completed/attempted
  public String svcTxns() {
    return of(completed.value(), attempted.value());
  }
  // attempted - completed
  public String svcPend() {
    return ""+(attempted.value()-completed.value());
  }
  // avg txn time
  public String svcAvgTime() {
    return DateX.millisToSecsPlus(times.getAverage());
  }
  private final Counter completed = new Counter();
  private final Counter attempted = new Counter();
  private final Accumulator times = new Accumulator();
  private int txnFgThreadPriority;
  private int txnBgThreadPriority;
  public AuthManager(PayMateDBDispenser dbd, boolean preloadAll,
                     int txnFgThreadPriority, int txnBgThreadPriority) {
    super("AuthManager", dbd);
    this.dbd = dbd;
    this.txnFgThreadPriority = txnFgThreadPriority;
    this.txnBgThreadPriority = txnBgThreadPriority;
    if(preloadAll) {
      // load the rest of the authorizers
      AuthorizerRow authers = dbd.getPayMateDB().getAuths();
      while(authers.next()) {
        prepAuth(authers);
      }
    }
  }

  public Authorizer [] listAsArray() {
    return list.getArray();
  }

  public void downAll() {
    for(int i = list.size(); i-->0;) {
      Authorizer auth = (Authorizer) list.elementAt(i);
      auth.down();
    }
  }

  Monitor authStarter = new Monitor(AuthManager.class.getName()+"AuthStarter"); // prevents auths from getting loaded more than once.

  /**
   * Move most, if not all, of the following code into the Authorizer class **** [especially the constructor and init stuff]
   */
  public Authorizer prepAuth(AuthorizerRow authrow) {
    Authorizer auth = null;
    try {
      authStarter.getMonitor();
      Authid authid = authrow.authid();
      auth = findAuthById(authid);
      if(auth == null) {
        // not started; instantiate one
        String forTrace=authrow.authclass+"["+authrow.authname+"]";
        dbg.WARNING("attempting to instantiate authorizer:"+forTrace);
        Object o = ReflectX.newInstance(authrow.authclass);
        // then, init it
        if(o == null) {
          PANIC("Fatal: failed to instantiate:"+forTrace);
          dbg.ERROR("loadClass returned null:"+forTrace);
        } else {
          if(!(o instanceof Authorizer)) {
            PANIC("Fatal: NOT AN AUTHORIZER!!! "+forTrace);
            dbg.ERROR("bad Class for auth:"+forTrace);
          } else {
            dbg.WARNING("succeeded:"+forTrace);
            auth = (Authorizer)o;
            dbg.WARNING("cast:"+forTrace);
            auth.init(authid, authrow.authname, txnFgThreadPriority,
                      txnBgThreadPriority);
            dbg.WARNING("need to add:"+forTrace);
            list.add(auth);
            dbg.WARNING("after adding:"+forTrace);
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

  public Authorizer findAuthById(Authid authid) {
    return list.findById(authid);
  }

  // used by UserSession to start and stop the service
  public Authorizer findAuthByName(String authname) {
    return list.findByName(authname);
  }
/**
 * create TxnRow and fill it with SOME of the info passed in.
 */
  private TxnRow newTxnRecord(PayMateDB db, PaymentRequest  request, TxnReference tref, Associateid assid){
    TxnRow record = new TxnRow();
    // only non-null fields need to be init here.
    record.transtarttime = db.Now();
    record.setVoided(false);
    record.setSettleop(request.OperationType()); // here is the only place we set the settleop!
    record.setClientReferences(tref); // this MUST come before the card info
    record.setSaleInfo(request.sale);
    record.setAuthRequest(request.origMessage());
    record.associateid = db.Safeid(assid);
    return record;
  }
  /**
  * copies info from original request to make it easier to formulate queries
  * @return merchantInfo associated with original query.
  */
  private MerchantInfo copyFromOriginal(PayMateDB db, TxnRow record,TxnRow original){
    record.setPaytype(original.paytype());
//    record.institution = original.institution;

    record.origtxnid= original.txnid; //"link to orig txn"
    // pick up original authorizer (and settler?) and
    //... ensure that they are still in business and
    //... that the id is still the SAME institution!
    record.authid= original.authid;
    record.settleid=original.settleid;
    //actually, we must NEVER change the essential content of an authorizer definition
    //and we can't remove a record without removing all txns that reference it.
    return db.getAuthMerchantInfo(original.terminalid(), original.authid());
  }

  public ActionReply doGateway(PayMateDB db, GatewayRequest request) {
    if(request == null) {
      return ActionReply.Fubar("AuthMgr.doGateway(): request is null");
    }
    // which terminal is this?
    Terminalid terminalid = request.terminalid;
    if(!Terminalid.isValid(terminalid)) {
      return ActionReply.Fubar("AuthMgr.doGateway(): terminalid is invalid");
    }
    // find the authorizer for this terminal
    Authid authid = db.getDefaultAuthidForTerminal(terminalid);
    if(!Authid.isValid(authid)) {
      return ActionReply.Fubar("AuthMgr.doGateway(): no default authorizer found for terminalid " + terminalid);
    }
    Authorizer authorizer = findAuthById(authid);
    // add the data to the authattempt database (which really doesn't mean authorize attempt)
    AuthAttempt attempt = new AuthAttempt();
    attempt.authrequest.setrawto(request.origMessage());
    attempt.terminalid = terminalid;
    attempt.authid = authid;
    db.startAuthAttempt(attempt);
    // send the gatewayrequest to it
    return authorizer.gateway(db, request, attempt);
  }

 /**
  * decline something without marking the database
  */
  private ActionReply failThisImmediately(PaymentRequest  request, int arstat, String reason){
    dbg.ERROR(reason);
    ActionReply ar= ActionReply.For(request);
    ar.setState(arstat);
    ar.Errors.add(reason);
    PANIC("TXN FAILED:" + reason);
    return ar;
  }

  /**
   * something that isn't in database yet.
   * inherent operation, one that doesn't need a particular authorizer
   * this will be the one and only operation upon the database record
   *
   * +++ %%% @@@ This needs MAJOR testing!
   *
   */
  private ActionReply authAndRecord(PayMateDB db, TxnRow record,AuthResponse auth){
    record.incorporate(auth);
    record.tranendtime = db.Now();
    record.authid = record.settleid = "1";//for all internal declines
    record.authstarttime=record.authendtime=db.Now(); //or we can leave blank
    record.authattempt.setAuthResponse(auth.rawresponse.rawValue().getBytes());
    if(!Txnid.isValid(record.txnid())) {
      db.startTxn(record);//db record actually created here.
    } else {
      db.stampAuthDone(record); // bug in authmix1: this line was omitted altogether!
    }
    Txnid txnid = record.txnid();
    if(txnid.isValid()){
      return Authorizer.forTxnRow(record);
    } else {
      return failThisImmediately(PaymentRequest.Null(),ActionReplyStatus.DatabaseQueryError,"Couldn't create txn record");
    }
  }
 /**
   * decline something that isn't in database yet.
   */
  private ActionReply declineAndRecord(PayMateDB db, TxnRow record,String reason){
    return authAndRecord(db, record,AuthResponse.mkDeclined(reason));
  }
  private ActionReply failAndRecord(PayMateDB db, TxnRow record,String reason){
    return authAndRecord(db, record,AuthResponse.mkFailed(reason));
  }
  private ActionReply approveAndRecord(PayMateDB db, TxnRow record){
    return authAndRecord(db, record,AuthResponse.mkApproved(record.stan));
  }

  private ActionReply InsufficientPriveleges(PayMateDB db,TxnRow record){
    return declineAndRecord(db, record, "Insufficient Priveleges");
  }

  private ActionReply OverStoreLimit(PayMateDB db,TxnRow record,RealMoney maxtxnlimit){
    return declineAndRecord(db, record,AuthResponse.mkOverLimitStr(record.rawAuthAmount().Image(), maxtxnlimit, "store"));
  }

  /**
   * void and modify replies report original txn info for receipts
   * @param original
   * @param record
   */
  private void copyOldvalues(TxnRow original, TxnRow record){
    //card info parts
    record.setCard(original.card());
    record.institution=original.institution;
    //dollar amount parts
    record.setAuthAmount(original.rawAuthAmount());
    //#do NOT change the record settleAmount, it contains the modification amount.
  }

  /**
   * @todo: migrate into base authorizer!
   * @param original
   * @param record
   * @param allowed
   * @param db
   * @return
   */
  private ActionReply preprocessVoid(TxnRow original, TxnRow record, boolean allowed /*permissions*/,
                                     PayMateDB db) {
    String errMessage=null;//failure flag, and detail thereon.
    switch (original.settleop().Value()) {
      default:
      case SettleOp.Unknown: { //transaction in progress
        errMessage = "original record is defective";
      } break;
      case SettleOp.Return:
      case SettleOp.Sale: {
        int actionenum = (byte) original.response().action.charAt(0);
        switch (actionenum) {
          default:
          case Ascii.U: { //stoodin sale hasn't made it back from authorizer
            errMessage = "original still being processed";
          } break;
          case Ascii.D:
          case Ascii.F: //checkc permissions and either approve or decline
            if (allowed) {
              // modify original txn!
              original.setVoided(true);//@todo: apply function 'stamp voided'
              return db.updateTxn(original)? approveAndRecord(db, record):
                  failAndRecord(db,record,"database error attempting void");
            } else {
              return InsufficientPriveleges(db, record);
            } // break;
          case Ascii.A: { //check permissions, pass on to auth if ok
            if (allowed) {
              return null; // authorize it
            } else {
              return InsufficientPriveleges(db, record);
            }
          } //break;
        }
      } //break;
      case SettleOp.Void:
      case SettleOp.Query:
      case SettleOp.Modify: {
        errMessage = "original not a voidable txn type:" +
            original.settleop().Image();
      } break;
    }
    if(!StringX.NonTrivial(errMessage)){
      errMessage = "coding error!";
    }
    return declineAndRecord(db, record, errMessage);
  }

  private ActionReply applyModification(TxnRow original,TxnRow record,
                                        boolean allowed, RealMoney maxtxnlimit,
                                        PayMateDB db){
    String errMessage=null;//failure flag, and detail thereon.
    switch (original.settleop().Value()) {
       default:
       case SettleOp.Unknown: //transaction in progress
         errMessage="original record is defective";
         break;
       case SettleOp.Return:
         errMessage="modification of a return not allowed";
         break;
       case SettleOp.Sale: {
         int actionenum = (byte) original.response().action.charAt(0);
         switch (actionenum) {
           default:
           case Ascii.U: //stoodin sale hasn't made it back from authorizer
             if(original.wasStoodin()){
               if(record.rawSettleAmount().exceeds(maxtxnlimit)){
                 return OverStoreLimit(db, record, maxtxnlimit);
               } else {
                 // else always approve it, increasing a potential loss :)
               }
             } else {
               errMessage="original still being processed";
             }
             break;
           case Ascii.D:
           case Ascii.F: //check permissions and either approve or decline
             errMessage="original was declined (or failed)";
             break;
           case Ascii.A: {
             if (allowed) { //check permissions, do it if allowed
               // the client already checked this if it stoodin, so don't check here
               if( ! record.stoodin() && record.rawSettleAmount().exceeds(maxtxnlimit)){
                 return OverStoreLimit(db, record, maxtxnlimit);
               } else {
                 // always approved
               }
             } else {
               errMessage="Insufficient Priveleges";
             }
           } break;
         }
       } break;
       case SettleOp.Void:
       case SettleOp.Query:
       case SettleOp.Modify: {
         errMessage = "original not a modifiable txn type:" + original.settleop().Image();
       } break;
     }
     if(StringX.NonTrivial(errMessage)){
       return declineAndRecord(db, record, errMessage);
     } else {
       // MODIFY HERE
       original.setSettleAmount(record.rawSettleAmount());
       original.setSettle(true);
       return db.updateTxn(original) ? approveAndRecord(db, record) :
         failAndRecord(db,record,"database error attempting modification");
     }
  }

  public static final String ARMSGINVALIDTERMINAL = "Terminal not known to database";
  public static final String ARMSGINVALIDSTORE    = "Storeid not defined for this terminalid:";

  /**
   * @param request
   * @param linfo contains store and terminal etc context for the request
   * @param reply is only nontrivial for stoodins. It is the reply as stood in (at client)
   * @param voidme is only nontrivial for stoodins that were voided before being submitted for real-authorization.
   */
  public ActionReply doFinancial(PaymentRequest  request, Store store, TerminalInfo tinfo, Associateid assid, AssociatePermissions permissions, PaymentReply sireply, PaymentReply sivoidme) {
    dbg.Enter("doFinancial");
    StopWatch sw = new StopWatch();
    try {
      PayMateDB db = dbd.getPayMateDB();
      attempted.incr();
      Terminalid terminalid = request.terminalid;
      if(!terminalid.isValid()) {      // Is it a valid request?
        return failThisImmediately(request,ActionReplyStatus.InvalidTerminal, ARMSGINVALIDTERMINAL);
      }
      if(store == null) {
        return failThisImmediately(request,ActionReplyStatus.InvalidTerminal, ARMSGINVALIDSTORE+terminalid); //pathological exit
      }
      // if it is a good terminal and store,
      // mutex the terminal here (allows us to prevent multiples from coming in and hitting doretry) !!!!
      TerminalMutex termutex = TerminalMutex.getMutex(terminalid); // if this returns null, it is a SYSTEM FAILURE !!!
      boolean wasStoodin = (sireply != null);
      try {
        termutex.getMonitor();
        if (request.createsTxn()) { // if creates new txn records (rather than modifying old)
          dbg.ERROR("About to do retry; tinfo=" + tinfo.toSpam());
          ActionReply ar = doRetry(db, request, terminalid); // See if this is a duplicate transaction.
          dbg.ERROR("RETRY DONE.");
          if (ActionReply.Successful(ar)) {
            if (ar instanceof PaymentReply) { // if was found and authed
              PaymentReply realreply = (PaymentReply) ar;
              if (sireply != null && !sireply.isApproved()) { // client stoodin and declined
                //@todo we need to void or do a return
                PANIC("STANDIN,client rejected,server authed,needs refund:" + realreply.tref().toSpam());
              } else {
                // what ?
              }
              PANIC_NO_EMAIL("DORETRY,duplicate prevented:" + realreply.tref().txnId);
            } else {
              // +++ what would this be?    Coding error!  Panic!  Shuld never happen!
            }
            return ar; //+++ should log warning; this is a dup!
          } else {
            // duplicate not detected or incomplete or defective or (in progress and not stoodin)
          }
        } else {
          //duplicate is detected by original txn's state, when it matters
        }

        TxnReference tref= request.TxnReference();
        TxnRow record = newTxnRecord(db, request,tref,assid);//does NOT create one in the database
        TxnRow original = null;
        MerchantInfo merch = new MerchantInfo();
        RealMoney maxtxnlimit = new RealMoney();
        Authorizer auth = null;
        boolean isModify = request.isModify();
        boolean wasStoodinApproved = wasStoodin && sireply.isApproved();
        boolean wasStoodinApprovedModify = isModify && wasStoodinApproved;

        if(request.modifiesTxn()) {//find original
          record.setStan(STAN.NewFrom(0));//ignore trash from legacy clients.
          //+_+ stans aren't unique enough, this depends upon rolloff.
          Txnid txnid = request.findByTxnid() ? request.originalTxnid() :
              db.getTxnid(request.terminalid, request.stan2modify());
          if(Txnid.isValid(txnid)) {
            original = db.getTxnRecordfromTID(txnid);
          } else {
            // original still null
          }
          if (original == null) {
            String msg = "DECLINED,record not found " + request;
            dbg.ERROR(msg);
            // if modify was stoodin, PANIC to let the user know the txn is a LOSS!!!
            if(wasStoodinApprovedModify) {
              PANIC("LOSS:"+msg);
            }
            return declineAndRecord(db, record, "original not found");
          } else {
            // we have the original
            copyOldvalues(original, record);
          }
          merch = copyFromOriginal(db, record,original);
          maxtxnlimit = db.getAuthInfo(store.storeId(), original, merch); // +_+ this is wasteful, a special query would be more efficient
          if (original.isVoided()) { // original ALREADY voided
            if (request.isVoider()) {
              // this is going to be approved, else voided wouldn't be true
              TxnRow theVoid = db.getTxnRecordfromTID(db.getVoidForOriginal(original.txnid()));
              dbg.WARNING("VOID,record already voided, returning original void " + request);
              // get info for receipt
              return theVoid.extractReply();
            } else { //Modify a Voided txn
              dbg.WARNING("CHANGE,record already voided, declining " + request);
              return declineAndRecord(db, record, "original has been voided");
            }
          } else {
            // continue to process void of unvoided txn
          }
          if (original.isSettled()) {
            String msg  ="DECLINE,record already settled " + request;
            //void and modify respond the same
            dbg.ERROR(msg);
            // if modify was stoodin, PANIC to let the user know the txn is a LOSS!!!
            if(wasStoodinApprovedModify) {
              PANIC("LOSS:"+msg);
            }
            // +++ when we have event notification for customers, notify them of this
            return declineAndRecord(db, record, "record already settled");
          }
          auth = findAuthById(record.authid());//note: modifiers copy original's authid into record.
          if(!auth.accepts(record.transfertype())) {
             return declineAndRecord(db, record, record.transfertype().Image() +
                                     " not supported by authorizer " +
                                     auth.serviceName()+"["+auth.id+"]");
          }
          // permissions are checked for certain voids and all modifications inline
          if (request.isVoider()) {
            ActionReply ar = preprocessVoid(original, record,permissions.permits(AssociatePermissions.PermitVoid), db);
            if (ar != null) {
              return ar;
            } else {
              // proceed to authing this, below, where we also check permissions here? +++
              record.setAuthz(true);
            }
          } else {
            // assumes this is a modify txn! (that there are only two types of modifier)
            if(store.enmodify) {
              if(record.paytype().is(PayType.Credit)) { // can only modify credit txns!
                return applyModification(original, record, permissions.permits(AssociatePermissions.PermitSale), maxtxnlimit, db);
              } else {
                return declineAndRecord(db, record, "cannot modify a "+record.paytype().Image()+" transaction");
              }
            } else {
              return declineAndRecord(db, record, "modify not enabled for store");
            }
          }
        } //end finding original and dealing with the grosser issues related to it

        else { //action moves money (svc queries move $0)
          record.setAuthAmount(request.Amount());
          if(request.hasSomeCardInfo()) { // hasCardInfo checks to see if the card is valid.  However, even if WE think the card is valid, that has nothing to do with whether or not a card was SUPPLIED!
            record.setCard(request.card);
            if (request.hasPin()) {
              record.setPINdata(request.pin);
            }
            //reject corrupt card
            BinEntry cardguess = db.getBinEntry(request.card.bin());
            request.card.applyBinEntryMoots(cardguess); // sets moots
            if(!request.card.isComplete()) {
              return declineAndRecord(db,record,"invalid card info");
            }
            //note BIN discrepancy, but trust client
            String cardPayType=db.getPaymentTypeFromCardNo(record);///sets Institution and return paytype
            record.setPaytype(request.PaymentType());//same as request.sale.payby ??
            if(record.paytype!=cardPayType){
              dbg.WARNING(" bin says "+cardPayType+", for request type "+record.paytype().Image());
              //@todo: potential offline debit... put BIN into a list of candidates.
              //@todo: do we need to deal with credit vs giftcard here?
            }
          } else {
            PANIC("Request ["+request+"] has no card ["+ ((request.card == null) ? "null" : request.card.toSpam().toString()) +"]!  This isn't supposed to happen!"); // Maye the card is bad?  Should still try to make it work!
          }

          //would restore check processing here.

          maxtxnlimit = db.getAuthInfo(store.storeId(), record, merch); // gets the authtermid, authid, etc.
          if( ! record.authid().isValid()) {//pathological error
            String msg = "no authorizer for "+request.sale.type.payby.Image()+" "+StringX.TrivialDefault(record.institution, "");
            record.setPaytype(request.sale.type.payby);//set to what client expected.
            return declineAndRecord(db,record,msg);
          }
          if(sireply != null){//we may still be filtering these out at a higher level
            boolean wasVoided = (sivoidme != null);
            boolean wasDeclined = !sireply.Succeeded(); //for when we record failed operations at the client.
            record.incorporateStandin(sireply);
            if(wasVoided){ // voided in the client before being sent
              record.voided=Bool.TRUE();
              // +++  this has a potential for being terribly wrong! details please???
              return approveAndRecord(db,record);//+_+ but doesn't create a voiding txn. alh actually likes that.
            }
            if(wasDeclined){ // declined in the client before being sent
              //mark and skedaddle
              // +++  this has a potential for being terribly wrong! details please???
              return declineAndRecord(db,record, sireply.auth().authmsg);
            }
            //else continue on to submit for real authorization
          }
          // only check for overlimit on Return, Sale, and AuthOnly
          switch(request.OperationType().Value()) {
            case TransferType.Return:
            case TransferType.Sale:
            case TransferType.Authonly: {
              //@todo: relocate this clause into authorize function ???
              if (record.rawAuthAmount().exceeds(maxtxnlimit)) { //then locally decline
                dbg.ERROR("Txnamount limit exceeded! txnAmount=" + record.rawAuthAmount().Value() + ", Limit=" + maxtxnlimit);
                return OverStoreLimit(db, record,maxtxnlimit);
              }
            } break;
          }
        }
        if(auth == null) {
          auth = findAuthById(record.authid()); //note: modifiers copy original's authid into record.
          if(auth == null) {//considerred pathological
            dbg.ERROR("Could not find authorizer to use!");
            PANIC("Could not find auth by id: " + record.authid() + " for PTIN="+record.paytype+record.institution);
            return declineAndRecord(db,record,"CALL SUPPORT! (auth not found by id)");//@todo: should be failure?  NO.  Fail indicates that you can retry. n o-only fail things that can stand in
          }
        }
        // now insert the record
        tref.setId(db.startTxn(record));//db record actually created here.

        if(!tref.txnId.isValid()) {
          if(false /* wasStoodin */) {
            // probably should send a PANIC with the info in it so that we can manually enter it as a force.
          } else {
            dbg.ERROR("ERROR inserting a new record into txn !!!! Follows: " +
                      record);
            return failThisImmediately(request,
                                       ActionReplyStatus.DatabaseQueryError,
                                       "could not insert txn record");
          }
        }
        if(!auth.accepts(record.transfertype())) {
           return declineAndRecord(db, record, record.transfertype().Image() + " not supported by authorizer");
        }
//time for permissions check!
        // only check permissions for certain kinds of txns (any create, plus some voids)
        // note that modifications should have already been done!
        boolean allowed = false;
        switch(request.OperationType().Value()) {
          // creates
          case TransferType.Authonly:
          case TransferType.Force:
          case TransferType.Sale:
          case TransferType.Query: {
            allowed = permissions.permits(AssociatePermissions.PermitSale);
          } break;
          case TransferType.Return: {
            allowed = permissions.permits(AssociatePermissions.PermitReturn);
          } break;
          case TransferType.Reversal: {//some voids get here, when we need to send void to authorizer rather than just modify the settlement
            allowed = permissions.permits(AssociatePermissions.PermitVoid);//could actually set this to 'true' as it has already been checked or we don't get here.
          } break;
          case TransferType.Modify:// all modifies are completed by the time we get here, never sent to authorizer except via modifiying other things that go into settlement
            allowed = false;
          case TransferType.Unknown: {
            allowed = false; // +++ coding error
          }
        }
        if(allowed){
          if(record.transfertype().is(TransferType.Authonly) && !store.enauthonly) {
            return declineAndRecord(db, record, "authonly not enabled for store");
          }
          // at this point, we should be able to check record.authz,
          // and only ones with "true" should be here
          return auth.authorize(db, record, store, original, merch);
        } else {
          return InsufficientPriveleges(db, record);
        }
      } catch(Exception e) {
        dbg.Caught(e);
        return failThisImmediately(request,ActionReplyStatus.FailureSeeErrors,"Exception:"+e.getMessage());
      } finally {
        termutex.freeMonitor();
      }
    } catch (Exception e) {
      dbg.Caught(e);
      return failThisImmediately(request,ActionReplyStatus.FailureSeeErrors,"Exception:"+e.getMessage());
    } finally {
      completed.incr();
      times.add(sw.Stop());
      dbg.Exit();
    }
  }



  /**
  * try to find a recent record that matches a retried financial request
  * @return the reply if one is found.
  */
  private final ActionReply doRetry(PayMateDB db, PaymentRequest  request, Terminalid terminalid){
    dbg.Enter("doRetry");
    try {
      TxnRow tj=db.getTransactionForRetry(terminalid, request);
      if(tj!=null){ // now that we have the latest record compare it to the request
        dbg.VERBOSE("got a match");
        if(request.hasSomeCardInfo()){ // needs further matchimg
          // check to see if we failed to talk to authorizer
          if(!tj.responded()) {
            if(!tj.wasStoodin()) { // if not, reply with ServerFailure & put it into the problems log
              PANIC("RETRY,STANDIN,doRetry():retry of a txn in process");
              ActionReply fry = ActionReply.For(request);
              fry.Errors.Add("Transaction in progress.");
              fry.status.setto(ActionReplyStatus.TryAgainLater);
              return fry;
            }
          }
          if(request.card.equals(tj.card())){//query is missing some fields...
            //...query will accept same card with different expiration dates.
//            return card4Txn(tj,cr.card);
            return Authorizer.forTxnRow(tj);
          }
          dbg.WARNING("imperfect card match");
        }
//        else if(request instanceof CheckRequest){
//          return Authorizer.forTxnRow(tj);
//          CheckRequest cr=(CheckRequest)request;
//          return check4Txn(tj,cr.check);///later...
//        }
      }
      return null;
    } finally {
      dbg.Exit();
    }
  }

  // next 3 *Txn functions needed by doRetry() ...

  /**
  * @return a CardReply extracted from a txn record.
  */
//  private final PaymentReply card4Txn(TxnRow tj, MSRData card){
//    //was it debit or credit? 'til we have debit presume credit
//    PaymentReply reply = new PaymentReply();
//    insertFinancialsFromTxn(tj,reply);
//    return reply;
//  }

//  /**
//  * @return  a CheckReply extracted from a txn record.
//  */
//  private final PaymentReply check4Txn(TxnRow tj, MICRData check){
//    CheckReply reply=new CheckReply();
//    reply.ManagerOverrideData="";//field 116. some compendium that ensures the resent request is allowed to exceed limits.
//    insertFinancialsFromTxn(tj,reply);
//    return reply;
//  }

//  private final void insertFinancialsFromTxn(TxnRow tj,PaymentReply frep){
//    frep.auth().setAll(tj.actioncode,tj.approvalcode,tj.authresponsemsg,tj.authtracedata,tj.authrrn);
//    frep.setReference(tj.tref());//our referenceinfo, not authorizer's.
//    frep.setState(ActionReplyStatus.Success); //successful PROCESS regardless of approval:
//  }

  /**
   * @param request the BatchRequest
   * @param linfo contains store and terminal etc context for the request
   *  +++ @@@ %%% Terminal aggregation will cause this to have duplicates !!!
   *  +++ need to figure out which ones before calling this function, and have a functino that either accepts a storeid or a list of terminalids !!!
   */
  public void submitAll(PayMateDB db, Terminalid terminalid, boolean auto) {
    // for this terminal, find all authorizers
    try {
      AuthorizerRow authers = db.getAuthidsForTerminal(terminalid);
      Authid authid;
      while(authers.next()) {
        try {
          authid = authers.authid();
          if(!submit(db, terminalid, authid, auto)) {
            // but keep going (don't break)
            PANIC("Failed to submit a batch to auth " + authid + " for terminalid " + terminalid);
          }
        } catch (Exception e2) {
          dbg.Caught(e2);
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  public boolean submit(PayMateDB db, Terminalid terminalid, Authid authid, boolean auto) {
    Authorizer auth = findAuthById(authid);
    if(auth == null) {
      PANIC("Unable to find authorizer for terminalid="+terminalid);
    } else {
      // do a batch submittal to this authorizer
      if(!auth.submit(db, terminalid, auto)) { // if it fails, scream your head off
        PANIC("Unable to issue submittal for terminalid="+terminalid+" to auth"+authid);
      } else {
        return true;
      }
    }
    return false;
  }

  public boolean retrySubmittal(PayMateDB db, Batchid batchid) {
    // release the old txns at this point, then just do a full submittal
    db.clearBatchDetails(batchid); // clear the details
    db.setBatchTotals(batchid, 0, 0); // clear the totals
    // just in case there is no valid terminalid yet
    Terminalid terminalid = db.getTerminalForBatch(batchid);
    Authid authid = db.getAuthForBatch(batchid);
    return submit(db, terminalid, authid, false /*auto*/);
  }

}

class TerminalMutex extends Monitor {

  // static stuff
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TerminalMutex.class);
  private static Hashtable list = new Hashtable(30, 10); // good enough size for now
  private static final Monitor tmm = new Monitor("TerminalMutexMonitor");
  public static final String genName(Terminalid terminalid) {
    return TerminalMutex.class.getName()+".T"+terminalid;
  }
  public static final TerminalMutex getMutex(Terminalid terminalid) {
    TerminalMutex tm = null;
    String termidstr = String.valueOf(terminalid);
    try {
      tmm.getMonitor();
      // first, try to find one
      tm = (TerminalMutex)list.get(termidstr);
      // if can't find one, make one
      if(tm == null) {
        tm = new TerminalMutex(terminalid);
        list.put(termidstr, tm);
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      tmm.freeMonitor();
      return tm;
    }
  }

  // object stuff
  public TerminalMutex(Terminalid terminalid) {
    super(genName(terminalid));
  }

}

//$Id: AuthManager.java,v 1.156 2004/03/03 07:28:58 mattm Exp $
