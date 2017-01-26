package net.paymate.web;
/**
* Title:        $Source: /cvs/src/net/paymate/web/ConnectionServer.java,v $
* Description:  Manages the server communications (Actions)<p>
* Copyright:    2000 PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Revision: 1.28 $
*/
import net.paymate.connection.*;
import  net.paymate.net.*;
import  net.paymate.util.*;
import  net.paymate.data.*;
import net.paymate.lang.ThreadX;
import  net.paymate.awtx.RealMoney;
import net.paymate.lang.StringX;
import  net.paymate.util.timer.*;
import  net.paymate.database.ours.query.*;
import  net.paymate.database.*;
import  java.sql.*;
import  net.paymate.jpos.data.*;
import  java.io.*;
import  java.util.Date;//must be explicit to override sql.date
import  java.util.*;
import  net.paymate.data.*; // TimeRange
import  net.paymate.data.sinet.hardware.*;
import net.paymate.data.sinet.business.*;
import  net.paymate.authorizer.*;
import net.paymate.terminalClient.PosSocket.Formatter;
import net.paymate.terminalClient.Receipt;

public class ConnectionServer extends Service {
  protected static final ErrorLogStream dbg=ErrorLogStream.getForClass(ConnectionServer.class);
  // singleton stuff:
  public static final ConnectionServer THE() {
    return connectionServer;
  }
  private static ConnectionServer connectionServer = null;
  public static final void init(PayMateDBDispenser dbd,
                                ApplianceTrackerList applist,
                                boolean preloadAllAuths, int FGTXNTHREADPRIORITY,
                                int BGTXNTHREADPRIORITY) {
    connectionServer = new ConnectionServer(dbd, applist, preloadAllAuths,
                                            FGTXNTHREADPRIORITY,
                                            BGTXNTHREADPRIORITY);
  }

  public AuthManager authmgr = null;
  private PayMateDBDispenser dbd = null; // However, in this class, try to use the PayMateDB passed in.  There is no thread here.
  private ApplianceTrackerList applist = null;

  private String alertList;

  public ConnectionServer(PayMateDBDispenser dbd, ApplianceTrackerList applist,
                          boolean preloadAllAuths /* +++ get from configs */,
                          int FGTXNTHREADPRIORITY, int BGTXNTHREADPRIORITY) {
    super("CONNECTIONSERVER", dbd, true);
    this.dbd = dbd;
    this.applist = applist;
    authmgr = new AuthManager(dbd, preloadAllAuths, FGTXNTHREADPRIORITY,
                              BGTXNTHREADPRIORITY);
    alertList = dbd.getServiceParam(serviceName(), ALERTLIST, defaultMaillist());
    PANIC(alertList, "init():", "have a nice day :)");
  }

  // service stuff ...
  public String svcTxns() {
    return of(completes.value(), attempts.value());
  }
  public String svcPend() {
    return ""+(completes.value()-attempts.value());
  }
  public String svcLogFile() {//someone has to report on ErrorLogStream's fpf.
    return ErrorLogStream.fpf != null ? ErrorLogStream.fpf.status() : "ErrorLogStream.fpf is null!";
  }
  public void down() {
    // stub
  }
  public void up() {
    // stub
  }
  public boolean isUp() {
    return true;
  }

  private BatchReply badBatch(String spoiler,TerminalInfo tinfo){
    BatchReply bratch=BatchReply.New(tinfo);
    bratch.fubar(spoiler);
    dbg.ERROR(spoiler+" terminal:"+ tinfo!=null?tinfo.toSpam():"no info");
    return bratch;
  }

  // +++++ combine this with Unsettled Transaction Format & the new data provider/consumer model.

  private final StoreReply doStoreReply(PayMateDB db, StoreRequest request, LoginInfo linfo, boolean auto) {
    StoreReply srep=new StoreReply();
    if( ! linfo.permits(AssociatePermissions.PermitClose)){
      srep.setState(ActionReplyStatus.InsufficientPriveleges);
    } else {
      srep.setState(issueDeposit(db.getStoreForTerminal(request.terminalid), auto));
    }
    return srep;
  }

