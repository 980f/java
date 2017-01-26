package net.paymate.authorizer;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/AuthTransaction.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.28 $
 */

import net.paymate.util.*; // UTC
import net.paymate.data.*; // Txnid
import net.paymate.database.ours.query.*; // TxnRow
import net.paymate.util.timer.*; // StopWatch
import net.paymate.awtx.RealMoney;
import net.paymate.lang.ThreadX;
import net.paymate.data.sinet.business.*;

public class AuthTransaction extends AuthorizerTransaction implements Comparable {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(AuthTransaction.class);

//  /**
//   * @todo if we follow through on the following then genTransaction can be in the base class and NOT copied a zillion times.
//   */
//  public static AuthTransaction makeClass(Class extension,TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch){
//    return extension.newInstance().init(record, original, storeid, slim, merch);
//  }

  public AuthTransaction init(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    this.storeid = storeid;
    this.record = record;
    this.original=original;
    this.slim = slim;
    this.merch = merch;
    notifyMon = new Monitor("Notify"+(record != null ? record.txnid : "")); // +++ put in superclass?
    return this;
  }

  public boolean isGateway() {
    return false;
  }

  public AuthTransaction(TxnRow record, TxnRow original, Storeid storeid, StandinLimit slim, MerchantInfo merch) {
    init(record, original, storeid, slim, merch);
  }

  public Storeid storeid = null;
  public TxnRow record = null;
  public TxnRow original = null;//tranny that we will modify
  public StandinLimit slim = null;
  public MerchantInfo merch = null;

  private Monitor notifyMon;  // used for callbacks
  public AuthResponse response = null;
  public AuthRequest request = null;
//  public boolean wasStoodin = false; // use a function on the request?
  private final UTC created = UTC.Now();

  private Counter standinRetryCount = new Counter(0, 1000); // used for recycling standins
  public AuthTransaction bumpCounter() {
    standinRetryCount.incr();
    return this;
  }
  public boolean canStandinAgain() {
    return ((standinRetryCount.getRollover() == 0) && (standinRetryCount.value() != 0)); // put these calls into one function in Counter or extension +_+
  }

  public Txnid txnid() {
    return this.record.txnid();
  }

  public Terminalid terminalid() {
    return this.record.terminalid();
  }

  public boolean wasStoodin() {
    return (record!=null) && record.wasStoodin();
  }

  /**
   * Compares this object with the specified object for order.  Returns a
   * negative integer, zero, or a positive integer as this object is less
   * than, equal to, or greater than the specified object.<p>
   */
  public int compareTo(Object o){
    // "this" is obviously an AuthTransaction, but is other?
    // if not, or if it is null, then this is more important
    if((o == null) || !(o instanceof AuthTransaction)) {
      return 1; // this is greater than other
    }
    AuthTransaction other = (AuthTransaction) o;
    // live is more important than stoodin
    if(!wasStoodin() && other.wasStoodin()) {
      return 1;
    }
    if(!wasStoodin()) { // live txn ordering
      if(this.created.getTime() > other.created.getTime()) {
        return 1;
      }
      if(this.created.getTime() < other.created.getTime()) {
        return -1;
      }
      // times are the same (unusual)
      // just let the priority queue handle it
    } else { // stoodin txn ordering
      // just let the priority queue handle it, for now
      // +++ later order by: the one that hasn't been tried in a long time goes first
    }
    return this.request.compareTo(other.request);
  }

  public boolean equals(Object o) {
    Txnid thisTxnid = this.txnid();
    if(thisTxnid != null) {
      Txnid txnid = null;
      if(o instanceof AuthTransaction) {
        AuthTransaction at = (AuthTransaction)o;
        txnid = at.txnid();
      } else if(o instanceof Txnid) {
        txnid = (Txnid)o;
      }
      return thisTxnid.equals(txnid);//moved null handling code into the equals()
    }
    return false;
  }

  public final StopWatch timer = new StopWatch(false); // don't start until we say so.
  /**
   * @return true if NOT notified [if timedout].
   */
  public boolean waitFor(long timeoutMs) {//use np.util.Waiter
    timer.Start();
    boolean timedout = ThreadX.waitOn(this, timeoutMs);
    timer.Stop();
    return timedout;
  }
  private boolean aborted = false;
  private boolean done    = false;
  public boolean aborted() {
    return aborted;
  }
  public void abort() {
    try {
      notifyMon.getMonitor();
      if(!done) {
        aborted = true;
      }
      awaken();
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      notifyMon.freeMonitor();
    }
  }
  private void awaken() {
    ThreadX.notify(this);
  }
  public void finish() {//processResponse
    try {
      notifyMon.getMonitor();
      if(!aborted) {
        done = true;
      }
      awaken();
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      notifyMon.freeMonitor();
    }
  }
}
