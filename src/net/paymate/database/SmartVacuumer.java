package net.paymate.database;

/**
 * <p>Title: $Source: /home/andyh/localcvs/pmnet/cvs/src/net/paymate/database/SmartVacuumer.java,v $</p>
 * <p>Description: Intelligently cleans up the database</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.14 $
 */

import net.paymate.lang.ThreadX;
import net.paymate.util.Accumulator;
import net.paymate.util.DateX;
import net.paymate.util.EasyProperties;
import net.paymate.util.ErrorLogStream;
import net.paymate.util.Service;
import net.paymate.util.ServiceConfigurator;
import net.paymate.util.timer.StopWatch;

public class SmartVacuumer extends Service implements Runnable {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(SmartVacuumer.class);

  public static final String NAME = "SmartVacuumer";
  public SmartVacuumer(ServiceConfigurator cfg) {
    super(NAME, cfg, true);
  }

  private Thread thread = null;
  public boolean isUp() {
    return thread!=null && thread.isAlive();
  }
  public void up() {
    if(!isUp()) {
      shouldStop = false;
      thread = new Thread(this, NAME);
      thread.setDaemon(true);
      thread.start();
      // now it is running
    }
  }
  public void down() {
    if(isUp()) {
      shouldStop=true;
      thread.interrupt(); // in case it is asleep
      primed = false; // to be sure that we reload configs on up()
    }
  }

  protected void loadConfigs() {
    // +++ what do these mean and how should we set them?
    final int VACBASETHRESHOLD   = 1000;
    final int VACSCALINGFACTOR   = 2;
    final int SLEEPBASEVALUE     = 300;
    final int SLEEPSCALINGFACTOR = 2;
    final int MAXAGE = 1500000000;
    // ints
    vacuum_base_threshold  =configger.getIntServiceParam(serviceName(), "VACBASETHRESHOLD"  , VACBASETHRESHOLD);
    analyze_base_threshold =configger.getIntServiceParam(serviceName(), "ANLZBASETHRESHOLD" , VACBASETHRESHOLD / 2);
    sleep_base_value       =configger.getIntServiceParam(serviceName(), "SLEEPBASEVALUE"    , SLEEPBASEVALUE);
    max_age                =configger.getIntServiceParam(serviceName(), "MAXAGE"            , MAXAGE);
    // floats
    vacuum_scaling_factor  =configger.getIntServiceParam(serviceName(), "VACSCALINGFACTOR"  , VACSCALINGFACTOR);
    analyze_scaling_factor =configger.getIntServiceParam(serviceName(), "ANLZSCALINGFACTOR" , VACSCALINGFACTOR / 2);
    sleep_scaling_factor   =configger.getIntServiceParam(serviceName(), "SLEEPSCALINGFACTOR", SLEEPSCALINGFACTOR);
    println("loadedConfigs: " +
            "\nvacuum_base_threshold="+vacuum_base_threshold+
            "\nanalyze_base_threshold="+analyze_base_threshold+
            "\nsleep_base_value="+sleep_base_value+
            "\nmax_age="+max_age+
            "\nvacuum_scaling_factor="+vacuum_scaling_factor+
            "\nanalyze_scaling_factor="+analyze_scaling_factor+
            "\nsleep_scaling_factor="+sleep_scaling_factor
            );
  }

  private boolean shouldStop = false;
  public boolean shouldStop() {
    return shouldStop;
  }

