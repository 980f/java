package net.paymate.database;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/BackupAgent.java,v $
 * Description:  Backs up the database, in the background, if desired
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.4 $
 *
 * This class provides a single agent to handle all database backups (unloads),
 * even single tables for transport between systems.
 *
 * The backup can happen at a lower priority and with a sleep in between rows,
 * so as to facilitate slower processing, which will allow
 * the database engine to keep up.
 * Backups should not be run when the system is coming up or going down.
 * +++ So, we need to code a Stop() function to stop the agent.
 */

import net.paymate.util.*;
import java.util.*;

public class BackupAgent implements QActor {

  // a way to get the report of what ran and how well it did (separate list from the run queue) ...
  private Vector ran = new Vector();

  public Enumeration jobs() {
    return new BackupAgentRanEnumeration(ran.toArray(), dbg);
  }

  // nothing happens on construction
  public static final BackupAgent New(String threadname) {
    return new BackupAgent(threadname);
  }

  public void bringup() {
    this.realagent.Clear(); // +++ investigate if this is correct
  }

  public void shutdown() {
    this.realagent.Stop();
  }

  public boolean isUp() {
    return !realagent.Stopped();
  }

  public void Stop() {
    // stub.  Used by QAgent!  Don't put anything in here, and DEFINITELY don't call shutdown() from here, or you will get into an infinite loop!
  }

  public boolean Clear() {
    boolean ret = true;
    for(int i = ran.size(); i-->0;) {
      try {
        BackupJob bj = (BackupJob)ran.elementAt(i);
        if(bj.done && bj.succeeded) {
          ran.remove(bj);
        }
      } catch (ArrayIndexOutOfBoundsException aiobe) {
        // no biggie, just threw on elementAt(), most likely
        i = ran.size();
      } catch (Exception e) {
        dbg.Caught(e);
        ret = false;
        break;
      }
    }
    return ret;
  }

  public int pendingCount() {
    return realagent.Size();
  }

  /**
   * @param backupPath - path to backup directory, cannot be null
   * @param tables - list of tables to backup, cannot be null
   * @param threadPriority - what priority to run the backup thread at
   * @param perRowSleepMs - number of milliseconds to sleep between processing rows
   *
   * NOTE: Interrupted exceptions from new backup jobs being inserted into the list
   * should have a negligible effect on the backup sleeps.  It's all good.
   *
   * @returns BackupJob - null if the job could not be created.
   */
  public final BackupJob backup(DBMacros db, String backupPath, String table, int threadPriority, long perRowSleepMs, Date reqTime) {
    BackupJob job = BackupJob.New(db, backupPath, table, backupJobCounter.incr(), threadPriority, perRowSleepMs, reqTime, null);
    if(job != null) {
      realagent.Post(job);
      ran.add(0, job); // so we can keep up with it later.
      ttlJobCount.incr();
    } else {
      dbg.ERROR("Could not queue job.");
    }
    return job;
  }
  // backs up the whole database
  public final BackupJob [] backup(DBMacros db, String backupPath, int threadPriority, long perRowSleepMs) {
    // Add the database.capabilities to the backup? +++
    // Database structure, maybe?
    //      logHTML(ps, new org.apache.ecs.ElementContainer().addElement(net.paymate.web.page.DBPage.printCapabilities(this.getDatabaseMetadata(), net.paymate.web.color.ColorScheme.MONEY, null, null)));
    TableInfoList tables = db.getTableList();
    BackupJob [] jobs = new BackupJob[tables.size()];
    Date now = Safe.Now();
    for(int i = 0; i < tables.size(); i++) {
      jobs[i] = backup(db, backupPath, tables.itemAt(i).name(), threadPriority, perRowSleepMs, now);
    }
    return jobs;
  }

  private static final ErrorLogStream dbg = new ErrorLogStream(BackupAgent.class.getName());
  private QAgent realagent = null;
  private Counter backupJobCounter  = new Counter();

  private BackupAgent(String threadname) {
    realagent = QAgent.New(threadname, this);
    realagent.config(60000); // hmm ???
    realagent.config(dbg);
    realagent.Clear();
  }

  // don't make these public ? +++
  public Accumulator bytesRead = new Accumulator();
  public Accumulator times = new Accumulator();
  public Accumulator bytesWritten = new Accumulator();
  public Counter ttlJobCount = new Counter();

  /**
   * I wish this wasn't public; guess I could use an inner class or something.
   */
  private BackupJob buj = null;
  public void runone(Object fromq) {
    buj = null;
    try {
      if(fromq == null) {
        dbg.ERROR("fromq is null!");
        return;
      }
      if(!(fromq instanceof BackupJob)) {
        dbg.ERROR("fromq was not a BackupJob!");
      }
      buj = (BackupJob)fromq;
      buj.run();
    } catch (Exception e) {
      dbg.Caught(e);
      if(buj != null) {
        buj.complain("Exception running job: " + e, false);
      }
    } finally {
      if(buj != null) {
        buj.done = true;
        times.add(buj.duration());
        bytesRead.add(buj.wrote());
        bytesWritten.add(buj.filesize());
        buj = null;
      }
    }
  }

  public String toString() {
    BackupJob job = buj; // get the reference
    String inProgress = "";
    if(job != null) {
      inProgress = job.filename();
    }
    return inProgress;
  }

}

class BackupAgentRanEnumeration implements Enumeration {
  ErrorLogStream dbg = null;
  Object [] jobs = null;
  int index = 0;
  public BackupAgentRanEnumeration(Object [] jobs, ErrorLogStream dbg) {
    this.dbg = dbg;
    this.jobs = jobs;
    if((dbg != null) && (jobs == null)) {
      dbg.ERROR("jobs == null!");
    }
  }
  public Object nextElement() {
    Object o = null;
    if(hasMoreElements()) {
      o = jobs[index++];
    }
    return o;
  }
  public boolean hasMoreElements() {
    return (jobs != null) && (index < jobs.length);
  }
}
