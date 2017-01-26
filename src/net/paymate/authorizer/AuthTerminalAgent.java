package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthTerminalAgent.java,v $
 * Description:  Terminal Agent for an Authorizer.  One AuthTerminalAgent per authorizer+terminal.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.63 $
 */

import net.paymate.util.*; // QAgent, QActor, PriorityComparator
import net.paymate.data.*; // Terminalid
import net.paymate.awtx.*; // Realmoney
import net.paymate.database.PayMateDB;
import net.paymate.database.PayMateDBDispenser;
import net.paymate.lang.ThreadX;
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public abstract class AuthTerminalAgent implements QActor {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthTerminalAgent.class);

  private QAgent agent = null;
  private QAgent standin = null;
  protected AuthSocketAgent socketeer = null;
  protected /* probably some extension of this class */ AuthSocketAgent submitter = null;
  protected Authorizer authorizer = null;
  private Counter sequencer = new Counter(0, 0);
  private Monitor seqMon;
  private boolean lastFailed = false;
  private Accumulator centsQueued = new Accumulator();
  // load the following when "this" is created, and use and save it, without reloading every time
  public Counter termbatchnumer = null;  // batch numberer; needed occasionally by the authorizer when cleaning up
  private boolean submitting = false;
  private Monitor authSubmitMutex = null; // +++ use LibraryBook?
  public Terminalid myTerminalid = null;
  private AuthTerminalStandinAgent standinAgent;

  public AuthTerminalAgent(Authorizer authorizer, Terminalid term,
                           Counter sequencer, Counter termbatchnumer,
                           int fgThreadPriority, int bgThreadPriority) {
    this.authorizer = authorizer;
    this.myTerminalid = term;
    String name = String.valueOf(authorizer.serviceName())+"_T"+term.value();
    agent = QAgent.New(name, this, PriorityComparator.Normal(), fgThreadPriority);
    standinAgent = new AuthTerminalStandinAgent(10, agent); // +++ get tmieout from configs
    standin = QAgent.New(name+"bg", standinAgent, PriorityComparator.Normal(), bgThreadPriority);
    authSubmitMutex = new Monitor(name+"SubmitMutex");
    // sequencer
    seqMon = new Monitor(name+".Sequence");
    this.sequencer = sequencer;
    this.termbatchnumer = termbatchnumer;
    // start it
    agent.Start();
    standin.Start();
  }

  public String foregroundStatus() {
    return agent.status();
  }
  public String standinStatus() {
    return standin.status();
  }

  // sequencer stuff
  // 4.7 Field 7 - Transaction Sequence #
  // Incremented with wrap from LOW to HIGH with each auth request.
  // do something completely different when we implement paymentech 99+ ?
  public final void setNextSequence(PayMateDB db, TxnRow record) {
    if(ObjectX.NonTrivial(record) && !StringX.NonTrivial(record.authseq)){
      record.authseq = setNextSequence(db, record.terminalid(), record.txnid());
      dbg.ERROR("setNextSequence: "+record.authseq);
    }
  }
  /**
   * If you overload this, be sure to use the seqMon !!! (have to make it protected, I guess, or else use your own monitor, or else move some of the guts of this to a subfunction and use it)
   */
  private final String setNextSequence(PayMateDB db, Terminalid terminalid, Txnid txnid) {
    try {
      seqMon.getMonitor();
      // clear the sequencer
      sequencer.Clear();
      // read last value from the database & set the Counter
      sequencer.chg(db.getSequence(authorizer.id, terminalid));
      // incr the Counter
      int number = (int)sequencer.incr();
      // set the database
      db.setSequence(authorizer.id, terminalid, number, txnid);
      // return the counter value
      return Long.toString(number);
    } catch (Exception ex) {
      dbg.Caught("in setNextSequence",ex);
      return "";
    } finally {
      seqMon.freeMonitor();
    }
  }

  /* parameters: @@@ +++ [can wait until we do the 99+ version of Paymentech]
   *  Persistent: [T|F], whether or not to close the socket after it has done a single txn
   *  WriterHeartBeatIntervalMs: <1 means none
   *  PrioritizedPorts: one or more IP:port pairs for connecting
   */

  /**
   * Internal; used by QAgent.  Ignore.
   */
  public final void Stop() {
    // stub
  }

  /**
   * External; used by AuthTermAgentList.  HEED!
   */
  public final void stop() {
    standin.Stop();
    agent.Stop();
  }

  public final void runone(Object o) {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    if(o instanceof AuthTransaction) { // will gatewaytransaction go here?
      transact(db, (AuthTransaction)o);
    } else if(o instanceof AuthSubmitTransaction) {
      runoneSubmit(db, (AuthSubmitTransaction)o);
    } else {
      authorizer.PANIC("AuthTerminalAgent.runone() received object that was neither AuthTransaction nor AuthSubmitTransaction!");
    }
  }

  private final void transact(PayMateDB db, AuthTransaction authTran) {
    subtractTxnCents(authTran);
    boolean completed = false;
    boolean gateway = authTran.isGateway();
    AuthRequest request = authTran.request;
    TxnRow record = authTran.record;
    setNextSequence(db, record);
    request.fromRequest(authTran.record, authTran.original, authTran.merch);
    boolean voided = (record != null) && record.isVoided();
    try {
      if(request != null) {
        if(voided) {
          // don't auth an already-voided txn! (this will only be CC)
          completed = true;
          authTran.response.action = ActionCode.Declined;
          authTran.response.authmsg = "voided before authorized";
        } else {
          if(authorizer.isUp()) {
            authorizer.connectionAttempts.incr();
            if(socketeer == null) {
              socketeer = makeSocketAgent();
            }
            if(socketeer != null) {
              dbg.ERROR("About to send the request:" + authTran.request);
              socketeer.sendRequest(authTran); // puts it on the socket thread
              completed = !authTran.response.statusUnknown();
              authorizer.println("completed=" + completed);
              AuthSocketAgent asa = socketeer;
              socketeer = null;
              if(asa != null) {
                asa.kill();
              }
            } else {
              authorizer.println("inblocked is null!");
            }
          } else {
            dbg.ERROR("Can't perform authorization of txn '" + record.txnid +
                      "' since the authorizer is down!");
          }
        }
      } else {
        dbg.ERROR("Can't perform authorization of txn '" + record.txnid +
                  "' since AuthRequest is null");
      }
    } catch(Exception exception1) {
      authorizer.println("Failed to make connection to sppserver " + exception1);
      dbg.Caught(exception1);
    } finally {
      boolean wasServerStoodin = false;
      if(completed) {
        lastFailed = false;
        dbg.ERROR("Everything went perfectly! [right]");
        if(!gateway) {
          authorizer.markDone(db, authTran.record, authTran.response);
          // need to check to see if it was a void, and if so, void the original!
          if(authTran.record.isReversal() && authTran.response.isApproved() &&
             (authTran.original != null /* unfound gateways may pass*/)) {
            authorizer.stampVoided(db, authTran.original);
          }
          // this is a new notification so that we can see when standins are finalized, and what the result was
          dbg.VERBOSE("authed="+authTran.response.authed()+", wasStoodin="+authTran.wasStoodin()+", action="+authTran.response.action);
          if(authTran.response.authed() && authTran.wasStoodin()) { // was stoodin before and is completed now
            authorizer.PANIC("AUTHED SI: "+authTran.txnid()+" " +
                             (authTran.response.isApproved() ? "OK" :
                              (authTran.response.isDeclined() ? "LOSS!" : "UNKNOWN!")));
          }
        }
        // this chunk goes in the AuthTerminalAgent or AuthTransaction
        if(!voided && authTran.response.authed()) { //make a boolean fn on authTran.
          authorizer.connections.incr();
        }
      } else {
        if(!lastFailed) { //checking before setting only makes sense if we are going to do something on first failure; we do. the dbg stmt
          dbg.ERROR("first failure after a success");
          lastFailed = true;
        }
        wasServerStoodin = redoStandin(authTran); // if this was a standin attempt, recycle it
      }
      authorizer.logFile.flush();
      if(authTran == null) {
        dbg.ERROR("transact(): authTran is null!!!");
      } else if(authTran.isGateway()) { // note that locating this here assumes that we will never standin gatewayed txns
        dbg.VERBOSE(
            "transact(): authTran is gateway; removing $0 from centsQueued.");
      } else if(authTran.record == null) {
        dbg.ERROR("transact(): authTran.record is null!!!");
      } else if(wasServerStoodin) {
        dbg.WARNING("transact(): wasServerStoodin is true.");
      } else {
        dbg.VERBOSE(
            "transact(): this was a normal txn; removing it from centsQueued");
      }
      authTran.finish();
    }
  }

  // if this was a standin attempt, recycle it
  private final boolean redoStandin(AuthTransaction authTran){
    // was it a standin?
    if(authTran.canStandinAgain()) {
      if(authorizer.isUp()) {
        standin.putUnique(authTran);
        addTxnCents(authTran);
      }
      return true;
    }
    return false;
  }

  private final void addTxnCents(AuthTransaction authTran) {
    centsQueued.add(txnCents(authTran));
  }
  private final void subtractTxnCents(AuthTransaction authTran) {
    centsQueued.remove(txnCents(authTran));
  }
  private final void subtractTxnCents(long cents) {
    centsQueued.remove(cents);
  }
  private final long txnCents(AuthTransaction authTran) {
    return ((authTran==null) || authTran.isGateway() || (authTran.record==null)) ?
        0L :
        authTran.record.rawAuthAmount().Value();
  }

  public final Accumulator centsQueued() {
    return centsQueued;
  }

  // for live's
  // +++ The location of the contents of this function are suspect ...
  public final boolean attemptNow(PayMateDB db, AuthTransaction authTran) {
    boolean ret = attemptLater(db, authTran); // put it in the queue
    boolean socketeerError = false;
    if(ret) {
      // then put the client thread to sleep
      authorizer.println("waiting for " + authorizer.timeout + " millis.");
      boolean timedout = authTran.waitFor(authorizer.timeout);
      authorizer.txnTimes.add(authTran.timer.millis());// add the timer to the list
      if(timedout) {
        authorizer.timeouts.incr();
        authorizer.println("Thread timed out (a bad thing).  Killing socket ...");
        // kill the original socket (something bad happened)
        try {
          AuthSocketAgent mysocketeer = socketeer;
          if(mysocketeer!= null) {
            mysocketeer.kill();
            socketeerError = mysocketeer.createError;
          }
        } catch (Exception e) {
          dbg.Caught(e);
        }
        authorizer.println(".. socket killed.");
      } else {
        authorizer.println("Thread interrupted (a good thing).");
      }
      try {
        // the timing of this is nebulous; don't rely on it
        // probably put it in the AuthSocketAgent.
        if(socketeerError) {
          // send email notification!
          authorizer.PANIC("Error opening socket for txns.",
                           (authTran != null) ? "socketOpenAttemptCount = " +
                           authTran.socketOpenAttempts.value() : "");
        }
      } catch (Exception e) {
        dbg.Caught(e);
      }
    }
    return ret;
  }

  // for standins
  public final boolean attemptLater(PayMateDB db, AuthTransaction authTran) {
    boolean ret = false;
    try {
      authSubmitMutex.getMonitor();
      if(!submitting) { // only can do one at a time.
        if(!authTran.isGateway()) {
          db.stampAuthStart(authTran.record);
        }
        addTxnCents(authTran);
        agent.putUnique(authTran);
        ret = true;
      } else {
        dbg.ERROR("Can't auth now #" + authTran.txnid() + ", as the authterm is sending a batch");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      authSubmitMutex.freeMonitor();
    }
    return ret;
  }


  /**
   * Submit a batch for settlement
   */
  public final boolean submit(AuthSubmitTransaction submittal) {
    boolean ret = false;
    boolean error = false;
    try {
      authSubmitMutex.getMonitor();
      // +++ have it do the database work to create the batch and collect the txns BEFORE adding it to the queue,
      // +++ then do so regardless of what else is in it
      // +++ and get rid of the following if statement,
      // +++ and get rid of the SubmittalLock() (but keep the authSubmitMutex so that we don't submit twice at exactly the same time (collection nightmare))
      if(!submitting && (agent.Size() == 0)) {
        submitting = true;
        ret = true;
        agent.putUnique(submittal);
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      authSubmitMutex.freeMonitor();
      return ret;
    }
  }

  private final void runoneSubmit(PayMateDB db, AuthSubmitTransaction submittal) {
    // creates the batch record and sets needed fields in the submittal object
    // +++ @@@ %%% Do the next line before putting it in the list, NOT HERE !!!  (Do in above function, submit())
    switch(db.newBatch(submittal, authorizer.batchSequenceRange, termbatchnumer /* used, not incremented */, submittal.auto)) {
      case 0: {
        // don't need to submit; no records!  just log this; not unusual.
        authorizer.PANIC_NO_EMAIL("Unable to submit batch.  No records ready:"+submittal); // logs only; no email
      } break;
      case -1: {
        authorizer.PANIC("Unable to generate a new batch for submittal:"+submittal);
      } break;
      default: {
        submitter = authorizer.genSubmittalAgent();
        submitter.sendSubmittal(submittal); // on this thread (check the return value? +++)
authorizer.println("finishing batch ! [calling db.finishbatch]");
        db.finishBatch(submittal, authorizer.sequenceRange, termbatchnumer /* incremented */);
        submitter = null;
      }
    }
    releaseSubmittal();  // release the lock
  }

  public final void releaseSubmittal() {
    try {
      authSubmitMutex.getMonitor();
      submitting = false;
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      authSubmitMutex.freeMonitor();
    }
  }

  // for voiding from the queue
  public final int removeAny(Txnid txnid, RealMoney cents) {
    int ret = agent.removeAny(txnid);
    ret += standin.removeAny(txnid);
    for(int j = ret; j-->0;) {
      subtractTxnCents(cents.absValue()); //+_+ LedgerValue not threadsafe ???
    }
    return ret;
  }

  public abstract AuthSocketAgent makeSocketAgent();
}

class AuthTerminalStandinAgent implements QActor {
  private QAgent foreground;
  private int sleeptimeSeconds;
  public AuthTerminalStandinAgent(int sleeptimeSeconds, QAgent foreground) {
    this.foreground = foreground;
    this.sleeptimeSeconds = sleeptimeSeconds;
  }
  public void runone(Object fromq) {
    if(fromq instanceof AuthTransaction) {
      ThreadX.sleepFor(Ticks.forSeconds(sleeptimeSeconds)); // +++ get from configs for slowing standin spam
      foreground.putUnique(((AuthTransaction)fromq).bumpCounter()); // +++ we might need to check on the contents of the response to see ifsomeone is supposed to clear or wipe something
    } else {
      // +++ scream
    }
  }
  public void Stop() {
    //
  }

}