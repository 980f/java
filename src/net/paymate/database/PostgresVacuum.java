package net.paymate.database;

import net.paymate.util.EasyProperties;

/**
 * Created by: andyh
 * Date: Mar 19, 2005   9:48:50 AM
 * (C) 2005 hal42
 */
public class PostgresVacuum {
  /////////////////////////////////
  // Begin vacuum statistics stuff
  // possibly create another class to hold this stuff
  // and contain it in this class, like TableInfo

  int reltuples, relpages;
  long analyze_threshold, vacuum_threshold;
  long CountAtLastAnalyze;// = inserts + updates as of last analyze or initial values at startup
  long CountAtLastVacuum;// = deletes + updates as of last vacuum or initial values at startup
  long curr_analyze_count, curr_vacuum_count;		// Latest values from stats system

  public EasyProperties vacuumStats() {
    EasyProperties ezp = new EasyProperties();
    ezp.setInt( "reltuples", reltuples );
    ezp.setInt( "relpages", relpages );
    ezp.setLong( "curr_analyze_count", curr_analyze_count );
    ezp.setLong( "curr_vacuum_count", curr_vacuum_count );
    ezp.setLong( "CountAtLastAnalyze", CountAtLastAnalyze );
    ezp.setLong( "CountAtLastVacuum", CountAtLastVacuum );
    ezp.setLong( "analyze_threshold", analyze_threshold );
    ezp.setLong( "vacuum_threshold", vacuum_threshold );
    return ezp;
  }

}
