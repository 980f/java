package net.paymate.connection;
/**
* Title:        $Source: /cvs/src/net/paymate/connection/ConnectionServer.java,v $
* Description:  Manages the server communications (Actions)<p>
* Copyright:    2000 PayMate.net<p>
* Company:      paymate<p>
* @author       paymate
* @version      $Revision: 1.198 $
*/

import  net.paymate.net.*;
import  net.paymate.util.*;
import  net.paymate.ISO8583.factory.*;
import  net.paymate.ISO8583.data.*;
import  net.paymate.awtx.RealMoney;

import  javax.servlet.http.*;
import  net.paymate.util.timer.*;

import  net.paymate.web.UserSession;
import  net.paymate.database.ours.query.*;
import  net.paymate.database.*;

import  java.sql.*;
import  net.paymate.jpos.data.*;
import  java.io.*;
import  java.util.Date;//must be explicit to override sql.date
import  java.util.*;

import  net.paymate.data.*; // TimeRange
import  net.paymate.net.SendMail;

import net.paymate.authorizer.*;
import net.paymate.authorizer.cardSystems.*;


public class ConnectionServer {
  protected static final ErrorLogStream dbg=new ErrorLogStream(ConnectionServer.class.getName());

  private String servicename = "CONNECTIONSERVER";
  public String hostname = "unspecified";
  public PrintFork problemLog=null;
  public LogFile logFile=null;

  public AuthManager authmgr = null;
//  private SendMail mailer = null;
  private boolean inited = false;
  public boolean isUp() {
    return inited;
  }
  public final void init(String hostname, PayMateDB db, SendMail mailer, boolean preloadAllAuths /* +++ get from configs */) {
    this.hostname = hostname;
//    this.mailer = mailer;
    receiptFilePath = db.getServiceParam(servicename, "receiptsPath", receiptFilePath);
    authmgr = new AuthManager(db, hostname, /*PrintStream*/null /* +++ fix this and use one that can print to a dbg! */, mailer, preloadAllAuths);
    logFile = new LogFile("problems", false);
    problemLog = logFile.getPrintFork();
    problemLog.println("Init, OK");
    inited = true;
    mailer.send(db.getServiceParam(servicename, "bootupList", "alien@spaceship.com,alheilveil@austin.rr.com"), hostname+" ConnServer.init() at GMT:"+Safe.timeStampNow(), "have a nice day :)");
    ApplianceTracker.init(hostname, mailer, db);
  }

  /**
  * @return informix update error code.
  */
  public final int closeDrawer(UserSession session,TerminalID T, TimeRange ranger){
    // +++ when we have switched to using batch numbers (batchid's or bmid's),
    // +++ don't stamp any that are still in progress (actioncode = null or tranendtime = null)
    // @@@ use the return id value from an update to get the id for stamping the tranjour records!
    return session.db.update(session.db.genCloseShift(T,ranger,session.linfo));
  }

