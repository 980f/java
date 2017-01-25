package net.paymate.authorizer.cardSystems;


/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/cardSystems/CardSystemsAuth.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.34 $
 */

import java.io.*;
import java.net.*;
import java.util.*;
import net.paymate.net.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import java.security.cert.*;
import net.paymate.authorizer.*;
import net.paymate.connection.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.*;
import net.paymate.ISO8583.data.*;
import net.paymate.data.*;
import net.paymate.web.*; // +++ NEED TO MOVE LOGININFO to DATA (or some of its guts)

public class CardSystemsAuth extends Authorizer {

  private boolean lastFailed = false;

  public static final String name = "not set";

  private static final ErrorLogStream dbg = new ErrorLogStream(CardSystemsAuth.class.getName());
  private static final String STR_LOG_FILE_NAME = "cardsystems.log";
  private IPSpec ips[] = null;
  public int bufferSize;
  public String strConnectIPAddress;
  public int connectPort;

  private long timeout = 35000; // guessing, but get from configs

  public Vector inblockedList = new Vector(); // just to keep up with them for reporting ...

  /**
   *   connectIPAddress=207.247.99.117 (for
   *   connectPort=1020
   *   bufferSize=1024
   *   timeout=35000
   */

  public CardSystemsAuth() {
    // see superclass
  }

  public void init(int id, String name, PayMateDB db, String hostname, PrintStream backup, SendMail mailer) {
    super.init(id, name, db, hostname, backup, mailer);
    bringup(); // must do this last thing in each authorizer module!
  }

  // OVERLOAD THIS IN THE EXTENDED CLASS & THEN CALL IT FIRST THING IN THAT FUNCTION !!!!!!!
  protected void loadProperties() {
    super.loadProperties();
    try {
      timeout = Long.parseLong(myprops.getProperty("timeout", ""+timeout));
      connectPort = Integer.parseInt(myprops.getProperty("connectPort", "2010"));
      bufferSize = Integer.parseInt(myprops.getProperty("bufferSize", "1024"));
      strConnectIPAddress = myprops.getProperty("connectIPAddress", "127.0.0.1");//"207.247.99.117 207.247.99.117"); // need to handle getting multiples
      // multiple IP's or domain names are separated with a space " "
      try {
        StringBuffer sb = new StringBuffer(strConnectIPAddress);
        String token = null;
        TextList ipNames = new TextList();
        while(Safe.NonTrivial(token=Safe.cutWord(sb))) {
          dbg.WARNING("Added ip address: " + token);
          ipNames.add(token);
        }
        ips = new IPSpec[ipNames.size()];
        for(int i = ips.length; i-->0;) {
          ips[i] = new IPSpec(ipNames.itemAt(i), connectPort);
        }
      } catch (Exception caught){
        dbg.Caught("failure while reading ip list - ", caught);
      }
      dbg.WARNING(""+ips.length+" ip address added for ["+strConnectIPAddress+"].");
    } catch (Exception e) {
      dbg.Caught("Exception attempting to load properties: ", e);
    }
  }

  //+_+ replace with dynamically selected authorizer timezone.
  static LocalTimeFormat CardSysTimeZone=LocalTimeFormat.New(TimeZone.getTimeZone("America/Chicago"), "MM/dd/yyyy HH:mm:ss");
  // LTF's must be mutexed!
  static final Monitor emailltfmon = new Monitor("EmailLTFMon");
  final void yellForAuthHelp(TxnRow tj, String hostname){
    String subject = hostname +" --> " + name + " cnxn failed! "+Safe.timeStampNow();
    TextList msgs = new TextList();
    msgs.add("Barbara Brown, this is an automated message from paymate's txn server (" + hostname + ").");
    msgs.add("The following transaction was in progress when we lost communications with your server.");
    msgs.add("Can you please reply to this message, telling us whether or not that txn was authorized (we hope not)?");
    msgs.add("Thank you!");
    msgs.add("");
    if(tj != null) {
      msgs.add("txn= "+tj.stan);
      msgs.add("seq= "+tj.authseq);
      msgs.add("terminal= "+tj.authtermid);
      msgs.add("amount= "+tj.transactionamount);
      msgs.add("last 4 digits of card= "+tj.cardGreeked());
      msgs.add("[YYYYMMDDhhmmss GMT]= "+tj.transtarttime);
      try {
        emailltfmon.getMonitor();
        msgs.add("[" + CardSysTimeZone.getFormat() + ' '+CardSysTimeZone.getZone().getDisplayName()+"]= "+CardSysTimeZone.format(PayMateDB.tranUTC(tj.transtarttime)));
      } catch (Exception e) {
        dbg.Caught(e);
      } finally {
        emailltfmon.freeMonitor();
      }
    }
    println("About to send this email message: " + subject);
    String msg = msgs.asParagraph();
    println(msg);
    mailAlert(subject, msg);
  }