  private boolean primed = false;
  public void prime() {
    try {
      PayMateDB db = PayMateDBDispenser.getPayMateDB();
      loadConfigs();
      TableProfile[] tables = tables(db);
      // initialize the data
      println("initializing statistics for all tables ...");
      for (int tbli2 = tables.length; tbli2-- > 0; ) {
        if (shouldStop) {
          break;
        }
        TableProfile tbl = tables[tbli2];
        EasyProperties stats = db.getTableStats(tbl);
        if (stats.allKeys().size() == 0) {
          logerror("Could not get table stats on table " + tbl.name());
          continue;
        } else {
          long ntupins = stats.getLong("n_tup_ins", 0);
          long ntupupd = stats.getLong("n_tup_upd", 0);
          long ntupdel = stats.getLong("n_tup_del", 0);
          PostgresVacuum pgv=tbl.pgv;
          pgv.CountAtLastAnalyze = ntupins + ntupupd;
          pgv.curr_analyze_count = pgv.CountAtLastAnalyze;
          pgv.CountAtLastVacuum = ntupdel + ntupupd;
          pgv.curr_vacuum_count = pgv.CountAtLastVacuum;
          pgv.reltuples = stats.getInt("reltuples", 0);
          pgv.relpages = stats.getInt("relpages", 0);
          pgv.analyze_threshold = (long) (analyze_base_threshold +
                                          analyze_scaling_factor *
                                          pgv.reltuples);
          pgv.vacuum_threshold = (long) (vacuum_base_threshold +
                                         vacuum_scaling_factor * pgv.reltuples);
          pf.WARNING("stats calculated for table " + tbl.name());
        }
      } // end of for loop that adds tables
      println("stats initialized.");
      primed = true;
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  public static final String AGENOTICE = "From the docs:\n"+
      "\"With the standard freezing policy, the age column will start at one billion for a "+
      "freshly-vacuumed database. When the age approaches two billion, the database must "+
      "be vacuumed again to avoid risk of wraparound failures. Recommended practice is "+
      "to vacuum each database at least once every half-a-billion (500 million) transactions, "+
      "so as to provide plenty of safety margin.\"\n"+
      "So we do a full database vacuum if age > 1.5billion.";

  // set in loadConfigs()
  int vacuum_base_threshold;
  int analyze_base_threshold;
  int sleep_base_value;
  int max_age;
  float vacuum_scaling_factor;
  float analyze_scaling_factor;
  float sleep_scaling_factor;

  // these are calculated for the whole time the system is up, not just while this service is up
  private Accumulator vacuums = new Accumulator();
  private Accumulator justanalyzes = new Accumulator();

  private boolean sleeping = false;
  private float sleep_secs = 0;

  public void run() {
    PayMateDB db = PayMateDBDispenser.getPayMateDB();
    TableProfile[] tables = tables(db);
    try {
      println("run starting ...");
      StopWatch sw = new StopWatch(false);
      if(!primed) {
        prime();
      }
      while (!shouldStop) { // Main Loop
        pf.VERBOSE("main loop starting...");
        sw.Start();//start does a reset as well, i.e. there is no pause/continue functionality on StopWatch
        boolean check_stats_enabled = db.getStatsRowLevelEnabled();
        if (!check_stats_enabled) {
          panicerror("Error: GUC variable stats_row_level must be enabled.");
          panicerror("       Please fix the problems and try again.");
        } else {
          int age = db.getDatabaseAge();
          if (age > max_age) {
            panicerror("DD age is " + age + ", > " + max_age + ".", AGENOTICE);
            // +++ !!! WARNING !!! This will cause the system to LOCK OUT TABLES !!!
            // db.vacuumDatabase(false, false, false); // check for return value?
          } else {
            pf.VERBOSE("Database age of " + age + " is still below the allowable limit of " + max_age + ".");
          }
          for (int tbli = tables.length; tbli-- > 0; ) {
            if (shouldStop) {
              break;
            }
            TableProfile tbl = tables[tbli];
            PostgresVacuum pgv=tbl.pgv;

            pf.VERBOSE("Checking statistics for table " + tbl.name() + " ...");

            EasyProperties stats = db.getTableStats(tbl);

            long ntupdel = stats.getLong("n_tup_del", 0);
            long ntupupd = stats.getLong("n_tup_upd", 0);
            pgv.curr_analyze_count = stats.getLong("n_tup_ins", 0) + ntupdel +
                ntupupd;
            pgv.curr_vacuum_count = ntupdel + ntupupd;

            /*
             * Check numDeletes to see if we need to vacuum, if so: Run vacuum analyze
             * (adding analyze is small so we might as well) Update table thresholds and
             * related information if numDeletes is not big enough for vacuum then check
             * numInserts for analyze
             */
            boolean run = false; // whether or not to do anything at all
            boolean analyzeOnly = false; // means no vacuum when true
            if((pgv.curr_vacuum_count - pgv.CountAtLastVacuum) >=
                pgv.vacuum_threshold) {
              run = true;
              analyzeOnly = false;
            } else if ( (pgv.curr_analyze_count - pgv.CountAtLastAnalyze) >=
                     pgv.analyze_threshold) {
              run = true;
              analyzeOnly = true;
            }
            pf.VERBOSE("Stats for " + tbl.name() + " =\n" + tbl.vacuumStats());
            if (run && !shouldStop) {
              String doing = analyzeOnly ? "Analyzing" : "Vacuuming";
              pf.WARNING(doing + " table " + tbl.name() + " ...");
              StopWatch opersw = new StopWatch();
              db.vacuumAnalyzeDatabase(true, true, false, !analyzeOnly); // +++ check the return value?
              if(!analyzeOnly) {
                vacuums.add(opersw.Stop());
              } else {
                justanalyzes.add(opersw.Stop());
              }
              /* Below set thresholds = base_value + scaling_factor * reltuples
                   Should be called after a vacuum since vacuum updates values in pg_class */
              pf.WARNING("post-" + doing + ", updating '"+tbl.name()+"' post-cleanup statistics ...");
              EasyProperties pages = db.getTablePages(tbl);
              if (pages.allKeys().size() > 0) {
                pgv.reltuples = pages.getInt("reltuples", 0);
                pgv.relpages  = pages.getInt("relpages" , 0);

                // update vacuum thresholds only if we just did a vacuum analyze
                if (!analyzeOnly) {
                  pgv.vacuum_threshold = (long) (vacuum_base_threshold +
                                                 vacuum_scaling_factor *
                                                 pgv.reltuples);
                  pgv.CountAtLastVacuum = pgv.curr_vacuum_count;
                }

                // update analyze thresholds
                pgv.analyze_threshold = (long) (analyze_base_threshold +
                                                analyze_scaling_factor *
                                                pgv.reltuples);
                pgv.CountAtLastAnalyze = pgv.curr_analyze_count;

                /*
                 * If the stats collector is reporting fewer updates then we
                 * have on record then the stats were probably reset, so we
                 * need to reset also
                 */

                if ( (pgv.curr_analyze_count < pgv.CountAtLastAnalyze) ||
                    (pgv.curr_vacuum_count < pgv.CountAtLastVacuum)) {
                  pgv.CountAtLastAnalyze = pgv.curr_analyze_count;
                  pgv.CountAtLastVacuum = pgv.curr_vacuum_count;
                }
              } else {
                logerror("Error getting pages for table " + tbl.name());
              }
            } else {
              pf.VERBOSE("Table " + tbl.name() + " does not require any work.");
            }
            pf.VERBOSE("Done checking statistics for table " + tbl.name() + ".");
          }
        }
        // Figure out how long to sleep etc ...
        times.add(sw.Stop());
        sleep_secs = sleep_base_value + sleep_scaling_factor * (sw.millis() / 1000);
        if(shouldStop) {
          break;
        } else {
          // +++ should do a wait/notify instead?
          pf.VERBOSE("Sleeping for " + sleep_secs + " seconds ...");
          sleeping = true;
          boolean completed = ThreadX.sleepFor(sleep_secs); // Larger Pause between outer loops
          sleeping = false;
          pf.VERBOSE("Done sleeping. " + (completed?"Completed full time.":"Interrupted. Stopping/Downing?"));
        }
      } // end of while loop
    } catch (Exception ex) {
      dbg.Caught(ex);
    } finally {
      if(!shouldStop) {
        panicerror("SmartVacuum service died unexpectedly!");
      }
      println("run ending ...");
    }
  }

  private void logerror(String msg) {
    dbg.ERROR(msg);
    println(msg);
  }

  private void panicerror(String msg) {
    dbg.ERROR(msg);
    println(msg);
    PANIC(msg);
  }

  private void panicerror(String msg, Object o) {
    dbg.ERROR(msg);
    println(msg);
    PANIC(msg, o);
  }

  private static TableProfile[] tables() {
    return tables(null);
  }
  private static TableProfile[] tables(PayMateDB db) {
    if(db == null) {
      db = PayMateDBDispenser.getPayMateDB();
    }
    return db.tables;
  }

  Accumulator times = new Accumulator();

  public String svcCnxns() {
    TableProfile[] tables = tables();
    return String.valueOf((tables != null) ? tables.length : 0);
  }
  public String svcTxns() {
    return String.valueOf(times.getCount());
  }
  public String svcPend() {
    return sleeping ? "1" : "0";
  }
  public String svcAvgTime() {
    return DateX.millisToSecsPlus(times.getAverage());
  }
  public String svcWrites() {
    return printStats(vacuums);
  }
  public String svcReads() {
    return printStats(justanalyzes);
  }
  public String svcNotes() {
    return sleeping ? ("Sleeping " + sleep_secs + " secs") : (isUp() ? "Running" : NOCOMMENT);
  }
}