  // +++++ combine this with Unsettled Transaction Format & the new data provider/consumer model.
  final ActionReply doBatchReply(BatchRequest request,UserSession session) throws java.sql.SQLException{
    dbg.Enter("doBatchReply");
    Statement stmt = null;
    BatchReply bratch=null;
    try {
      boolean amClosing = request.isClosing;
      //      dbg.VERBOSE("tid");
      // +++ change this to close THEN get range 0,1 if closing
      TerminalID T= new TerminalID(Safe.parseInt(request.terminalId), session.linfo.storeid);
      //      dbg.VERBOSE("ranger");
      TimeRange ranger= session.db.getPendingRange(T);
      //a hole opened up between a 'now' used in the "pendingRange" above and the terminal's concept of 'now'
      // +++ --- +_+ %%% !!!
      // NO! This cause WILDLY wrong values to go into the system, potentially.
      // We didn't see it at Taco during the install since the taco machines are CDT.
      // I DID see it on Bill's machine since his isn't set to teh right time.
      // You CANNOT rely on the appliance to have ANY sense of time _AT_ALL_ !!!
      // (It is now 4:45 AM CDT [11:45PM UTC], and Bill's appliance says it is 11:45 PM CDT!!!)
      //      ranger.setEnd(request.requestInitiationTime);//overwrite 'server.Now()'
      // +++ Using batch numbers stamped on the txns will prevent this being a problem.  We then just query for isEmpty(batchnumber), or ==-1 if -1 is default.
      bratch=BatchReply.New(ranger);
      bratch.ranger.setFormatter(session.linfo.ltf);
      // moved the closing from below the report, just in case
      if(amClosing){//&& we think we succeed in formatting all data
        closeDrawer(session,T,ranger);
      }
      TerminalInfo tinfo = session.db.getTerminalInfo(Safe.parseInt(request.terminalId));
      stmt = session.db.query(session.db.genBatchQuery(session.linfo.storeid,tinfo.getNickName(),ranger,true));
      if(stmt != null) {
        ResultSet rs = session.db.getResultSet(stmt);
        while(PayMateDB.next(rs)) {
          BatchLineItem blight=new BatchLineItem();
          TxnRow rec=TxnRow.NewOne(rs);
          blight.date = rec.refTime();
          TransactionID tid = rec.tid();
          blight.stan= tid.stan();
          blight.card= rec.card().accountNumber;
          blight.TypeColData = rec.cardType();
          blight.saleamount= rec.netamount();
          bratch.addItem(blight);
        }
        bratch.close(session.linfo.terminalName, amClosing, session.linfo.clerk.Name());
        bratch.status.setto(ActionReplyStatus.Success);
      } else {
        bratch.fubar("Couldn't access reference times");
      }
    } catch(Exception arf){
      dbg.VERBOSE("caught"+arf);
      if(bratch!=null){
        bratch.fubar(arf.getLocalizedMessage());
      }
    }
    finally {
      session.db.closeStmt(stmt);
      dbg.Exit();
      return bratch;
    }
  }

  public Accumulator receiptsRead = new Accumulator();
  public Accumulator receiptsWritten = new Accumulator();

  protected final ActionReply getReceipt(ReceiptGetRequest rsr ,UserSession session){
    ReceiptGetReply ar=null;

    TextList errs = new TextList();
    FileInputStream fis = null;
    String receipt = null;

    try {
      String filename = session.db.getReceipt(rsr.tid, Safe.parseInt(rsr.terminalId));//full path
      if(!Safe.NonTrivial(filename)){//try to find an orphaned receipt
        TxnRow rec = session.db.getTranjourRecordfromTID(rsr.tid, rsr.terminalId);
        filename = getOrphanReceipt(session.db, rec);
        dbg.WARNING("Hoping for orphaned receipt:"+filename+ " for:"+rsr.tid.image());
      }

      if(filename != null) {
        File file = new File(filename);
        if(file != null) {
          ByteArrayOutputStream baos = new ByteArrayOutputStream();
          fis = new FileInputStream(file);
          Streamer.swapStreams(fis, baos); // +++ check return value
          receipt = baos.toString();
          receiptsRead.add(receipt.length());
        } else {
          errs.add("Unable to locate receipt file on disk.");
        }
      } else {
        String msg = "Could not find receipt: " + rsr.tid.image();
        dbg.VERBOSE(msg + " for terminal: " + rsr.terminalId);
        errs.add(msg);
      }
    } catch (Exception trs) {
      String msg = "Exception attempting to retrieve receipt: " + rsr.tid.image();
      //            dbg.Message();
      dbg.Caught(msg,trs);
      errs.add(msg);
    } finally {
      if(fis != null) {
        try {
          fis.close(); // just in case
        } catch (Exception tfis) {
          dbg.WARNING("Exception closing file input stream.");
        }
      }
    }
    ar= new ReceiptGetReply(receipt);
    ar.status.setto((receipt!=null) ? ActionReplyStatus.Success : ActionReplyStatus.FailureSeeErrors); // cop out
    if(errs != null) {
      ar.Errors = errs; // +_+ append instead? ok as is, could create ar sooner.
    }
    return ar;
  }

