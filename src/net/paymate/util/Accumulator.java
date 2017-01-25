package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Accumulator.java,v $
 * Description:  I'm sure this statistics class exists somewhere!
 *               I just couldn't find it.
 *               >>I would be fairly surprised to find threadsafe statistics.
 *               most real statistics will not bother with long's, only will
 *               do doubles.
 *               >>I'd prefer a class name like "LongAverager". to match what I
 *               will someday import from my C++ stuff. Accumulator measn something
 *               else to us CPU jocks, such as having subtract as well as add.
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.6 $
 */

// +++ use 2 Counters.java's?  One for count and one for sum?

public class Accumulator {

  private static final Counter nameCounter = new Counter();

  public Accumulator() {
    mon = new Monitor("Accumulator." + nameCounter.incr());
  }

  // this is for initializing an Accumulator, so that we can pick up using it where we left off
  public Accumulator(long count, long sum) {
    this();
    set(count, sum);
  }

  private long count = 0;
  private long sum = 0;
  private long max = 0;
  private long min = Long.MAX_VALUE;
  private Monitor mon = null;

  public void add(long lasttime) {
    try {
      mon.getMonitor();
      count++;
      sum += lasttime;
      max = Math.max(max, lasttime);
      min = Math.min(min, lasttime);
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
    }
  }

  // add another accumulator in
  public void add(Accumulator that) {
    try {
      mon.getMonitor();
      count+=that.count;
      sum += that.sum;
      max = Math.max(max, sum / count);
      min = Math.min(min, sum / count);
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
    }
  }

  // this is for initializing an Accumulator, so that we can pick up using it where we left off
  private void set(long count, long sum) {
    try {
      mon.getMonitor();
      this.count = count;
      this.sum = sum;
      max = getAverage();// +++ ???
      min = getAverage();// +++ ???
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
    }
  }

  /**
   * @param lasttime - what to subtract from the total
   * Count is auto-decremented by 1.  Don't change the sign of lasttime before subtracting it.
   * Can use this for when you add() something in a moment before you find out that you should not have.
   * useful when this is extended into a moving sum rather than an accumulator.
   */
  public void remove(long lasttime) {
    try {
      mon.getMonitor();
      if(count>0){
        --count;
        sum -= lasttime;
      }
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
    }
  }

  public long getTotal() {
    long ret = -1;
    try {
      mon.getMonitor(); // longs are not atomic
      ret = sum;
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
      return ret;
    }
  }
  public long getCount() {
    long ret = -1;
    try {
      mon.getMonitor(); // longs are not atomic
      ret = count;
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
      return ret;
    }
  }
  public long getAverage() {
    long ret = -1;
    try {
      mon.getMonitor(); // longs are not atomic
      ret = (count > 0) ? (sum / count) : 0;
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
      return ret;
    }
  }
  public long getMax() {
    long ret = -1;
    try {
      mon.getMonitor(); // longs are not atomic
      ret = max;
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
      return ret;
    }
  }
  public long getMin() {
    long ret = -1;
    try {
      mon.getMonitor(); // longs are not atomic
      ret = min;
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
      return ret;
    }
  }
}
//$Source: /cvs/src/net/paymate/util/Accumulator.java,v $
