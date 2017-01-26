package net.paymate.util;

/**
 * Title:        $Source: /cvs/src/net/paymate/util/Accumulator.java,v $
 * Description:  I'm sure this statistics class exists somewhere!
 *               I just couldn't find it.
 *               "I would be fairly surprised to find threadsafe statistics."
 *               'well, these are.'
 *               "most real statistics will not bother with long's, only will do doubles."
 *               'we can change that if it makes you feel better, but i see no reason to use a double'
 *               "I'd prefer a class name like 'LongAverager' to match what I
 *               will someday import from my C++ stuff. Accumulator measn something
 *               else to us CPU jocks, such as having subtract as well as add."
 *               'Since this provides more statistics than just average,
 *               I would prefer something like StatsAccumulator.
 *               But, this does have subtract (remove).'
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

// @todo use 2 Counters.java's?  One for count and one for sum? NO: must lock summing and counting with single lock.

public class Accumulator implements isEasy {

  private static final Counter nameCounter = new Counter();

  public Accumulator() {
    mon = new Monitor("Accumulator." + nameCounter.incr());
  }

  public String toString(){
    return String.valueOf(this.sum)+Ascii.bracket(count);
  }

  public String toSpam() {
    return "Cnt:"+getCount()+"/Ttl:"+getTotal()+"/Avg:"+getAverage()+"/Min:"+getMin()+"/Max:"+getMax();
  }


  // this is for initializing an Accumulator, so that we can pick up using it where we left off
  public Accumulator(long count, long sum) {
    this();
    set(count, sum);
  }

  private long count = 0;
  private long sum = 0;
  private long max = Long.MIN_VALUE;//formerly 0, which didn't allow for negatives
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
      that.mon.getMonitor();//gotta gain ownersup of both objects
      count+=that.count;
      sum += that.sum;
      max = Math.max(max, that.max);//formerly used average!
      min = Math.min(min, that.min);
    } catch (Exception e) {
      // +++ bitch
    } finally {
      that.mon.freeMonitor();
      mon.freeMonitor();
    }
  }

  // this is for initializing an Accumulator, so that we can pick up using it where we left off
  private void set(long count, long sum) {
    try {
      mon.getMonitor();
      this.count = count;
      this.sum = sum;
      max = getAverage();// +_+ should make a function for restoring all four members.
      min = getAverage();// +_+ this is nicer than using extreme values.
    } catch (Exception e) {
      // +++ bitch
    } finally {
      mon.freeMonitor();
    }
  }

  public void zero() {
    set(0, 0);
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
    try {
      mon.getMonitor(); // longs are not atomic
      return sum;
    } finally {
      mon.freeMonitor();
    }
  }

  public long getCount() {
    try {
      mon.getMonitor(); // longs are not atomic
      return count;
    } finally {
      mon.freeMonitor();
    }
  }

  public long getAverage() {
    try {
      mon.getMonitor(); // longs are not atomic
      return (count > 0) ? (sum / count) : 0;
    } catch (Exception e) {
      return -1;//there is NO reaosnable return at this point.
    } finally {
      mon.freeMonitor();
    }
  }

  public long getMax() {
    try {
      mon.getMonitor(); // longs are not atomic
      return max;
    } finally {
      mon.freeMonitor();
    }
  }

  public long getMin() {
    try {
      mon.getMonitor(); // longs are not atomic
      return min;
    } finally {
      mon.freeMonitor();
    }
  }
//////////////////////////
// isEasy()
  public void save(EasyCursor ezp){
    try {
      mon.getMonitor(); // longs are not atomic
      ezp.setLong("sum",sum);
      ezp.setLong("num",count);
      ezp.setLong("min",min);
      ezp.setLong("max",max);
    } finally {
      mon.freeMonitor();
    }
  }

  public void load(EasyCursor ezp){
    try {
      mon.getMonitor(); // longs are not atomic
      sum=ezp.getLong("sum");
      count=ezp.getLong("num");
      min=ezp.getLong("min");
      max=ezp.getLong("max");
    } finally {
      mon.freeMonitor();
    }
  }

}
//$Id: Accumulator.java,v 1.13 2004/02/21 07:31:54 mattm Exp $