  public final String getOrphanReceipt(PayMateDB db, TxnRow rec) {
   //orphans are named swipetime_terminalId_sistan.txt
    int origterminalid = db.getOriginalTerminal(rec);
    int stanvalue= rec.siStan();
    TransactionID sitid= TransactionID.NewOffline(rec.clientreftime,stanvalue,origterminalid);
    return makeReceiptFileRef(sitid).getAbsolutePath();
  }

  public String receiptFilePath = File.separator + "receipts";// + File.separator;

  // These will possibly, eventually be in the database, so I'm having this class deal with storing them ...
  public final File makeReceiptFileRef(TransactionID real){
    //the tid might be of a standin, we lose that distinction here:
    String filename = real.image('_') + ".txt";
    return new File(receiptFilePath, filename);
  }

  final ActionReply doReceiptStore(ReceiptStoreRequest rsr, UserSession session){
    ReceiptStoreReply ar= new ReceiptStoreReply();
    FileOutputStream fos = null;
    TransactionID real=rsr.tid;
    dbg.Enter("deReceiptStore");
    try {
      if(rsr.tid.isStandin()){ //must find real tid for standin one.
        real=session.db.findStandin(rsr.tid, session.linfo);
        //if not found then we get real=rsr.tid, the standin id.
      }
      File file = makeReceiptFileRef(real);//!!! made sharable
      if(file.exists()) {
        if(!file.renameTo(new File(file.getAbsolutePath()+".backup."+Safe.timeStampNow()))) {
          ar.Errors.add("Unable to create receipt file '"+file.getPath()+"'; exists and couldn't be backed up (in use?)");
        } else {
          //we are unsure of what file object holds if rename fails so:
          file = makeReceiptFileRef(real); // set it back
        }
      }
      //      if(file.createNewFile()) {
        dbg.VERBOSE("saving receipt as:"+file.getAbsolutePath());
        fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        //add in standin info to receipt image??
        //... no, only the receipt query web page displays that info
        String rcptstr = rsr.receiptString();
        receiptsWritten.add(rcptstr.length());
        ps.print(rcptstr);
        ps.close();
        // stuff the things into the database; get errors back if not okay
        ar.Errors.appendMore(session.db.logReceipt(real, file.getPath(), rsr.clerk.Name(), rsr.terminalId));
      //      }
      //      else {//this fires off even when the file is saved dammit.
        //        //OR we are tyring to save it twice/
        //        ar.Errors.add("Unable to create receipt file:"+file.getPath());
      //      }
    } catch (Exception trs) {
      dbg.Caught("Error attempting to store receipt",trs);
    } finally {
      Safe.Close(fos);
      ar.storedOk = !TextList.NonTrivial(ar.Errors);
      ar.setState(true);//--- always succeed, receiptor better check storedOk
      dbg.Exit();
    }
    return ar;
  }

  /**
  * @param arstat is the status of the login check performed on most requests
  * @param session is for database access for clerk options
  */
  final ActionReply doClerkLogin(ActionReplyStatus arstat,UserSession session){
    // check to see if the login was any good
    LoginReply lr = null;
    if(arstat != null) {
      lr = new LoginReply(arstat.Value());
      if(arstat.is(ActionReplyStatus.Success)) {
        String packedcaps= session.linfo.permissions;
        lr.clerkCap.canMOTO=  false;//packedcaps.indexOf('M')>=0;
        //+++ check letter to privilege mappings, is there a class for these?  see net.paymate.web.UserPermissions and search for Pvoid in net.paymate.web.UserSession
        lr.clerkCap.canClose= packedcaps.indexOf('V')>=0;
        lr.clerkCap.canVOID=  packedcaps.indexOf('V')>=0;
        lr.clerkCap.canREFUND=packedcaps.indexOf('R')>=0;
      }
    }
    return lr;
  }

  final ActionReply doUpdate(UpdateRequest request, UserSession session){
    dbg.Enter("doUpdate");
    UpdateReply newone=new UpdateReply();
    try {
      //someday will investigate system settings here
      newone.opt.period=(int) Ticks.forMinutes(3);//---just to see it change
      newone.opt.txnHoldoff= (int)Ticks.forSeconds(57.0);//+++ should come from slowest authorizer for store.
      newone.setState(ActionReplyStatus.Success);
      logUpdate(request, session);
    } finally {
      dbg.Exit();
      return newone;
    }
  }