  public final boolean issueDeposit(Storeid storeid, boolean auto) {
    dbg.Enter("issueDeposit");
    try {
      PayMateDB db = dbd.getPayMateDB();
      Terminalid [ ] tids = db.getTerminalidsForStore(storeid);
      // iteration goes here..
      // get the list of all terminals for the store of this terminal
      for(int i = tids.length; i-->0;) {
        // add a batch reply to multireply
        Terminalid tid = tids[i];
        TerminalInfo ti = db.getTerminalInfo(tid);
        if(!ti.isGateway()) {
          try {
            authmgr.submitAll(db, tid, auto); // spawn batch submittals
          } catch (Exception ex) {
            dbg.Caught("submitting batch got:",ex);
            // but proceed, don't let it stop us from replying
          }
        } else {
          dbg.WARNING("Not submitting batch for gateway terminal: " + tid);
        }
      }
      return true;
    } catch(Exception e) {
      dbg.Caught(e);
      return false;
    } finally {
      dbg.Exit();
    }
  }

  private final ActionReply doGateway(PayMateDB db, GatewayRequest gwr) {
    return authmgr.doGateway(db, gwr);
  }

  public final MultiReply closeAllDrawers(Storeid storeid, boolean auto) {
    PayMateDB db = dbd.getPayMateDB();
    return closeAllDrawers(storeid, db, auto);
  }
  // this function does NOT get listings or subtotals.
  // It just closes the drawers.
  // It returns a set of BatchReplies with drawerids or error codes
  public final MultiReply closeAllDrawers(Storeid storeid, PayMateDB db, boolean auto) {
    MultiReply mr = new MultiReply();
    boolean allSuccess = true;
    if(Storeid.isValid(storeid)) {
      TerminalPendingRow tpr = db.getTerminalsForStore(storeid);
      while(tpr.next()) {
        Terminalid tid = tpr.terminalid();
        Drawerid drawerid = db.closeDrawer(tid, null, auto);
        ActionReply ar = new ActionReply();
        boolean succeeded = Drawerid.isValid(drawerid);
        ar.setState(succeeded);
        mr.add(ar);
        if(!succeeded) {
          PANIC("Failed to autodrawer for terminal " + tid + ", store " + storeid + "!");
          allSuccess = false;
        }
      }
    } else {
      dbg.ERROR("Invalid storeid passed to closeAllDrawers()!");
      allSuccess = false;
    }
    mr.setState(allSuccess);
    return mr;
  }

