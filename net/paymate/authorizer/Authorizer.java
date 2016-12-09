package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/Authorizer.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.20 $
 */

import net.paymate.data.*;
import net.paymate.database.*;
import net.paymate.database.ours.query.*;
import net.paymate.connection.*;
import java.io.*;
import java.util.*;
import net.paymate.util.*;
import net.paymate.net.*;
import net.paymate.web.*; // +++ NEED TO MOVE LOGININFO to DATA (or some of its guts)

public abstract class Authorizer implements Runnable {

  private static final ErrorLogStream dbg = new ErrorLogStream(Authorizer.class.getName());

  // Let's write a spec first !!

  /**
   * Needed:
   *  a name (String)
   *  an index to add to the end of the name in case we make multiples of this: (see util.Counter)
   *  a socket connection (A ManagedSocket)
   *  read and write threads for that socket
   *  a queue to put things into [includes the thread that left it here for waking up when done, along with the request and reponse]
   *  a status
   *  a cleaner (thread)
   *
   * Facilities:
   *  abort() -- abort the sending of a txn (causes the auth to void it if it comes back after this is called)
   *  add()   -- send a txn, puts in queue, notifies the writer thread to come get it
   *  remove()-- called by client thread after it is awakened, removes the item from queue
   *
   *  recd()  -- called by the reader thread when a reply is received, matches up the reply with a request, awakens the client thread
   *  send()  -- called by the writer thread to get a message for sending
   *
   *  init()  -- called by the AuthorizerManager to start the threads and make the socket connections [happens on a different (starter) thread?]
   */


  public static final String INVALID = "uninitialized";
  public int id = -1;
  public String name = INVALID;
  public EasyCursor myprops = null;

  public static Authorizer nullauthorizer = null;

  protected PayMateDB db = null;
  protected String hostname = "uninitialized";
  protected SendMail mailer = null;
  public Authorizer() { // required since we will be loading with the classname only!
  }

  // +++ mutex?
  // !!!! MUST OVERLOAD THIS IF YOU WANT IT TO WORK !!!!  THEN, MUST CALL THIS ONE (super.init(....)) !!!!!!
  public void init(int id, String name, PayMateDB db, String hostname, PrintStream backup, SendMail mailer) {
    this.id = id;
    this.name = name;
    logFileMon = new Monitor(name+"Logfile");
    this.db = db;
    this.hostname = hostname;
    this.mailer = mailer;
    makeLogfile(backup);
    pw().println(name+" inited.");
  }

  // OVERLOAD THIS IN THE EXTENDED CLASS & THEN CALL IT FIRST THING IN THAT FUNCTION !!!!!!!
  protected void loadProperties() {
    myprops = db.getServiceParams(name);
    // sequencercadjustment:
    long min = myprops.getLong("lowseq", 1); // sorta standard
    long max = myprops.getLong("highseq", 9999); // sorta standard
    sequencer = new Counter(min, max, sequencer.value()+100 /*for thread safety?*/); // just adjust the limits
    seqFmt = new Fstring((""+max).length(),'0'); // be sure to right justify it
  }

  /**
   * removes the moving thread to be notified upon reply receipt
   */
/*
  public boolean abort(Txnid txnid) {
    boolean ret = true;
// +++
    return ret;
  }
*/

  protected static final FinancialReply forTxnRow(TxnRow record) {
    FinancialReply ar = (FinancialReply)ActionReply.For(record.actionType(), record.refTime().getTime());
    ar.refTime= record.refTime();
    ar.setApproval(record.authidresponse);
    ar.tid= record.tid();
    ar.CATermID=record.cardacceptortermid;
    ar.setResponse(record.responsecode);
dbg.ERROR("ar = " + ar.toString());
    return ar;
  }

  protected static final int fakeAuth() {
    return fakeAuth(8);
  }

  protected static final int fakeAuth(int startDigit) {
    return ((startDigit*100000)+(int)(Safe.Now().getTime() % 99999));
  }

  // standin stuff ...
  protected StandinList stoodins = new StandinList();
  protected Thread standinAgent = null; // inited in the constructor
  private boolean runStandinAgent = true;
  public void run() { // DO NOT EXTEND THIS !!!
    // this function must NEVER exit unless the authorizer is being taken down PERMANENTLY (destroyed/finalized)
    // so make it very safe with try/catch, and maybe have it call another function instead of extending this
    // +++ make this chatty
    pw().println(name+" StandinAgent started.");
    while(runStandinAgent) { // +++ parameterize for destruction
      initStandin();
      try {
        while(true) { // +++ parameterize for destruction
          standinProcess();
          synchronized(stoodins) {
            try {
              stoodins.wait(Ticks.forMinutes(1)); // will be awakened with notify(), or will awaken once a minute (this can be the thing that tests the sockets to the auth once per minute?) +++ parameterize!
            } catch (InterruptedException ie) {
              // m-kay, what now?  log it?
            }
          }
        }
      } catch (Exception e) {
        dbg.Caught(e);
      }
    }
    pw().println(name+" StandinAgent exiting.");
  }