  final void logUpdate(UpdateRequest request, UserSession session){
    ApplianceTracker.logUpdate(request);
    dbg.WARNING("preping for appliance " + request.applianceId);
    authmgr.prepAuther(request.applianceId);
  }

  final ActionReply doConnection(ConnectionRequest request, UserSession session){
    dbg.Enter("doConnection");
    try {
      ConnectionReply cr = session.db.getApplianceInfo(request.applianceId);
      logUpdate(request, session);
      if(cr == null) {
        problemLog.println("Intruder alter! applianceid = " + request.applianceId); // can use to determine new appliances in the field
        // +++ auto-insert into appliances table?
        return ActionReply.For(request).setState(ActionReplyStatus.InvalidAppliance);
      }
      return cr;
    } catch (Exception ex) {
      return ActionReply.For(request).setState(ActionReplyStatus.ServerError);
    } finally {
      dbg.Exit();
    }
  }

  /**
  * process something that the client did a standin for.
  */
  protected final ActionReply doStandin(StoodinRequest srek,UserSession session){//pronounced "shreck"
    dbg.Enter("doStandin");
    StoodinReply ar=new StoodinReply(srek.recordID);//this id EXISTS to link the reply with the request, do not use for anything else.
    try {//only financial requests are (supposed to be) stood in for:
      if(srek.voidme!=null){//+++ someday create a voided txn record. +++ NEED TO LOG THESE TO PROVE PEOPLE AREN'T COMITTING FRAUD!!
        ar.setState(true);
      } else {
        ActionReplyStatus clerkcheck=session.login(srek.request);
        boolean snookered=clerkcheck.is(ActionReplyStatus.InvalidLogin)|| clerkcheck.is(ActionReplyStatus.InsufficientPriveleges);
        if(snookered){
          dbg.ERROR("we accepted an invalid clerk login");
          problemLog.println("We accepted an invalid clerk login for a standin! ...");
          problemLog.println(srek.toString());
        }
        //%%% +++ fix security hole ???  What hole?  Need more info.  Need suggested solutions.  We should recheck the embedded login so that not just anyone can do this for anyone.
        //don't recheck embedded login:
        FinancialReply buried= (FinancialReply) generateReply(srek.request, session, false, srek.reply, srek.voidme);
        //above cast only fails if request is way hopelessly borked.
        if(buried.Succeeded()){//remember that DECLINED txns succeed.
          ar.status.setto(ActionReplyStatus.Success);//ignores errors in standin flag setting +_+
        } else {
// +++ log this; it should never happen !!!
//          if(buried.ComFailed()){
//            ar.status.setto(buried.status.Value());
//          } else {
problemLog.println("FATAL! Couldn't handle the transaction!  Should never happen! ...");
problemLog.println(srek.toString());
            ar.Errors.appendMore(buried.Errors);
            ar.status.setto(ActionReplyStatus.ServerError);//@#@ this return is what hangs requests in client
//          }
        }
// already in table (or should be)
        //record "real" auth transaction info
        //session.db.updateStoodin(StandinRecord.New(session.linfo.terminalID, buried.tid, srek.reply.tid, srek.request.clerk ));
      }
    } catch(Exception caught){
      String stamp = Safe.timeStampNow();
      dbg.Caught(stamp, caught);
      ar.Errors.add("Caught:"+stamp);
      ar.status.setto(ActionReplyStatus.ServerError);
    } finally {
      //and deal with an error here?
      if (!ar.Succeeded()&&!ar.ComFailed()) {
        ////goes into the nonexistent standin failures log.
// store in problems list, let client wash its hands of this.
        problemLog.println("STANDIN,Failed,");
        problemLog.println(srek.toString());
        problemLog.println(",");
        problemLog.println(ar.toString());
      }
      dbg.Exit();
      return ar;
    }
  }

  public Counter completes = new Counter();

