package net.paymate.authorizer;

import java.util.Vector;
import net.paymate.ISO8583.data.*;// MOVE THIS ! +++
import net.paymate.util.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/StandinList.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

public class StandinList {

  private static final ErrorLogStream dbg = new ErrorLogStream(StandinList.class.getName());

  // +++ generalize this class and put a BASE of it in Util?

  private Vector contents = new Vector();

  // REMOVES the item, so put it back if it needs handling!
  public synchronized SrvrStandinRecord Next() {
    SrvrStandinRecord ssr = null;
    if(contents.size() > 0) {
      ssr = itemAt(0);
      contents.remove(ssr); // we'll add it back in later if we need to
    }
    return ssr;
  }

  // so that we can check this against the limits
  public synchronized long ttlCentsFor(int storeid) {
    long ret = 0;
    for(int i = contents.size(); i-->0;) {
      SrvrStandinRecord sr = itemAt(i);
      // +++ we are calculating absolutes here, not just oustanding $, what is the policy?
      if(sr.tid.caid == storeid) {
        ret += sr.cents;
      }
    }
    return ret;
  }

  /**
   * Attempts to find tid in the list.  If it is there, removes it from the list.
   */
  public synchronized boolean voidtxn(TransactionID tid) {
    boolean ret = false;
    SrvrStandinRecord sr = find(tid);
    if(sr != null) {
      contents.remove(sr);
      ret = true;
    }
    return ret;
  }

  private synchronized SrvrStandinRecord find(TransactionID tid) {
    for(int i = contents.size(); i-->0;) {
      SrvrStandinRecord sr = itemAt(i);
      if(sr.tid.equals(tid)) {
        return sr;
      }
    }
    return null;
  }

  private synchronized SrvrStandinRecord itemAt(int i) {
    SrvrStandinRecord sr = null;
    try {
      Object o = contents.elementAt(i);
      sr = (SrvrStandinRecord)o;
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return sr;
    }
  }

  public synchronized void add(TransactionID tid, long cents) {
    contents.add(new SrvrStandinRecord(tid, cents));
  }

  /**
   * DO NOT USE FOR ITERATING! because it exposes this extension of ObjectFifo to incoherence by exposing its size() function.
   * @return number of items in fifo. No guarantee that it won't be different the next time you look.
   */
  public synchronized int Size(){
    return contents.size();
  }

}

// $Id: StandinList.java,v 1.9 2001/11/17 06:16:57 mattm Exp $