  private final BatchReply doBatchReply(PayMateDB db, BatchRequest request, LoginInfo linfo) {
    // to prevent other programmers from accidentally using the terminalid in the batchrequest instead of one passed in, change it in the request before passing it in.
    BatchReply bratch=BatchReply.New();
    if( ! linfo.permits(AssociatePermissions.PermitClose)){
      bratch.setState(ActionReplyStatus.InsufficientPriveleges);
      return bratch;
    }
    try {
      Terminalid Termid= request.terminalid;//+++ make a list
      TerminalInfo tinfo = db.getTerminalInfo(Termid);
      bratch.set(tinfo);
      Drawerid drawer = new Drawerid(); // by default, is invalid
      // don't give the merchant the opportunity to close drawers at the client if they are using autoclose, just print! +++ %%% @@@
      if(request.isClosing){      // close THEN generate report
        drawer = db.closeDrawer(Termid, linfo.associateid(), false /*auto*/); // do the drawer completely before the batch
        // by doing the drawer first, you prevent db situations where the update from the drawer close and batch submittal slam into each other
        if(!request.justClose) { // justClose: means DON'T submit with a close, not relevent if not closing.
          try {
            authmgr.submitAll(db, Termid, false /*auto*/); // spawn batch submittals
          } catch (Exception ex) {
            bratch.Errors.add("Batch submission caused:").add(ex.getMessage());
            dbg.Caught("submitting batch got:",ex);
            //but proceed, don't let it stop us from closing a drawer
          }
        }
        if(!Drawerid.isValid(drawer)) { // couldn't do the close
          return badBatch("couldn't closeDrawer",tinfo);
        }
        bratch.isClosed=true;
        //can query end time via drawer id
        //can query start as well..
        if(tinfo.prefersNoBatchReport() || request.noReport) { // --- hack
          bratch.status.setto(ActionReplyStatus.Success);
          return bratch;
        }
      } else {//is pending data
        bratch.ranger().setStart(db.getPendingStartTime(tinfo.id())); //end will get set from items.
      }

      StopWatch queryTime = new StopWatch();  // a timer for the drawer query here
      TxnRow rec = db.getDrawerQuery(drawer, tinfo.id(), true);//invalid drawer gets us pending info
      queryTime.Stop();
      if(rec == null) {
        return badBatch("query failed",tinfo);
      }
      Accumulator dbnextTimes = new Accumulator();
      Accumulator rowloadTimes = new Accumulator();
      Accumulator txl8times = new Accumulator();
      try {
        int errcount=0;
        StopWatch dbnextsw = new StopWatch(false);
        StopWatch rowloadsw = new StopWatch(false);
        StopWatch blitesw = new StopWatch(false);
        while(rec.next(dbnextsw, rowloadsw)) {//possibly null on first attempt
          dbnextTimes.add(dbnextsw.millis()); // timer
          rowloadTimes.add(rowloadsw.millis()); // timer
          // use the same timer to time the extraction from a TxnRow to BatchLineItem
          blitesw.Start();
          BatchLineItem bitem = rec.blightem();
          txl8times.add(blitesw.Stop());
          if(!bratch.addItem(bitem)){
            ++errcount;
          }
        }
        if(errcount>0){
          bratch.Errors.add("["+errcount+"] defective items not shown");
        }
        if(request.justSummary) { // justSummary: don't put detail items into the reply. we still will have to loop over them to get summary values
          bratch.clearDetail();
        }
        bratch.status.setto(ActionReplyStatus.Success);
      } finally { //in case we get exceptions in the above, but let them throw up ...
        dbg.ERROR("doBatchReply times in ms: qry(" + queryTime.millis() +
                  "), dbnext(" + dbnextTimes.toSpam() +
                  "), rowload(" + rowloadTimes.toSpam() +
                  "), txl8(" + txl8times.toSpam() +
                  "), rowloadGetToDate(" + Query.fromResultSetGet.toSpam() +
                  "), rowloadSetToDate(" + Query.fromResultSetSet.toSpam() +
                  "), rowloadFieldsToDate(" + Query.fromResultSetFields.toSpam() +
                  "), rowloadNameToDate(" + Query.fromResultSetName.toSpam() +
                  ")");
        rec.close();
      }
      return bratch;
    } catch(Exception arf){
      dbg.Caught(arf);
      return badBatch(arf.getLocalizedMessage(),null);
    } finally {
      dbg.Exit();
    }
  }

//  private final ActionReply getReceipt(ReceiptGetRequest rsr) {
//    ReceiptGetReply rgr = new ReceiptGetReply();
//    EasyCursor ezp = receiptAgent.loadReceipt(rsr.tref, rgr.Errors);
//    rgr.setReceipt(ezp);
//    rgr.status.setto(rgr.isOk() ? ActionReplyStatus.Success : ActionReplyStatus.FailureSeeErrors); // cop out
//    return rgr;
//  }

  private final ActionReply doReceiptStore(ReceiptStoreRequest rsr){
    ReceiptStoreReply rsry= (ReceiptStoreReply)ActionReply.For(rsr); // new ReceiptStoreReply();//init now to collect errors in saveReceipt
// old
//    receiptAgent.saveReceipt(rsr.reference(), rsr.toDiskImage(), rsr.clerk.Name(), rsry.Errors);
// endold
// new
    if(rsr != null) {
      Receipt r = rsr.receipt();
      PayMateDB db = dbd.getPayMateDB();
      TxnReference tref = rsr.reference();
      if(r != null) {
        db.setSignature(tref, r.getSignature());
      } else {
        PANIC("doReceiptStore(): tried to store receipt, but receipt was null! tref=" + tref);
      }
    } else {
      PANIC("doReceiptStore(): ReceiptStoreRequest is null!");
    }
// endnew
    rsry.setState(true);//--- always succeed, receiptor better check storedOk
    return rsry;
  }