  /**
   * @param reply - For client standins only.  This is the reply that the client gave the customer
   * @param voidme - For client standins only.  This means that the clerk voided the txn before it could be authorized.
   * @return a reply for a request.
   * This function should never send a reply that causes a client to go into standin unless the database is unaccessible,
   * in which case itis unlikely that the code would have gotten this far (although it is possible).
   */
  public final ActionReply generateReply(ActionRequest request, UserSession session, boolean relogin, FinancialReply reply, ReversalReply voidme) {
    ActionReply ar = null;
    ActionReplyStatus arstat = null;

    try {
      dbg.Enter("generateReply");
      if(session == null) {
        // this should never happen!
        dbg.ERROR("UserSession was null!");
        ar = new ActionReply(ActionReplyStatus.ServerError);
      } else if(request == null) {
        ar = new ActionReply(ActionReplyStatus.GarbledRequest);
      } else if(relogin && request.fromHuman()) {
        // only for txnservlet stuff, not for adminservlet stuff
        // only certain ActionTypes (initiated by a human) need to be logged in.
        //relogin should only be false when a session is sure it has already done a login
        arstat = new ActionReplyStatus(session.login(request));
        if(!arstat.is(ActionReplyStatus.Success)) {
          ar=ActionReply.For(request).setState(arstat);//BAD CHOICE###setState() used a buggy aspect of trueenum.
          problemLog.println("LOGIN, FAILED,");
          problemLog.println(request.toString());
          problemLog.println(", response:");
          problemLog.println(ar.toString());
        }
      }
      if(ar == null) {
        switch(request.Type().Value()) {
          case ActionType.update:{
            ar= doUpdate((UpdateRequest)request,session);
          } break;
          case ActionType.batch: {
            ar= doBatchReply((BatchRequest)request,session);
          } break;
          case ActionType.clerkLogin: {
            ar = doClerkLogin(arstat,session);
            if(!ActionReply.Successful(ar)){//useful for testing problog.
              problemLog.println("LOGIN, CLERK, FAILED,");
              problemLog.println(ar==null?" null reply" :ar.toString());
            }
          } break;
          case ActionType.receiptGet: {
            ar= getReceipt((ReceiptGetRequest)request, session);
          } break;
          case ActionType.receiptStore: {
            ar= doReceiptStore((ReceiptStoreRequest)request, session);
          } break;
          case ActionType.connection:{
            ar= doConnection((ConnectionRequest)request,session);
          } break;
          // AuthMgr items:
          case ActionType.stoodin:{
            ar = doStandin((StoodinRequest)request, session);
          } break;
          default: {
            if(request.isFinancial()) {
              ar= authmgr.doFinancial((FinancialRequest)request, session, reply, voidme);
            } else {
              ar = ActionReply.For(request).setState(ActionReplyStatus.Unimplemented);
            } break;
          }
        }
      }
    } catch(Exception caught) {
      dbg.Caught(caught);
      if(ar==null){
        ar= ActionReply.For(request).setState(ActionReplyStatus.FailureSeeErrors);
      }
      ar.fubar(dbg.Location()+":Exception:"+caught);
    } finally {
      if(ar == null) {
        ar = ActionReply.Fubar(dbg.Location()+":ar finally null");
      }
      if(ar.Succeeded()) {
        completes.incr();
      }
      dbg.Exit();
      return ar;
    }
  }

  private final ConnSource toSource = new ConnSource(ConnSource.terminalObjects);
  public Counter attempts = new Counter();

  public final String ReplyTo(HttpServletRequest req, String theObject, UserSession session, EasyCursor props) {
    String ret=null;
    ActionReply reply = null;
    try {
      dbg.Enter("ReplyTo");
      attempts.incr();
      // try it direct
      EasyCursor p = new EasyCursor();
      if(req.getParameter(ConnSource.class.getName()).equals(toSource.Image())) {
        p.fromString(theObject, true);
      } else {
        // otherwise, try it from URL format
        if(props != null) {
          p.addMore(props);
        }
      }
      p.debugDump("Received properties:  ", ",", "=");
      ActionRequest request = ActionRequest.fromProperties(p);
      reply = generateReply(request, session, true /* cause only called from txnservlet */, null, null);
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
        ret = p2.toString();
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

//$Id: ConnectionServer.java,v 1.198 2001/11/17 20:06:36 mattm Exp $