  // This is mutexed cause there *could* be two threads running through this.
  // If those two threads are for the same txn, we don't want them to collide
  // +++ probably need to standardize this somehow in Authorizer.java so that all authorizers share this idea.
  private Monitor notifyMon = new Monitor(CardSystemsAuth.class.getName()+"Notify");
  public void handleResponse(ResponseNotificationEvent event) {
    CSResponseNotificationEvent csevent = (CSResponseNotificationEvent) event;
    try {
      notifyMon.getMonitor();
// +++ @@@ is this a dup ??? (is towaken null?)
// +++ @@@ dump to problems log
      csevent.socketeer.towaken.interrupt();
      csevent.socketeer.towaken = null; // well, it's null now!
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      notifyMon.freeMonitor();
    }
  }
// +++ can I kill the following crap?  Search it for pearls.
/*
//      String isoreq = "";
//      String isoresp = "";
//      Message m = null;
//      if(request.isReversal()) {
//        //find a transaction record and make a reversal message for it.
//        //@param tid is id of the reversal we are building
//        ReversalRequest request2 = (ReversalRequest)request
//        TxnRow rec = request2.byTID()?
//        session.db.getTranjourRecordfromTID(request2.toBeReversed, request.terminalId):
//        null //findExactly(request2.failed)
//        ;
//        if(rec != null) {
//          // +++ see if the txn is reversable (has not settled) !!!
//          TerminalInfo originalti = session.db.termInfoFromTranjour(rec);
//          m = Create.ForReversal(tid, originalti, rec);
//        }
//      } else {
//        m = Create.ForRequest(request, tid, ti);
//      }
//      if(m == null) {
//        return ar=ActionReply.For(request).setState(ActionReplyStatus.UnableToCreateTransaction);
//      }
//      isoreq = m.toString();
//      boolean posted = MessageQueue.postRequest(m, session.linfo.companyName, session.linfo.terminalName);
//      if(!posted) {
//        return ar=ActionReply.For(request).setState(ActionReplyStatus.UnableToCreateTransaction);
//      }
//
//      // sleep for the standard timeout period
//      boolean interrupted = false;
//      if(interrupted = (!ThreadX.sleepFor(timeout))) {
//        dbg.WARNING("InterruptedException occurred while txn thread was sleeping (probably okay).");
//      }
//      // get the reply
//      ar = MessageQueue.end(!interrupted);
//      // if the txn wasn't done, return with a stoodin auth after checking to see if we are over limit or not.
//      if(false) { // +++
//        ar.setState(ActionReplyStatus.ReplyTimeout); // +++ change the Response !!!
//      }
//      if(ar.status.is(ActionReplyStatus.HostTimedOut)) {
//        // on a timeout, send a GOINTOSTANDIN message? NO client has already gone into standin.
//        // however, we could have still gotten a good response, so check it +++
//        if(request.isReversal()){
//          // already have a good reply for it
//          //we extracted tranjour data from the message reply rather than tranjour directly.
//        } else {    //spontaneously reverse it
//          // +++ check to see if it worked anyway !!!!
//          if(!ar.status.is(ActionReplyStatus.HostTimedOut)) {
//            // try it!++++++++++++++++++++++++++
//          } else {
//            // but for now, just reverse it
//            dbg.WARNING("Timed out waiting on response from authorizer; reversing ...");
//            //the reversal is a separate transaction from what it reverses
//            TransactionID rtid = session.db.newtransaction(storeid);
//            if(!rtid.isComplete()) {
//              dbg.WARNING("[auto-reversal]: Could not getNextStanForStore(\"" + storeid + "\").");
//              ar.setState(ActionReplyStatus.DatabaseQueryError);
//              ar.Errors.add("DatabaseQueryError[auto-reversal]: could not find STAN from storeid");
//            } else {
//              m=Create.ForReversal(rtid, ti, m); // reversing the txn we just did with same terminal info
//              posted = MessageQueue.postRequest(m, session.linfo.companyName, session.linfo.terminalName); // post the reversal, then forget about it
//              // generate a response that the original transaction timed out
//              ar=ActionReply.For(request).setState(ActionReplyStatus.HostTimedOut); // --- superfluous?
//            }
//          }
//        }
//      } else {//probably a good transaction
//        // --- deal with the mainsail screwedness:
//        if(request.isReversal() && ar.status.is(ActionReplyStatus.Success)) {
//          // check to see if it actually should have been something else
//          TxnRow rec2 = session.db.getTranjourRecordfromTID(TransactionID.New("", ar.Stan(ar), session.linfo.storeid), session.linfo.terminalID);
//          if(rec2.eventnumber.equalsIgnoreCase("EVNT0012") && rec2.servername.equalsIgnoreCase("mssi100")) {
//            ar.Response=new ResponseCode("36");
//            ar.status.setto(ActionReplyStatus.Success);
//            // what else?
//          }
//        } else {
//          session.db.markClientTime(tid,request);
//        }
//      }
*/