  /**
  * @param arstat is the status of the login check performed on most requests
  * @param linfo is for database access for clerk options; needs to be a permissions object, or something
  */
  private final ActionReply doClerkLogin(LoginInfo linfo){
    LoginReply lr = new LoginReply();
    //@todo: apply overall enable bit
    if((linfo != null) && (linfo.permissions != null) && (linfo.permissions.store!=null)) {
      lr.clerkCap = linfo.permissions.store;
      lr.setState(true);
    } else {
      lr.setState(ActionReplyStatus.InvalidLogin);
      lr.clerkCap.clear();
    }
    return lr;
  }

  private final ActionReply doConnection(PayMateDB db, ConnectionRequest request, IPSpec fromip){
    dbg.Enter("doConnection");
    try {
      Appliance appliance = ApplianceHome.GetByName(request.applname);
      ConnectionReply cr = db.getApplianceInfo(appliance.applianceid()); // leave this for now
      if(cr == null) {
        PANIC("Intruder alter! applianceid = " + request.toEasyCursorString()); // can use to determine new appliances in the field
        return ActionReply.For(request).setState(ActionReplyStatus.InvalidAppliance);
      } else {
        applist.logUpdate(request, fromip); // ignore reply
      }
      return cr;
    } catch (Exception ex) {
      return ActionReply.For(request).setState(ActionReplyStatus.ServerError);
    } finally {
      dbg.Exit();
    }
  }

  private static final String srekinfo(StoodinRequest srek) {
    return "StoodinRequest:Terminalid="+srek.terminalid+", txn="+srek.request().TxnReference().toSpam();
  }

  /**
  * process something that the client did a standin for.
  */
  private final ActionReply doStandin(PayMateDB db, StoodinRequest srek, LoginInfo linfo){//pronounced "shreck"
    dbg.Enter("doStandin");
    StoodinReply ar=new StoodinReply(srek.recordID());//this id EXISTS to link the reply with the request, do not use for anything else.
    try {//only financial requests are (supposed to be) stood in for:
      if(srek.voidme()!=null){//+++ someday create a voided txn record.
        PANIC("doStandin() received txn voided while in standin "+srekinfo(srek));
        //record the void here, clear it in request call ourselves recursively then
        //void using txnid from the actionreply.
        ar.setState(true);
      } else {
        // fix a bug from the gateway stuff ...
        if(!Terminalid.isValid(srek.request().terminalid)) { // if the inner request didn't get a terminalid
          srek.request().terminalid = srek.terminalid; // give it the one from the outer request
        }
        // then check its terminalinfo
        TerminalInfo ti = db.getTerminalInfo(srek.terminalid);
        if(TerminalInfo.IsGateway(ti)) {
          // skip the login check, but set the linfo on the session, anyway, so other things don't croak ...
          linfo.getTerminalLoginInfo(null, srek.terminalid, null); // +++ inspect return value?
        } else {
          boolean loginOk=linfo.terminalLogin(srek.request());//@standin
          String logline = null;
          if( ! loginOk){//@loginsecurityhole@  stoodin requests may have been made with a bad login.
            logline = "doStandin() accepted invalid clerk login for "+srekinfo(srek);
          } else if(false){//@loginsecurityhole@  stoodin requests may have been made with a bad login.
//@todo: check priveleges on standins.
            logline = "doStandin() accepted clerk login with invalid priveleges for "+srekinfo(srek);
          }
          if(StringX.NonTrivial(logline)) {
            dbg.ERROR(logline);
            PANIC_NO_EMAIL(logline); // +++ not if its gatewayed terminal
          }
        }
        //we will eventually remove the following clause... right now it makes us behave like previous releases +++
        PaymentReply nullcheck = srek.reply();
        if(nullcheck == null) {
          ar.status.setto(ActionReplyStatus.FailureSeeErrors);
          ar.Errors.add("Unable to parse stoodin.paymentreply:"+srek);
          return ar;
        } else {
         if(!nullcheck.isApproved()){ //        @SID@
           //+++ eventually place record in database
           PANIC_NO_EMAIL("declined operation received from client", srek);
           ar.status.setto(ActionReplyStatus.Success); //successfully received standin.
           return ar;
         } else {
           // do it
         }
        }
        //don't recheck embedded login:
        PaymentReply buried= (PaymentReply) generateReply(srek.request(), linfo, false, srek.reply(), srek.voidme(), false, null);
        //above cast only fails if request is way hopelessly borked.
        if(buried.Succeeded()){//remember that DECLINED txns succeed.
          ar.status.setto(ActionReplyStatus.Success);//ignores errors in standin flag setting +_+
          // more @SID@ patching. this needs to preceed 'generateReply' to make the logic clean
          if(buried.isApproved()!=srek.reply().isApproved()){
            PANIC("client-server collision on standin",srek);
          }
        } else {
          // +++ log this; it should never happen !!!
          PANIC("FATAL! Couldn't handle the transaction!  Should never happen! for "+srekinfo(srek));
          ar.Errors.appendMore(buried.Errors);
          ar.status.setto(ActionReplyStatus.ServerError);//@#@ this return is what hangs requests in client
        }
        // already in table (or should be)
      }
    } catch(Exception caught){
      String stamp = DateX.timeStampNow();
      dbg.Caught(stamp, caught);
      ar.Errors.add("Caught:"+stamp);
      ar.status.setto(ActionReplyStatus.ServerError);
    } finally {
      //and deal with an error here?
      if (!ar.Succeeded()&&!ar.ComFailed()) {
        ////goes into the nonexistent standin failures log.
        // store in problems list, let client wash its hands of this.
        PANIC("STANDIN,Failed", "["+srekinfo(srek)+"], socketOpenAttemptCount = " +
              /*authTran.socketOpenAttempts.value() +*/ ", "+String.valueOf(ar));
      }
      dbg.Exit();
      return ar;
    }
  }