  public int queuedStandins() {
    return stoodins.Size();
  }

  /////////////////////////////////////
  // extend the following ...
  //
  /**
   *  background authorize them, one at a time
   */
  public abstract void standinProcess();
  //
  /**
   * load what you need to authorize from tables (includes txns that need to be stoodin now)
   */
  public abstract void initStandin();
  public abstract void handleResponse(ResponseNotificationEvent event); // for sending back the response to the authorization request
  /**
   * enqueues the request for authorization by this authorizer
   * so, this function receives the image of the txn record instead of a request? yes.
   * @param force inidicates that the client already stood it in, so we HAVE to try to process it, even if it exceeds limits
   */
  public abstract FinancialReply authorize(TxnRow record, TxnRow original, boolean force); // this function handles timeouts
  //
  // END extend
  /////////////////////////////////////

  private boolean down = false;
  public boolean shutdown() {
    if(down == false){
      down = true;
      markStateChange();
    }
    return isup();
  }
  public boolean bringup() {
    if(down == true) {
      loadProperties();
      startStandin(); // check placement of this
      down = false;
      markStateChange();
    }
    return isup();
  }
  // send an email out stating that the auth module was brought up or down.  DON'T send this to authorizer!
  private void markStateChange() {
    String line = hostname +" --> " + name + " service brought " + (isup() ? "UP" : "DOWN") + "! " + Safe.timeStampNow();
    mailAlert(line);
    println(line);
  }
//  public boolean shutdownFor(long millis) {
//    return down = true;
//  }
  public boolean isup() {
    return !down;
  }
  private void startStandin() {
    if(standinAgent == null) {
      standinAgent = new Thread(this, name+"StandinAgent");
      standinAgent.setDaemon(true);
      standinAgent.start();
    }
  }


  // sequencer stuff
  // 4.7 Field 7 - Transaction Sequence #
  // Incremented with wrap from 1 to 9999 with each auth request.
  // for now, we will use one for all terminals, as there is no way we will be getting 10000 txns in a few hours for a while
  // +++ soon, need to make this per-terminal
  private Counter sequencer = new Counter(0, 0);
  private Fstring seqFmt = null;
  private Monitor seqMon = new Monitor(name+"SequenceMonitor");
  protected void setNextSequence(TxnRow record) {
    try {
      seqMon.getMonitor();
      long number = (Safe.parseLong(record.stan) % (sequencer.max()-sequencer.min()+1))+sequencer.min();
      record.authseq = seqFmt.righted(Long.toString(number)).toString();
    } catch (Exception ex) {
      // +++ bitch
    } finally {
      seqMon.freeMonitor();
    }
  }


  // read/write and status info:
  public Accumulator writes = new Accumulator();
  public Accumulator reads = new Accumulator();
  public Accumulator txnTimes = new Accumulator();
  public Counter timeouts = new Counter();
  public Counter connections = new Counter();
  public Counter connectionAttempts = new Counter();

  // has no body
  protected final void mailAlert(String subject){
    mailAlert(subject, "");
  }

  protected final void mailAlert(String subject, String msgs){
    mailer.send(db.getServiceParam(name, "alertList", "alien@spaceship.com,alheilveil@austin.rr.com"), subject, msgs);
  }

  protected final void mailAlert(String subject, String msgs, String maillist){
    mailer.send(maillist, subject, msgs);
  }

  public LogFile logFile = null;
  protected PrintFork pf = null;
  protected Monitor logFileMon = null;

  protected void makeLogfile(PrintStream backup) {
    try {
      logFileMon.getMonitor();
      if(logFile == null) {
        logFile = new LogFile(name, false, backup);
        pf = logFile.getPrintFork(name);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      logFileMon.freeMonitor();
    }
  }

  public void println(String s) {
    if(pf != null) {
      try {
        pf.println(s);
      } catch (Exception e) {
        dbg.Caught(e);
      }
    } else {
      dbg.WARNING("pf is null");
    }
  }

  private PrintWriter pw = new AuthorizerPrintWriter(this);
  public PrintWriter pw() {
    return pw;
  }
}

class AuthorizerPrintWriter extends PrintWriter {
  Authorizer auth = null;
  public AuthorizerPrintWriter(Authorizer auth) {
    super((OutputStream)/*null*/System.out);
    this.auth = auth;
  }
  public void println(String toPrint) {
    auth.println(toPrint);
  }
}