  private FinancialReply standin(TxnRow record, TxnRow original /* for reversals */) {
    FinancialReply areply = null;
    dbg.WARNING("Standing in for txn: " + record.tid().image()); //email notify happens elsewhere
    try {
      // put into standin agent's list !!!
      stoodins.add(record.tid(), record.rawamount().Value());
      // create a good reply since it was stoodin
      areply = forTxnRow(record);
      areply.setState(ActionReplyStatus.Success);
      areply.setResponse("00"); // because there is no response code or auth on the record yet
      // awake the standin agent
      synchronized(stoodins) {
        stoodins.notify();
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return areply;
    }
  }

  // This happens when the system starts!  Not before or after!
  private boolean standinbeeninit=false;
  public void initStandin() {
    if(!standinbeeninit){
      db.getStoodins(stoodins, name);
      standinbeeninit=true; //follows above so that exceptions leave us NOT init.
    }
  }

  public void standinProcess() {
    try {
      if(!isup()) {
        return;
      }
      // find out which txns need authorizing and authorize them, one at a time (serially)
      // we don't want to iterate forever even if stuff is still coming in so we check
      //size and only do that many then quit even if the fifo is not empty
      for(int abunch=stoodins.Size(); abunch-->0 && isup();) {//
        SrvrStandinRecord srec = stoodins.Next();
        if(srec!=null){
          // now process it ...
          // 1) Get the tranjour record from ... tranjour
          TxnRow record = db.getTranjourRecordfromTID(srec.tid, null);
          if(record == null) {
            dbg.ERROR("standinProcess:RECORD = NULL!!!! VERY BAD !!!!");
            TextList mailtext = new TextList();
            mailtext.add("Please fix it ASAP!");
            mailtext.add(record.toString());
            mailAlert(hostname + " error selecting tranjour record for txn#: " + record.tid().image(), mailtext.asParagraph());
            // put it at the back of the queue
            stoodins.add(srec.tid, srec.cents);
          } else {
            // 2) if it is a reversal, get the original ... from tranjour
            TxnRow original = null;
            if(record.isReversal()) {
              TransactionID tido = TransactionID.New("", record.originalstan, record.storeid());
              original = db.getTranjourRecordfromTID(tido, null);
              // +++ check to see if we can still void the original (did someone else do it, etc.?)
              // +++ also, is it null?
            }
            // 3) Try to send it using your own thread.  (This handles everything for us.)
            authorize(record, original, true /* force==true since we ALREADY stoodin the txn! */);
            // 5) Sleep for a bit so as not to spam
            ThreadX.sleepFor(Ticks.forSeconds(1));
          }
        } else {
          // no problem; someone just voided a txn out of the queue
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  private byte [] requestToBytes(TxnRow record, TxnRow original) {
    byte [] bar = null;
    try {
      VisaBuffer vba = MAuthRec.fromRequest(record, original);
      if(vba == null) {
        dbg.ERROR("VisaBuffer [vba] is null!");
      } else {
        bar = vba.packet();
      }
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      return bar;
    }
  }

  // +++ these are the responses we have received from CS before:
  /*
  00 - OK
  01 - CALL / Refer to issuer
  04 - HOLD-CALL / Pick up card
  05 - DECLINE / Do not honor
  12 - INVALID TRANS / Invalid Transaction
  13 - AMOUNT ERROR / Invalid amount
  14 - CARD NO. ERROR / Invalid card number
  15 - NO SUCH ISSUER / No such issuer
  30 -     <!this is a format error, but is not listed in CS response codes!>
  43 - HOLD-CALL / Pick up card - Stolen
  51 - DECLINE / Insufficient funds
  54 - EXPIRED CARD / Expired card
  55 - WRONG PIN / Incorrect PIN
  57 - SERV NOT ALLOWED / Transaction not permitted - Card
  61 - DECLINE / Exceeds withdrawal limit
  62 - DECLINE / Invalid service code, restricted
  78 - NO ACCOUNT / No Account
  91 - NO REPLY / File is temporarily unavailable
  M4 - PLEASE TRY AGAIN / Default message for timeout
  */
  // +++ go through their whole list and find all responsecodes and decide how to handle each! +++
  private FinancialReply replyFromBytes(byte [] reply, TxnRow record, TxnRow original /*for reversals*/, boolean force) {
    FinancialReply areply = forTxnRow(record);
    try {
      VisaL0Response response = null;
      if(reply !=null){
        VisaBuffer vb = MAuthRec.forReply();
        vb.append(reply);// vb's were designed for directly receiving serial data
        response = MAuthRec.responseFrom(vb);
      }
      areply.setState(true);
      String authcode = ""+fakeAuth();// fake an authcode here
      String donetime = db.forTrantime(Safe.Now());
      // move this chunk into parent class +++
dbg.ERROR("response = " + response);
      if(response== null) { // (probably timed out!) and what else should be stoodin?
        ResponseCode whyCantStandin = new ResponseCode("00");
        if(!force) {
          whyCantStandin = canStandin(record);
        }
        if(whyCantStandin.isApproved()) {
          // move this chunk into parent class +++
          // -1 is our version of authdown, I guess +++ use standard ones?
          int changed = 0;
          if(!Safe.NonTrivial(record.clientreftime)) { // don't write over an old standin
            record.clientreftime=donetime; // clientreftime
            changed++;
          }
          if(!Safe.NonTrivial(record.stoodinstan)) { // don't write over an old standin
            changed++;
            record.stoodinstan = authcode;
          }
          boolean newStandin = (changed > 0);
          if(newStandin) {
            db.stampAuthStandin(record); // stamp the database with the fake authcode & time
          }
          standin(record, original); // and put this into standin (the original gets removed)
          areply.setResponse("00");
          //areply.Errors.add("Approved: "+authcode);
          // send an email about this (if we didn't already somewhere else) ------
          // but only send this email ONCE!
          if(newStandin) {
            mailAlert(hostname + " standing in on txn#: " + record.tid().image());
          }
        } else { // otherwise, yell for help since this txn likely DID occur, and we want to void it or reverse it, which we will do manually in the short term.
          // +++ later, we will do automatic reversals; for now, decline it
          yellForAuthHelp(record, hostname); // this should say that the txn should never have occurred, not that it was a dup
          authcode = ""; // DO NOT REMOVE! for use outside this block
          record.actioncode = whyCantStandin.actionCode();
          record.tranendtime = donetime; // tranendtime is the "we are done" field - DONE!
          record.authidresponse = authcode;
          record.hostresponsecode = "";
          record.responsecode = whyCantStandin.toString();
          db.stampAuthDone(record, original); // stamp the database
          areply.setState(true);
          areply.Errors.add(whyCantStandin.ExtendedDescription());
          areply.setResponse(record.responsecode);
        }
      } else { // everything went perfectly
        authcode = response.authcode; // DO NOT REMOVE! for use outside this block
        // move this chunk into parent class +++
        // RESPONSE = respcode=00, authcode=223161, authmsg=APPROVAL  223161.
        areply.setResponse(response.respcode); // DON'T move this!  The next line depends on it!
        record.actioncode = areply.Response.actionCode(); // DONE! +++ change from the old mainsail ISO response codes to the new Maverick ones.
        record.authidresponse = response.authcode;
        record.hosttracedata = response.authRefNumber;
        record.hostresponsecode = response.respcode; // ??? +++
        record.responsecode = response.respcode; // ??? +++
        record.tranendtime = donetime; // tranendtime is the "we are done" field - DONE!
        db.stampAuthDone(record, original); // stamp the database
        if(!"A".equals(record.actioncode)) {
          areply.Errors.add(response.authmsg);
        }
      }
      areply.CATermID = record.cardacceptortermid;
      areply.tid = record.tid();
      areply.setApproval(authcode);
      // then, return with success (we don't want the client to go into standin)
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      // +++ standin if anything is wrong with areply ... if you can
      return areply;
    }
  }

  public FinancialReply authorize(TxnRow record, TxnRow original, boolean force) {
    // first, update tranjour with the sequence number we are going to use ...
    // +_+ need a Safe.modulus(,);
    FinancialReply areply = null;
    try {
      String now = db.forTrantime(Safe.Now());
      // if this is a void, need to check to see if the original txn is in the standin queue, and then deal with it.
      if(record.isReversal() && (original != null) /* otherwise will fail anyway */ && stoodins.voidtxn(original.tid())) {
        ResponseCode rc = new ResponseCode("00");
        // voided it out of the list, so no need to proceed.
        String respcode = rc.toString(); // means voided before auth'd
        // +++ put all of the common stuff into a separate function!
        // mark the original as done, and change the authorizer to PAYMATE
        original.actioncode = rc.actionCode();
        original.authidresponse = "000000";
        original.hostresponsecode = respcode;
        original.responsecode = respcode;
        original.tranendtime = now;
        db.stampAuthDone(original, null); // stamp the database; NOT null!
        db.changeAuth(original, Authorizer.nullauthorizer.id);
        // mark the void record as done
        record.actioncode = rc.actionCode();
        record.authidresponse = "000000";
        record.hostresponsecode = respcode;
        record.responsecode = respcode;
        record.tranendtime = now;
        db.stampAuthDone(record, original); // stamp the database
        db.changeAuth(record, 0/*"PAYMATE"*/);
        // then send back an approval
        areply = forTxnRow(record);
        areply.setState(true);
      } else {
        // +_+ what if the original txn is being transacted while we speak?  dunno.  suggestions welcome.
        if(record.isReversal() || force || !exceedsTxnLimit(record)) {
          byte [] request = requestToBytes(record, original);
          // +++ test return value?
          record.authstarttime = db.forTrantime(Safe.Now()); // stamp the start time (setit back if the auth isn't successful)
          db.stampAuthStart(record);
          byte [] reply = transact(request, record); // do the transaction
          areply = replyFromBytes(reply, record, original, force);
          // +++ test return value?
        } else { // not a reversal, not already stoodin, and exceedsTxnLimit
          // decline, as it exceeds the limits
          ResponseCode rc = new ResponseCode("61");
          String respcode = rc.toString(); // means it exceeded the limit
          record.actioncode = rc.actionCode();
          record.authidresponse = "";
          record.hostresponsecode = respcode;
          record.responsecode = respcode;
          record.tranendtime = now;
          // mark the void record as done
          db.stampAuthDone(record, null /* null since this can't be a void */); // stamp the database
          db.changeAuth(record, Authorizer.nullauthorizer.id);
          // then send back an approval
          areply = forTxnRow(record);
          areply.setState(true);
          areply.Errors.add(rc.ExtendedDescription());
        }
      }
    } catch (Exception e) {
      dbg.Caught(e);
      // +++ make a real one?  yell for help?
    } finally {
      if(areply == null) {
        String subject = hostname +" --> " + name + " txn ["+record.tid()+"] failed in a bad way! "+Safe.timeStampNow();
        println("About to send this email message: " + subject);
        mailAlert(subject); // this shouldn't go to card systems
        areply.setState(ActionReplyStatus.ConnectFailed);  // +_+ send back a reply that causes the client to standin this txn (or the server).
      }
      return areply;
    }
  }

  byte [] transact(byte [] request, /* for logging/reporting --> */ TxnRow record) {
    byte [] myresult = null;
    InBlockedSocket inblocked = null;
    try {
      if(isup()) {
        connectionAttempts.incr();
        inblocked = new InBlockedSocket(ips/*findIPS()*/, bufferSize, this, hostname);
        if(inblocked != null) {
          inblockedList.add(inblocked);
          // inblocked.setDaemon(true);
          StopWatch timer = new StopWatch();  // set the timer
          inblocked.sendRequest(request);
          println("waiting for " + timeout + " millis.");
          boolean timedout = ThreadX.sleepFor(timeout);
          txnTimes.add(timer.Stop());// stop the timer & add the timer to the list
          if(timedout) {
            timeouts.incr();
            println("Thread timed out (a bad thing).  Killing socket ...");
            // kill the original socket (something bad happened)
            inblocked.kill();
            println(".. socket killed.");
          } else {
            println("Thread interrupted (a good thing).");
          }
          inblockedList.remove(inblocked);
          if((inblocked.reply != null) && (inblocked.reply.length > 0)) {
  dbg.ERROR("EEK: reply = ["+new String(inblocked.reply)+"]");
            connections.incr();
          }
          myresult = inblocked.reply;
        } else {
          println("inblocked is null!");
        }
      } else {
        dbg.ERROR("Can't perform authorization of txn '" + record.tid() + "' since the authorizer is down!");
      }
    } catch(Exception exception1) {
      println("Failed to make connection to sppserver " + exception1);
      dbg.Caught(exception1);
    } finally {
      if(myresult == null) {
        if(!lastFailed) {
          lastFailed = true;
        }
      } else {
        lastFailed = false;
      }
      logFile.flush();
      return myresult; // don't yell for help in here
    }
  }

  // +++ share the logic here between the client and server standin stuff
  // returns a response code of "00" or something else
  ResponseCode canStandin(TxnRow record) {
    String twoChars = "00";
    if(record.isReversal() || record.isReturn()) { // check to see if the txn type is standinable
      twoChars = "36";
    } else if(exceedsTxnLimit(record)) { // check to see if the txn exceeds the max transaction limit
      twoChars = "61";
    } else if(exceedsMaxSILmt(record)) { // check to see if the txn exceeds the max standin limit +++
      twoChars = "61"; // +++ use a better code here
    } else if(exceedsMaxTtlSILmt(record)) { // check to see if the txn exceeds the max total standin limit
      twoChars = "61"; // +++ use a better code here
    }
    return new ResponseCode(twoChars);
  }

  // +++ streamline these to use a single query ...

  private boolean exceedsTxnLimit(TxnRow record) {
    boolean ret = true;
    try {
      ret = (db.getStoreMaxTxnLimit(record.storeid())*100) < record.rawamount().Value();
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return ret;
    }
  }

  private boolean exceedsMaxSILmt(TxnRow record) {
    boolean ret = true;
    try {
      ret = (db.getStoreMaxSITxnLimit(record.storeid())*100) < record.rawamount().Value();
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return ret;
    }
  }

  private boolean exceedsMaxTtlSILmt(TxnRow record) {
    boolean ret = true;
    try {
      ret = (db.getStoreMaxSITtlTxnLimit(record.storeid())*100) < (record.rawamount().Value() + stoodins.ttlCentsFor(record.storeid()));
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return ret;
    }
  }

}