  // these two keep up with txns moving thru the ConnectionServer
  public final Counter attempts = new Counter();
  public final Counter completes = new Counter();

  /**
   * this is used to test appliance response to server not answering a request.
   */
  private boolean SLEEPER = false;
  public void SLEEPER(boolean to) {
    SLEEPER = to;
  }
  /**
   * generateReply given a successful login.
   * @param request
   * @param session
   * @param reply
   * @param voidme
   * @param fromweb
   * @return
   */
  private ActionReply genReply(ActionRequest request, LoginInfo linfo, PaymentReply reply, PaymentReply voidme, boolean fromweb, IPSpec fromip) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();

    switch(request.Type().Value()) {
      case ActionType.admin:{//used as a 'ping' by standin
        return ActionReply.Bogus("ping").setState(true);
      } //break;
      case ActionType.update:{
        try {
          UpdateRequest ur = (UpdateRequest)request;
          UpdateReply rep = applist.logUpdate(ur, fromip);
          return rep;
        } finally {
          if(SLEEPER) {//FOR SYSTEM DEBUG we kill ourselves for this long, so that appliance does not get an updateReply
            ThreadX.sleepFor(Ticks.forMinutes(30));
          }
        }
      } //break;
      case ActionType.batch: {
        return doBatchReply(db, (BatchRequest)request, linfo);
      } //break;
      case ActionType.clerkLogin: {
        return doClerkLogin(linfo);
      } //break;
//      case ActionType.receiptGet: {
//        return getReceipt((ReceiptGetRequest)request);
//      } //break;
      case ActionType.receiptStore: {
        ReceiptStoreRequest rsr= (ReceiptStoreRequest)request;
        //+_+ legacy bridge code to fix orphans:
        rsr.patch(linfo.terminalID(),request.requestInitiationTime);
        return doReceiptStore(rsr);
      } //break;
      case ActionType.connection:{
        return doConnection(db, (ConnectionRequest)request, fromip);
      } //break;
      // AuthMgr items:
      case ActionType.stoodin:{
        return doStandin(db, (StoodinRequest)request, linfo);
      } //break;
      case ActionType.store: {
        return doStoreReply(db, (StoreRequest)request,linfo, false /*auto*/);
      } //break;
      case ActionType.gateway: {
        return doGateway(db, (GatewayRequest)request);
      } //break;
      case ActionType.payment: {
        //logins now do all these lookups.
        if(!Terminalid.isValid(request.terminalid)) {//+_+ this should never be true
          // get it from the session
          request.terminalid = linfo.terminalID();
        }
        TerminalInfo ti = db.getTerminalInfo(request.terminalid);
        if((linfo.ti == null) && !fromweb){// don't set linfo.ti if txn from web!
          linfo.ti = ti;
        }
        return authmgr.doFinancial( (PaymentRequest) request, linfo.store, ti,
                                   linfo.associateid(), linfo.permissions,
                                   reply, voidme).setLegacy(request.legacy);
      } // break;
      default: {
        return ActionReply.For(request).setState(ActionReplyStatus.Unimplemented);
      }
    }
  }

  /**
   * @param reply - For client standins only.  This is the reply that the client gave the customer
   * @param voidme - For client standins only.  This means that the clerk voided the txn before it could be authorized.
   * @return a reply for a request.
   * This function should never send a reply that causes a client to go into standin unless the database is unaccessible,
   * in which case itis unlikely that the code would have gotten this far (although it is possible).
   */
  public final ActionReply generateReply(ActionRequest request, LoginInfo linfo, boolean relogin, PaymentReply reply, PaymentReply voidme, boolean fromweb, IPSpec fromip) {
    ActionReplyStatus arstat = null;
    try {
      dbg.Enter("generateReply");
      attempts.incr();
      if(linfo == null) {//this should never happen!
        dbg.ERROR("UserSession was null!");
        return new ActionReply(ActionReplyStatus.ServerError);
      }
      if(request == null) {
        dbg.ERROR("request was null!");
        return new ActionReply(ActionReplyStatus.GarbledRequest);
      }
      // the only time relogin is true is when txns are coming from txnservlet
      // (NOT adminservlet, so fromweb is ALWAYS false when relogin is true)
      if(!fromweb) {
        if (relogin) {
          if(linfo.terminalLogin(request)){
            //we are happy
          } else {
            // for legacy ...
            if(request.Type().is(ActionType.clerkLogin)) {
              return doClerkLogin(null);
            } else {
              return ActionReply.Bogus("Login failed").setState(ActionReplyStatus.InvalidLogin);
            }
          }
        }
      } else {
        dbg.VERBOSE("session login presumed current");
      }

      ActionReply ar=genReply(request,linfo,reply,voidme,fromweb,fromip);
      if(ar == null) {
        return ActionReply.Fubar(dbg.Location()+":ar finally null");
      } else {
        return ar;
      }

    } catch(Exception caught) {
      dbg.Caught(caught);
      return ActionReply.Fubar(dbg.Location()+":Exception:"+caught);
    } finally {
      completes.incr();
      dbg.Exit();
    }
  }

  private static final ConnSource toSource = new ConnSource(ConnSource.terminalObjects);

  /**
   * web reversals do not pass through here
   */
  public final String ReplyTo(String theObject, LoginInfo linfo, EasyProperties props, IPSpec remoteIP) {
    String ret=null;
    ActionReply reply = null;
    try {
      dbg.Enter("ReplyTo");
      // try it direct
      EasyCursor p = new EasyCursor();
dbg.ERROR("theObject:"+theObject+", linfo:"+linfo+", props:"+props);
      if(StringX.equalStrings(props.getString(ConnSource.class.getName()), toSource.Image())) {
        p.fromString(theObject, true);
      } else {
        // otherwise, try it from URL format
        if(props != null) {
          p.addMore(props);
        }
      }
      p.debugDump("Received properties:  ", ",", "=");
      ActionRequest request = ActionRequest.fromProperties(p);
      reply = generateReply(request, linfo, true /* cause only called from txnservlet */, null, null, false, remoteIP);
    } catch (Exception e) {
      dbg.Caught(e);
      if(reply == null) {
        dbg.WARNING("(and reply==null)");
        reply = new ActionReply(ActionReplyStatus.GarbledRequest);//+_+ invalid request or serverError...
      }
    } finally {
      try {
        if(reply == null) {
          dbg.WARNING("Leaving ReplyTo, and Reply==null !!  (sending back unknown)");
          reply = new ActionReply(ActionReplyStatus.UnknownException);
        }
        EasyCursor p2 = reply.toProperties();
        ret = String.valueOf(p2);
        dbg.VERBOSE("Outgoing properties:  " + ret);
      } catch (Exception t) {
        dbg.Caught(t);
      } finally {
        dbg.Exit();
        return ret;
      }
    }
  }

}

//$Id: ConnectionServer.java,v 1.28 2004/02/21 07:31:54 mattm Exp $

