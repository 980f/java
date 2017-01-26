package net.paymate.database;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/BackupJob.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.2 $
 */

// +++ Make a terse log file with only the following fields reported when a backup starts and completes.
// +++ Make a verbose log file (maybe just dbquery) that contains details of what happened.

import java.util.*;
import java.io.*;
import net.paymate.util.*;
import net.paymate.util.timer.*;
import java.util.zip.*;

public class BackupJob {

/**
 * This class mostly acts as a job definition and status reporter for a BackupAgent.
 * The job itself is run by the BackupAgent that it is handed to (created in).
 * The code that does the actual backup is in here, though,
 * even though the BackupAgent's thread is what runs through it.
 */

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(BackupJob.class);
  private Date reqTime = null;
  private String reqWho = null;
  private String backupPath = null;
  private String filename = null;
  private long jobNumber = -1;
  private int threadPriority = Thread.NORM_PRIORITY;
  private long perRowSleepMs = 0; // 0 acts as a yield
  private int width = -1;
  private String table = null;
  private DBMacros db = null;
  public boolean done = false;
  public boolean succeeded = false;
  public boolean started = false;
  public TextList errors = new TextList();
  private Accumulator wrote = new Accumulator();
  private StopWatch sw = new StopWatch(false); // +++ keep up with these somewhere for later reporting?

  /**
   * @param backupPath - path to backup directory, cannot be null
   * @param tables - list of tables to backup, cannot be null
   * @param number - job number assigned by BackupAgent
   * @param reqWho - who requested the backup, can be null
   * @param reqTime - when it was request, can be null
   */
  public static final BackupJob New(DBMacros db, String backupPath, String table, long number, int threadPriority, long perRowSleepMs, Date reqTime, String reqWho) {
    return new BackupJob(db, backupPath, table, number, threadPriority, perRowSleepMs, reqTime, reqWho);
  }

  public String name() {
    return "" + reqWho + "@" + reqTime + " [" + jobNumber + "]"; // works even if they are all null
  }

  public String toString() {
    return name();
  }

  private Monitor mutex = null;

  /**
   * This function just performs checks & mutexes for safety.  doBackup() does the work.
   */
  public void run() {
    int oldPriority = Thread.currentThread().getPriority();
    try {
      mutex.getMonitor();
      started = true;
      Thread.currentThread().setPriority(threadPriority);
      // check for situations that absolutely prevent a job from getting created ...
      if(db == null) {
        complain("null DB!", false);
        return;
      }
      if(!Safe.NonTrivial(backupPath)) {
        complain("null backupPath!", false);
        return;
      }
      if(!Safe.NonTrivial(table)) {
        complain("null tablename!", false);
        return;
      }
      File buPath = new File(backupPath);
      buPath = new File(buPath, ""+Safe.timeStamp(reqTime));
      buPath.mkdirs(); // ignore result, cause ...
      if(!buPath.exists()) {
        complain("could not create backupPath directory", false);
        return;
      }
      if(!buPath.isDirectory()) {
        complain("backupPath is not a directory", false);
        return;
      }
      doBackup(buPath);
    } catch(Exception e) {
      complain("Exception running: " + e, false);
    } finally {
      done = true;
      mutex.freeMonitor();
      Thread.currentThread().setPriority(oldPriority);
    }
  }

  public final void complain(String about, boolean okay) {
    dbg.ERROR("BackupJob " + name() + ": " + about); // +++ sounds like a function call for these two
    errors.add(about);
    succeeded &= okay;
  }

  public Date reqTime() {
    return reqTime;
  }
  public String reqWho() {
    return reqWho;
  }
  public long jobNumber() {
    return jobNumber;
  }
  public int threadPriority() {
    return threadPriority;
  }
  public long perRowSleepMs() {
    return perRowSleepMs;
  }
  public String table() {
    return table;
  }
  public long duration() {
    return sw.millis();
  }
  public Accumulator wrote() {
    return wrote;
  }
  public int width() {
    return width;
  }
  public String filename() {
    return Safe.TrivialDefault(filename, "");
  }
  public long filesize() {
    long filesize = 0;
    if(file != null) {
      try {
        filesize = file.length();
      } catch (Exception e) {
        dbg.Caught(e);
      }
    }
    return filesize;
  }

  private File file = null;

  private void doBackup(File path) {
    PrintStream ps = null;
    FileOutputStream fos = null;
    GZIPOutputStream gzip = null;
    try {
      // create the output file
      file = new File(path, table+".unl.gz"); // should never be unique thanks to the job number
      filename = file.getAbsolutePath();
      try {
        fos = new FileOutputStream(filename);
        dbg.ERROR("Backing up " + table + " to file: " + filename);
        gzip = new GZIPOutputStream(fos); // +++ maybe make this an option?
        ps = new PrintStream(gzip);
      } catch (Exception e) {
        dbg.Caught(e);
        dbg.ERROR("Unable to output to specified file.  Backing up to system log file.");
        ps = dbg.fpf.getPrintStream();
      }
      // the table's contents
      sw.Start();
      db.backupTable(table, ps, perRowSleepMs, wrote); // the rows counter gives us a live display
      sw.Stop();
      String logMessage = ((wrote.getCount() < 0) ? "Did NOT back" : " Backed") + " up " + wrote.getCount() + " rows of table " + table + " in " + Safe.millisToSecsPlus(duration()) + ".";
      dbg.ERROR(logMessage);
      if(wrote.getCount() < 0) {
        errors.add(logMessage);
      } else {
        succeeded = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      try {
        if(ps != null) {
          ps.flush();
          ps.close();
        }
        if(gzip != null) {
          gzip.flush();
          gzip.close();
        }
        if(fos != null) {
          fos.flush();
          fos.close();
        }
      } catch (Exception e) {
        dbg.Caught(e);
      }
    }
  }

  private BackupJob(DBMacros db, String backupPath, String table, long number, int threadPriority, long perRowSleepMs, Date reqTime, String reqWho) {
    this.db = db;
    this.backupPath = backupPath;
    this.table = table;
    this.reqTime = (reqTime == null) ? Safe.Now() : reqTime;
    this.jobNumber = number;
    this.threadPriority = threadPriority;
    this.perRowSleepMs = perRowSleepMs;
    this.reqWho = Safe.TrivialDefault(reqWho, Thread.currentThread().getName());
    this.mutex = new Monitor(name());
    if(db != null) {
      width = db.profileTable(table).width();
    }
  }
}
