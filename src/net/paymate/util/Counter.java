package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Counter.java,v $
 * Description:  Threadsafe increment and decrement (is this an issue?--nice to have handy)
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

public class Counter {
  private Monitor mon = new Monitor(Counter.class.getName());
  private long count = 0; // create a thread-safe counter class so that incrementing and getting are atomic +++
  private long min = 0;
  private long max = Long.MAX_VALUE;
  // +_+ make a constructor that starts with a specific value
  public Counter() {
    // default starts at zero
    // and has the limits [0:Long.MAX_VALUE]
  }

  public Counter(long start) {
    count = start;
  }

  public Counter(long min, long max) {
    this.min = min;
    this.max = max;
    norm();
  }

  public Counter(long min, long max, long start) {
    this(min, max);
    count = start;
    norm();
  }

  public final long max() {
    return max;
  }

  public final long min() {
    return min;
  }

  public final long value() {
    return count;
  }

  /**
   * @return ++count
   */
  public final long incr() {
    return chg(1);
  }

  /**
   * @return --count
   */

  public final long decr() {
    return chg(-1);
  }

  /**
   * interlock modify and read operations
   * @return modified value
   */
  public final long chg(long by) {
    try {
      mon.getMonitor();
      count+=by;
      norm();
    } catch (Exception ex) {
      // +++ bitch !!!
    } finally {
      long ret = count;
      //we must wait until after setting teh return value before freeing the monitor.
      mon.freeMonitor();
      return ret;
    }
  }

  /**
   * @return object after clearing
   */
  public final Counter Clear(){
    try {
      mon.getMonitor();
      count=0;
      return this;
    } catch (Exception ex) {
      // +++ bitch !!!
      return this;
    } finally {
      mon.freeMonitor();
    }
  }

  /**
   * normalize
   */
  private final long norm() {
    if(count < min) {
      count = max; // ??? roll over ???
    } else if(count > max) {
      count = min; // roll over
    }
    return count;
  }
}
//$Id: Counter.java,v 1.6 2001/10/03 23:56:04 mattm Exp $