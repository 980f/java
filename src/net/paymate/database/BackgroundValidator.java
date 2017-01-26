package net.paymate.database;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/database/BackgroundValidator.java,v $</p>
 * <p>Description: Does after-validation, background validation for PayMateDB </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.13 $
 */

import net.paymate.util.Service;
import net.paymate.util.ServiceConfigurator;
import net.paymate.util.UTC;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.Ticks;
import net.paymate.util.DateX;
import net.paymate.data.TimeRange;
import net.paymate.lang.ThreadX;
import net.paymate.util.timer.StopWatch;
import net.paymate.util.Accumulator;

public class BackgroundValidator extends Service implements Runnable {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(BackgroundValidator.class);

  public static final String NAME = "DBBGValidator";

  public BackgroundValidator(ServiceConfigurator configger) {
    super(NAME, configger);
    initLog();
  }

  private Thread thread = null;
  public boolean isUp() {
    return thread!=null && thread.isAlive();
  }
  private boolean shouldstop = false;
  public boolean shouldstop() {
    return shouldstop;
  }
  public void up() {
    if(!isUp()) {
      shouldstop = false;
      loadConfigs();
      thread = new Thread(this, NAME);
      thread.start();
      // now it is running
    }
  }
  public void down() {
    if(isUp()) {
      shouldstop = true;
      thread.interrupt(); // in case it is asleep
    }
  }

  protected void loadConfigs() {
//    EasyProperties configs = configger.getAllServiceParams(serviceName());
  }

  public static final double DEFAULTSTARTSLEEPSECS = 60.0;// must be double; +++ get from configs?
  StopWatch sw = new StopWatch(false);

  public void run() {
    sw.Start();
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    try {
      println("starting background validation ...");
      loadConfigs();
      db.backgroundvalidate(reads, writes);
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      sw.Stop();
      println("run ended.");
    }
  }

  private Accumulator reads = new Accumulator();
  public String svcReads() {
    return printStats(reads);
  }
  private Accumulator writes = new Accumulator();
  public String svcWrites() {
    return printStats(writes);
  }
  public String svcCnxns() {
    return "1"; // only one of these in the system.
  }
  public String svcPend() {
    return isUp() ? "1" : "0";
  }
  public String svcAvgTime() {
    return DateX.millisToTime(sw.millis());
  }
  public String svcNotes() {
    return isUp() ?
        (shouldstop() ? "Shutting down ..." : PayMateDB.bgvalidatorStatus()):
        "Done " + (shouldstop() ? "[aborted]": "[completed]"); // +++ deal with "shouldn't be run at all"
  }
}
