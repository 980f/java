/**
 * Title:        DBMacros<p>
 * Description:  macros for performing database queries and updates<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: DBMacros.java,v 1.82 2001/11/17 06:16:58 mattm Exp $
 */

package net.paymate.database;
import  java.sql.*;
import  net.paymate.util.*;
import  net.paymate.util.timer.*;
import  java.util.*;
import  java.io.*;
//kill retro support import  com.informix.jdbc.*; // IfmxStatement
import  net.paymate.data.*; // UniqueId
import  java.lang.reflect.Field; // for reflection of java.sql.Types class

// +++ put more thread security in here?

public class DBMacros extends GenericDB {
  private static final ErrorLogStream dbg = new ErrorLogStream(DBMacros.class.getName(), ErrorLogStream.WARNING);

  public static final int FAILED=-1; //where #of rows is expected

  public DBMacros(DBConnInfo connInfo) {
    super(connInfo);
  }

  public static final ResultSetMetaData getRSMD(ResultSet rs) {
    ResultSetMetaData rsmd = null;
    if(rs != null) {
      try {
        rsmd = rs.getMetaData();
      } catch (SQLException e) {
        dbg.ERROR("Exception occurred getting ResultSetMetaData!");
        dbg.Caught(e);
      }
    }
    return rsmd;
  }

  public String [] getStringsFromQuery(QueryString queryStr) {
    Statement stmt = query(queryStr);
    TextList strings = new TextList();
    if(stmt != null) {
      ResultSet rs = null;
      rs = getResultSet(stmt);
      if(rs != null) {
        int fieldCount = 0;
        ResultSetMetaData rsmd = getRSMD(rs);
        try {
          fieldCount = rsmd.getColumnCount();
        } catch (Exception t) {
          dbg.Caught(t); // +++ more details?
        }
        if(!next(rs)) {
          dbg.WARNING("No records in ResultSet!");
        } else {
          for(int i = 1; i <= fieldCount; i++) {
            strings.add(getStringFromRS(i, rs));
          }
        }
      } else {
        dbg.ERROR("getStringsFromQuery()->query() call did not succeed (null resultSet)");
      }
      closeStmt(stmt);
    } else {
      dbg.ERROR("getStringsFromQuery()->query() call did not succeed (null statement)");
    }
    String [] str1 = strings.toStringArray();
    return str1;
  }

  public String getStringFromQuery(QueryString queryStr) {
    return getStringFromQuery(queryStr, 0);
  }

  public String getStringFromQuery(QueryString queryStr, int field) {
    String result = null;
    String [] results = getStringsFromQuery(queryStr);
    if((results != null) && (field < results.length)) {
      result = results[field].trim();
    }
    return result;
  }

  public String getCatStringsFromQuery(QueryString queryStr) {
    String result = "";
    String [] results = getStringsFromQuery(queryStr);
    if(results != null) {
      for(int i = 0; i < results.length; i++) {
        String adder = results[i].trim();
        result += adder;
      }
    }
    return result.trim();
  }

  public int getIntFromQuery(QueryString queryStr) {
    return getIntFromQuery(queryStr, 0);
  }

  public int getIntFromQuery(QueryString queryStr, int field) {
    String tmp = getStringFromQuery(queryStr, field);
    if(Safe.NonTrivial(tmp)) {
      return Integer.valueOf(tmp).intValue();
    }
    return Safe.INVALIDINTEGER;
  }

  public double getDoubleFromQuery(QueryString queryStr, int field) {
    String tmp = getStringFromQuery(queryStr, field);
    if(Safe.NonTrivial(tmp)) {
      return Double.valueOf(tmp).doubleValue();
    }
    return -1.0;
  }

  public double getDoubleFromRS(int column, ResultSet myrs) {
    return Double.parseDouble(Safe.TrivialDefault(getStringFromRS(column, myrs), "0.0"));
  }

  public double getDoubleFromRS(String column, ResultSet myrs) {
    return Double.parseDouble(Safe.TrivialDefault(getStringFromRS(column, myrs), "0.0"));
  }

  private static Monitor timerStrMon = new Monitor("DBMacros.timerStr");
  private static Fstring timerStr = new Fstring(7, ' ');
  private static void logQuery(int qt, long millis, QueryString query, int retval, boolean regularLog) {
    String toLog = "exception attempting to log the query";
    try {
      timerStrMon.getMonitor(); // +++ create an fstring that mutexes itself !!!
      DBFunctionType qtype = new DBFunctionType(qt);
      toLog = qtype.Image() + timerStr.righted(String.valueOf(millis)) + " ms = " + query + (qtype.is(DBFunctionType.UPDATE) ? " returned " + retval : "");
      if(regularLog) {
        dbg.WARNING(toLog); // eventually remove ???
      }
      log(toLog);
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      timerStrMon.freeMonitor();
    }
  }

  public Statement query(QueryString queryStr, boolean throwException) throws Exception {
    Statement stmt = null;
    Exception mye = null;
    StopWatch swatch = new StopWatch();
    try {
      dbg.Enter("query");
      Connection mycon = getCon();
      if(mycon != null) {
// +++ cast to IfmxStatement for our entire system so that we can use the facilities provided therein (like autofree and getSerial()).
// +++ deal with other possible parameters to this statement ...
        stmt = mycon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY /* +++ do we really want to say read only ? */);

        // +++ optimize this
        try {
          stmt.setFetchSize(30);
        } catch (Exception e) {
          dbg.Caught(e);
        }
        // +++ optimize this

        String qs = queryStr.toString();
        if(stmt.executeQuery(qs) == null){
          dbg.ERROR("queryStmt() generates a null statement!");
        }
      } else {
        dbg.ERROR("Cannot call queryStmt() unless connection exists!");
      }
    } catch (Exception t) {
      if(throwException) {
        mye = t;
      } else {
        dbg.Caught("Exception performing query: " + queryStr, t);
        String toLog = "Query: " + swatch.millis() + " ms = " + queryStr;
        dbg.Caught(toLog, t);
        log(toLog + "; " + t);
      }
    } finally {
      queryStats.add(swatch.millis());
      logQuery(DBFunctionType.QUERY, swatch.millis(), queryStr, -1, true);
      dbg.Exit();
      if(mye != null) {
        throw mye;
      }
      return stmt;
    }
  }

  public Statement query(QueryString queryStr) {
    Statement stmt = null;
    try {
      stmt = query(queryStr, false);
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return stmt;
    }
  }

  public int update(QueryString queryStr, UniqueId id, boolean throwException) throws Exception {
    int retval = FAILED; // the error code
    Exception tothrow = null;
    StopWatch swatch = new StopWatch();
    try {
      dbg.Enter("update");
      Statement stmt = null;
      Connection mycon = null;
      // do the query
      try {
        dbg.Enter("update" + (throwException ? "(RAW)":""));
        mycon = getCon();
        if(mycon != null) {
          stmt   = mycon.createStatement();
          String qs = queryStr.toString();
          if(stmt!=null){
            retval = stmt.executeUpdate(qs); // this is where sql exceptions will most likely occur
          }
        } else {
          dbg.ERROR("Cannot call update() unless connection exists!");
        }
      } catch (Exception e) {
        if(throwException) {
          tothrow = e;
        } else {
          dbg.Caught("Exception performing update: " + queryStr, e);
          if(mycon == null) {
            dbg.WARNING("mycon == null");
          }
          if(stmt == null) {
            dbg.WARNING("stmt == null");
          } else {
            try {
              SQLWarning warn = stmt.getWarnings();
              while(warn != null) {
                dbg.WARNING("warn: " + warn);
                warn = warn.getNextWarning();
              }
            } catch (Exception trash) {
              // who cares
            }
          }
          if(queryStr == null) {
            dbg.WARNING("queryStr == null");
          }
        }
      } finally {
        try {
          if(!mycon.getAutoCommit()) {
            mycon.commit();
          }
        } catch (Exception e) {
          dbg.Caught(e);
        } finally {
//kill informix
//          if((stmt != null) && (id != null) && stmt instanceof IfmxStatement) {
//            IfmxStatement ifmxstmt = (IfmxStatement)stmt;
//            try {
//              id.setValue(ifmxstmt.getSerial()); // assumes it isn't supposed to be getSerial8(), so we need to deal with this +++
//            } catch (Exception eser) {
//              // swallow for now
//              dbg.Caught(eser);
//            }
//          }
          closeStmt(stmt);
        }
        dbg.Exit();
      }
    } finally {
      try {
        updateStats.add(swatch.millis());
        logQuery(DBFunctionType.UPDATE, swatch.millis(), queryStr, retval, true);
      } catch (Exception e2) {
        dbg.Caught(e2);
      } finally {
        dbg.Exit();
        if(tothrow != null) {
          dbg.WARNING("Throwing " + tothrow);
          throw tothrow;
        } else {
          return retval;
        }
      }
    }
  }
  public int update(QueryString queryStr, UniqueId id) throws Exception {
    return update(queryStr, id, false);
  }
  public int update(QueryString queryStr, boolean throwException) throws Exception {
    return update(queryStr, null, throwException);
  }
  public int update(QueryString queryStr) {
    int retval = FAILED; // changed from 0 recently.  should have?
    try {
      retval = update(queryStr, null, false);
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      return retval;
    }
  }

  public static Accumulator queryStats = new Accumulator();
  public static Accumulator updateStats = new Accumulator();
  public static Accumulator nextStats = new Accumulator();

  private static final int NOT_A_COLUMN = -1;

  public static final String getStringFromRS(String fieldName, ResultSet myrs) {
    return getStringFromRS(NOT_A_COLUMN, fieldName, myrs);
  }

  public static LogFile logFile = null;
  private static PrintFork pf=null;
  private static final String LOGFILENAME = "dbquery";
  public static final boolean init(PrintStream backup) {
    logFile = new LogFile(LOGFILENAME, false, backup);
    dbg.ERROR("Log file = " + logFile.getName());
    pf = logFile.getPrintFork();
    pf.println("inited OK");
    return true;
  }

  private static final void log(String toLog) {
    try {
      pf.println(toLog);
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }
  protected static final void logRaw(String toLog) {
    try {
      logFile.getPrintStream().println(toLog);
    } catch (Exception e) {
      dbg.Caught(e);
    }
  }

  public static final String getStringFromRS(int column, ResultSet myrs) {
    return getStringFromRS(column, null, myrs);
  }

  private static final String getStringFromRS(int column, String fieldName, ResultSet myrs) {
    String ret = null;
    try {
      if(column < 1) {
        column = myrs.findColumn(fieldName);
      }
      ret = myrs.getString(column);
    } catch (Exception t) {
      dbg.ERROR("getStringFromRS: Exception getting column [" + column + "/" + fieldName + "] (not included in query?): ");
      dbg.Caught(t);
    }
    // --------------- NEXT LINE COULD BE CAUSING PROBLEMS,
    // but try to fix the problems elsewhere so that we can preserve empty strings here.
    // return Safe.OnTrivial(Safe.TrivialDefault(ret, "").trim(), " "); // Why are we doing this ?!?!?!?  I think because leaving it out makes empty (raised) boxes in the html tables, but we should fix those in the html tables, and not here.
    return Safe.TrivialDefault(ret, "").trim();
  }

  public static final int getIntFromRS(int column, ResultSet myrs) {
    return Safe.parseInt("0"+getStringFromRS(column, myrs));
  }

  public static final int getIntFromRS(String column, ResultSet myrs) {
    return Safe.parseInt(getStringFromRS(column, myrs));
  }

  public static final boolean getBooleanFromRS(int column, ResultSet myrs) {
    return isTrue(getStringFromRS(column, myrs));
  }

  public static final boolean getBooleanFromRS(String column, ResultSet myrs) {
    return isTrue(getStringFromRS(column, myrs));
  }

  // either 'T' or 'Y' are true.
  public static final boolean isTrue(String chr) {
    boolean ret = Safe.NonTrivial(chr) && ("TYty".indexOf(chr) > -1); // trivial defaults to false
    return ret;
  }

  public static final int getColumnCount(ResultSetMetaData rsmd) {
    int ret = 0;
    try {
      ret = rsmd.getColumnCount();
    } catch (Exception e) {
      dbg.ERROR("getColumnCount() excepted attempting to get column count from the ResultSetMetaData");
    }
    return ret;
  }

  public int getColumnCount(ResultSet rs) {
    return getColumnCount(getRSMD(rs));
  }

  public static final int NAME    = 0;
  public static final int TYPE    = 1;
  public static final int SIZE    = 2;
  public static final int NULLABLE= 3;

  public static final String ISNOTNULLABLE = "NOT NULL";
  public static final String ISNULLABLE = "";

  public static final String getColumnAttr(ResultSetMetaData rsmd, int what, int rsCol) {
    String ret = null;
    try {
      switch(what) {
        case NAME: {
          ret = rsmd.getColumnLabel(rsCol);
        } break;
        case TYPE: {
          ret = rsmd.getColumnTypeName(rsCol);
        } break;
        case SIZE: {
          ret = "" + rsmd.getColumnDisplaySize(rsCol);
        } break;
        case NULLABLE: {
          ret = ((rsmd.isNullable(rsCol) == ResultSetMetaData.columnNullable) ? ISNULLABLE: ISNOTNULLABLE);
        } break;
      }
    } catch (Exception e) {
      dbg.ERROR("getColumnAttr() excepted attempting to get column info from the ResultSetMetaData");
      dbg.Caught(e);
    }
    return ret;
  }

  /**
   * Get the tables for the connected database
   */
  private ResultSet getTables() {
    ResultSet rs = null;
    try {
      String types [] = {
        "TABLE",
        "VIEW",
//        "SYSTEM TABLE",
//        "GLOBAL TEMPORARY",
//        "LOCAL TEMPORARY",
//        "ALIAS",
//        "SYNONYM",
      };
      rs = getDatabaseMetadata().getTables(null, null, null, types);
    } catch (Exception e) {
      dbg.Caught(e);
    }
    return rs;
  }

  public TableInfoList getTableList() {
    // get the list of all tables in the new database
    TableInfoList tables = new TableInfoList();
    ResultSet rs = getTables();
    if(rs != null) {
      boolean cont = true;
      while(cont) {
        if(cont = next(rs)) {
          String name = getTableInfo(rs, "TABLE_NAME").toUpperCase();
          if(name != null) {
            tables.add(new TableInfo(getTableInfo(rs, "TABLE_CAT"),
                                     getTableInfo(rs, "TABLE_SCHEM"),
                                     name,
                                     getTableInfo(rs, "TABLE_TYPE"),
                                     getTableInfo(rs, "REMARKS")));
          }
        }
      }
      closeStmt(getStatement(rs));
    }
    return tables;
  }

  private String getTableInfo(ResultSet rs, String info) {
    String ret = "";
    try {
      dbg.Enter("getTableInfo");
      ret = rs.getString(info);
    } catch (Exception e) {
      dbg.ERROR("Excepted trying to get field " + info + " from result set.");
    } finally {
      dbg.Exit();
    }
    return ret;
  }

  // +++ move the outputting stuff to a different file or generate it in txt so that this file doesn't have html stuff in it!

  /**
  * Backs up a single table
  *
  * +++ extract the CSV stuff out of here and make a generic CSV report outputter
  *
  * @param tablename - the name of the table to backup
\  */
  public boolean backupTable(String tablename, PrintStream ps, long perRowSleepMs, Accumulator rows) {
    boolean okay = false;
    Statement st = null;
    try {
      // vars
      StringBuffer row = new StringBuffer(1000);
      // get the data
      QueryString qs = QueryString.SelectAllFrom(tablename);
      st = query(qs);
      try {
        ResultSet rs = getResultSet(st);
        ResultSetMetaData rsmd = rs.getMetaData();
        int colcount = rsmd.getColumnCount();
        // output the headers here
        for(int col = 0; col < colcount; col++) {
          // prepend a comma on all but first
          if(col != 0) {
            row.append(",");
          }
          try {
            row.append(rsmd.getColumnName(col+1));
          } catch (Exception e) {
            row.append("!EXCEPTION!");
          }
        }
        ps.println(row.toString());
        // now, do the detail
        while(next(rs)) {
          row.setLength(0); // clear it
          for(int col = 0; col < colcount; col++) {
            // prepend a comma on all but first
            row.append((col != 0) ? ",\"" : "\"");
            try {
              row.append(getStringFromRS(col+1, rs));
            } catch (Exception e) {
              row.append("!EXCEPTION!");
            }
            row.append("\"");
          }
          String rowstr = row.toString();
          rows.add(rowstr.length());
          ps.println(rowstr);
          ThreadX.sleepFor(perRowSleepMs); // < 1 acts as a yield
        }
        okay = true;
      } catch (Exception e2) {
        dbg.Caught(e2);
      }
    } catch (Exception e){
      dbg.Caught(e);
    } finally {
      closeStmt(st);
      return okay;
    }
  }

  public static final boolean next(ResultSet rs) {
    boolean ret = false;
    StopWatch swatch = new StopWatch();
    try {
      if(rs != null) {
        ret = rs.next();
      }
    } catch (Exception e) {
      dbg.ERROR("next() excepted attempting to get the next row in the ResultSet ... ");
      dbg.Caught(e);
    } finally {
      long dur = swatch.millis();
      nextStats.add(dur);
      if(dur > 0) {
        logQuery(DBFunctionType.NEXT, dur, QueryString.Clause(""), -1, false);
      }
      return ret;
    }
  }

  public static final ResultSet getResultSet(Statement stmt) {
    ResultSet ret = null;
    try {
      if(stmt != null) {
        ret = stmt.getResultSet();
      }
    } catch (Exception e) {
      dbg.ERROR("getRS excepted attempting to get the ResultSet from the executed statement");
      dbg.Caught(e);
    }
    return ret;
  }

  ////////////////////////////////////
  // database profiling

  public static final String ALLTABLES = null;

  /**
   * creates TableProfile.java class files
   * and a single databaseProfile class
   * for a database connection
   */
  public void profileToClasses(String directory, String databaseName) {
    // get the connection's profile
    DatabaseProfile dbprofile = profileDatabase(databaseName, ALLTABLES, false);
    // now, write those out as classes
    String packageName = databaseName.toLowerCase();
    TextList tableNames = new TextList();
    for(int iTable = dbprofile.size(); iTable-->0;) {
      TableProfile tp = dbprofile.itemAt(iTable);
      // proper the tablename to use a classname
      String tableName = Safe.proper(tp.name());
      String className =  tableName + "Table";
      tableNames.Add(tableName);
      String filename = directory + File.separator + className + ".java"; // +++ need a Safe.catPath function
      FileOutputStream fos = Safe.fileOutputStream(filename);
      if(fos == null) {
        dbg.ERROR("Unable to create file: " + filename);
        continue;
      }
      PrintWriter pw = new PrintWriter(fos);
      pw.println("//DO NOT EDIT!!! THIS IS A MACHINE GENERATED FILE!!");
      pw.println();
      pw.println("package net.paymate.database." + packageName + ";");
      pw.println();
      pw.println("/" + "**");
      pw.println(" * Title:        " + className);
      pw.println(" * Description:  The " + tableName + " table static representation");
      pw.println(" * Copyright:    Copyright (c) 2000-2001");
      pw.println(" * Company:      PayMate.net");
      pw.println(" * @author PayMate.net");
      pw.println(" * @version $" + "Id$");
      pw.println(" *" + "/");
      pw.println();
      pw.println("import net.paymate.database.TableProfile;");
      pw.println("import net.paymate.database.ColumnProfile;");
      pw.println();
      pw.println("public class " + className + " extends TableProfile implements DBConstants {");
      pw.println();
      TextList fieldNames = tp.fieldNames();
      fieldNames.sort(); // sort alphabetically
      TextList stmts = new TextList(fieldNames.size());
      for(int iField = 0; iField < fieldNames.size(); iField++) {
        ColumnProfile cp = tp.column(iField);
        pw.println("  public static final ColumnProfile " + cp.name() + " = ColumnProfile.create(\"" + cp.name() + "\"  , \"" + cp.type() + "\", " + cp.size() + ", " + cp.nullable() + ", " + cp.displayName() + "\"," + cp.autoIncrement() + ");");
      }
      pw.println();
      pw.println("  private static final ColumnProfile [] staticColumns = {");
      pw.println(fieldNames.asParagraph("    ", ",\r\n"));
      pw.println("  };");
      pw.println();
      pw.println("  public " + className + "() {");
      pw.println("    super(\"" + tableName.toUpperCase() + "\", staticColumns);");
      pw.println("  }");
      pw.println();
      pw.println("}");
      try {
       // flush & close the file
        pw.flush();
        pw.close();
      } catch (Exception e) {
        // +++ bitch
      }
    }
    databaseName = Safe.proper(databaseName);
    String className = databaseName + "Database";
    String filename = directory + File.separator + className + ".java"; // +++ need a Safe.catPath function
    FileOutputStream fos = Safe.fileOutputStream(filename);
    if(fos == null) {
      dbg.ERROR("Unable to create file: " + filename);
    } else {
      PrintWriter pw = new PrintWriter(fos);
      pw.println("//DO NOT EDIT!!! THIS IS A MACHINE GENERATED FILE!!");
      pw.println("package net.paymate.database." + packageName + ";");
      pw.println();
      pw.println("/" + "**");
      pw.println(" * Title:        " + className);
      pw.println(" * Description:  The " + databaseName + " database static representation");
      pw.println(" * Copyright:    Copyright (c) 2000-2001");
      pw.println(" * Company:      PayMate.net");
      pw.println(" * @author PayMate.net");
      pw.println(" * @version $" + "Id$");
      pw.println(" *" + "/");
      pw.println();
      pw.println("import net.paymate.database.DatabaseProfile;");
      pw.println();
      pw.println("public class " + className + " extends DatabaseProfile {");
      pw.println();
      // table entries here
      tableNames.sort();
      TextList objectList = new TextList();
      for(int i = 0; i < tableNames.size(); i++) {
        String tableName = tableNames.itemAt(i);
        String objectName = tableName.toLowerCase();
        String tableClassName = tableName + "Table";
        objectList.Add(objectName);
        pw.println("  public static final " + tableClassName + " " + objectName + " = new " + tableClassName + "();");
      }
      // constructor
      pw.println();
      pw.println("  public " + className + "() {" );
      pw.println("    super(\"" + databaseName + "\");");
      // add table objects here in constructor
      pw.println(objectList.asParagraph("    this.add(", ");\r\n"));
      pw.println("  }");
      pw.println("}");
      try {
       // flush & close the file
        pw.flush();
        pw.close();
      } catch (Exception e) {
        // +++ bitch
      }
    }
  }

  public DatabaseProfile profileDatabase(String databaseName, String tablename, boolean sort) {
    DatabaseProfile tables  = new DatabaseProfile(databaseName);
    if(tablename != ALLTABLES) {
      TableProfile tp = profileTable(tablename);
      if(sort) {
        tp.sort();
      }
      tables.add(tp);
    } else {
      TableInfoList tablelist = getTableList();
      for(int i = 0; i < tablelist.size(); i++) {
        TableProfile tp = profileTable(tablelist.itemAt(i).name());
        if(tp.numColumns() > 0) {
          if(sort) {
            tp.sort();
          }
          tables.add(tp);
        } else {
          // +++ bitch
        }
      }
    }
    return tables;
  }

  public String getColumnDataType(String tablename, String fieldname) {
    String ret = null;
    TableProfile dbp = profileTable(tablename);
    if(dbp != null) {
      ColumnProfile cp = dbp.column(fieldname);
      if(cp != null) {
        ret = cp.dataType;
      } else {
        // +++ bitch
      }
    } else {
      // +++ bitch
    }
    return javaSqlType(ret);
  }

  private static final EasyCursor ezp = new EasyCursor();
  private static boolean ezpset = false;
  private static final Monitor javaSqlTypesMonitor = new Monitor(DBMacros.class.getName()+".javaSqlTypes");
  public static final String javaSqlType(String typeNum) {
    String ret = "";
    int type = Safe.parseInt(typeNum);
    // lock
    try {
      javaSqlTypesMonitor.getMonitor();
      // only have to do this once per run
      if(!ezpset) {
        // use reflection to find every 'public final static int' in the class java.sql.Types, and add the item to ezp
        try {
          Class c = java.sql.Types.class;
          Field[] fields = c.getFields();
          for(int i = fields.length; i-->0;) {
            Field field = fields[i];
            int ivalue = field.getInt(c); // probably doesn't work
            String value = String.valueOf(ivalue);
            ezp.setString(value, field.getName());
          }
        } catch (Exception e) {
          /* abandon all hope ye who enter here */
        }
        ezpset = true;
      }
    } finally {
      javaSqlTypesMonitor.freeMonitor();
    }
    ret = ezp.getString(typeNum, "");
    return ret;
  }



  public TableProfile profileTable(String tableName) {
    ResultSet cpmd = null;
    Vector cols = new Vector(100);
    try {
      dbg.VERBOSE("profiling table '"+ tableName +"'");
      cpmd = getDatabaseMetadata().getColumns(null, null, tableName.toUpperCase(), null);
      if(cpmd != null) {
        while(next(cpmd)) {
          ColumnProfile cp = null;
          try {
            String tablename = Safe.TrivialDefault(getStringFromRS("TABLE_NAME", cpmd), "");
            String columnname = Safe.TrivialDefault(getStringFromRS("COLUMN_NAME", cpmd), "");
            String type = Safe.TrivialDefault(getStringFromRS("TYPE_NAME", cpmd), "");
            String size = Safe.TrivialDefault(getStringFromRS("COLUMN_SIZE", cpmd), "");
            String nullable = Safe.TrivialDefault(getStringFromRS("IS_NULLABLE", cpmd), "");
            cp = ColumnProfile.create(tablename, columnname, type, size, nullable);
            cp.tableCat        = Safe.TrivialDefault(getStringFromRS("TABLE_CAT", cpmd), "");
            cp.tableSchem      = Safe.TrivialDefault(getStringFromRS("TABLE_SCHEM", cpmd), "");
            cp.dataType        = Safe.TrivialDefault(getStringFromRS("DATA_TYPE", cpmd), "");
            cp.decimalDigits   = Safe.TrivialDefault(getStringFromRS("DECIMAL_DIGITS", cpmd), "");
            cp.numPrecRadix    = Safe.TrivialDefault(getStringFromRS("NUM_PREC_RADIX", cpmd), "");
            cp.nullAble        = Safe.TrivialDefault(getStringFromRS("NULLABLE", cpmd), "");
            cp.remarks         = Safe.TrivialDefault(getStringFromRS("REMARKS", cpmd), "");
            cp.columnDef       = Safe.TrivialDefault(getStringFromRS("COLUMN_DEF", cpmd), "");
            cp.charOctetLength = Safe.TrivialDefault(getStringFromRS("CHAR_OCTET_LENGTH", cpmd), "");
            cp.ordinalPosition = Safe.TrivialDefault(getStringFromRS("ORDINAL_POSITION", cpmd), "");
          } catch (Exception e2) {
            dbg.Caught(e2);
          }
          if(cp != null) {
            cols.add(cp);
            dbg.VERBOSE("Adding column " + cp.name());
          }
        }
        try {
          cpmd.close(); // don't do this anywhere else, please
        } catch (Exception e) {
          dbg.Caught(e);
        }
      } else {
        dbg.ERROR("cpmd is NULL!");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    }
    ColumnProfile columns [] = new ColumnProfile[cols.size()];
    for(int i = 0; i < cols.size(); i++) {
      columns[i] = (ColumnProfile)cols.elementAt(i);
    }
    return TableProfile.create(new TableInfo(tableName), columns) ;
  }

  public boolean tableExists(String tableName) {
    boolean ret = false;
    if(Safe.NonTrivial(tableName)) {
      TableInfoList tablelist = getTableList();
      for(int i = tablelist.size(); i-->0; ) {
        if(tablelist.itemAt(i).name().equalsIgnoreCase(tableName)) {
          ret = true;
          break;
        }
      }
    }
    return ret;
  }

  public boolean fieldExists(String tableName, String fieldName) {
    TableProfile tp = profileTable(tableName);
    // skip through and look for the fieldname
    return tp.fieldExists(fieldName);
  }

  public boolean primaryKeyExists(String tableName, String constraintName) {
    boolean ret = false;
    // +++ put this into the table profiler
    if(constraintName != null) {
      if(tableName != null) {
        ResultSet rs = null;
        try {
          rs = getDatabaseMetadata().getPrimaryKeys(null, null, null/*tableName.toUpperCase()  testing !!! @@@*/);
        } catch (Exception e) {
          dbg.Caught(e);
        }
        if(rs != null) {
          try {
            while(next(rs)) {
              String cname = getStringFromRS("PK_NAME", rs);
              if(constraintName.equalsIgnoreCase(cname)) {
                ret = true;
                dbg.ERROR("constraintName="+constraintName+", cname="+cname+", "+(ret ? "" : "NOT ")+"equal!");
                break;
              }
              dbg.ERROR("constraintName="+constraintName+", cname="+cname+", "+(ret ? "" : "NOT ")+"equal!");
            }
          } catch (Exception e) {
            dbg.Caught(e);
          } finally {
            closeRS(rs);
          }
        } else {
          dbg.ERROR("ResultSet = NULL !!!");
        }
        dbg.VERBOSE(tableName + " primary key constraint " + constraintName + " does " + (ret ? "" : "NOT ") + "exist.");
      } else {
        dbg.ERROR("tableName = NULL !!!");
      }
    } else {
      dbg.ERROR("constraintName = NULL !!!");
    }
    return ret;
  }

  public boolean indexExists(String indexName) {
    boolean ret = false;
    // +++ put this into the table profiler
    if(indexName != null) {
      indexName = indexName.trim();
      ResultSet rs = null;
      try {
        rs = getDatabaseMetadata().getIndexInfo(null, null, null, false, true);
      } catch (Exception e) {
        dbg.Caught(e);
      }
      if(rs != null) {
        try {
          while(next(rs)) {
            String cname = getStringFromRS("INDEX_NAME", rs).trim();
            if(indexName.equalsIgnoreCase(cname)) {
              ret = true;
              dbg.ERROR("indexName="+indexName+", cname="+cname+", "+(ret ? "" : "NOT ")+"equal!");
              break;
            }
            dbg.ERROR("indexName="+indexName+", cname="+cname+", "+(ret ? "" : "NOT ")+"equal!");
          }
        } catch (Exception e) {
          dbg.Caught(e);
        } finally {
          closeRS(rs);
        }
      } else {
        dbg.ERROR("ResultSet = NULL !!!");
      }
      dbg.VERBOSE("Index " + indexName + " does " + (ret ? "" : "NOT ") + "exist.");
    } else {
      dbg.ERROR("IndexName = NULL !!!");
    }
    return ret;
  }

  // VALIDATOR STUFF
  /**
   * Returns true if the field was properly dropped
   */
  protected final boolean dropField(String tablename, String fieldname) {
    boolean ret = false;
    try {
      dbg.Enter("dropField");
      if(fieldExists(tablename, fieldname)) {
        if(!getDatabaseMetadata().supportsAlterTableWithDropColumn()) {
          dbg.ERROR("dropField " + tablename + "." + fieldname + ": was not able to run since the DBMS does not support it!");
          ret = true;
        } else {
          dbg.ERROR("dropField " + tablename + "." + fieldname + ": returned " + update(QueryString.Clause("ALTER TABLE " + tablename + " DROP " + fieldname)));
          ret = !fieldExists(tablename, fieldname);
        }
      } else {
        dbg.ERROR("dropField " + tablename + "." + fieldname + ": already dropped.");
        ret = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return ret;
    }
  }

  // VALIDATOR STUFF
  /**
   * Returns true if the index was properly dropped
   */
  protected final boolean dropIndex(String indexname) {
    boolean ret = false;
    int i = -1;
    try {
      dbg.Enter("dropIndex");
      if(indexExists(indexname)) {
        i = update(QueryString.Clause("DROP INDEX " + indexname));
      } else {
        i = 0;
        dbg.ERROR("dropIndex " + indexname + ": index doesn't exist; can't drop.");
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.ERROR("dropIndex " + indexname + ": returned " + i);
      ret = (i == 0);
      dbg.Exit();
      return ret;
    }
  }

  /**
   * Returns true if the table was properly dropped
   */
  protected final boolean dropTable(String tablename) {
    boolean ret = false;
    try {
      dbg.Enter("dropTable");
      if(tableExists(tablename)) {
        dbg.ERROR("dropTable" + tablename + " returned " + update(QueryString.Clause("DROP TABLE " + tablename)));
        ret = !tableExists(tablename);
      } else {
        ret = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return ret;
    }
  }

  // +++ use dbmd's getMaxColumnNameLength to see if the name is too long
  protected final boolean addField(String tablename, String fieldname, String fieldSpex) {
    boolean ret = false;
    try {
      dbg.Enter("addField");
      if(!fieldExists(tablename, fieldname)) {
        dbg.ERROR("addField" + tablename + " returned " + update(/* +++ use: db.generateColumnAdd(tp, cp) (or something similar) instead! */
          QueryString.Clause("ALTER TABLE " + tablename + " ADD " + fieldname + " " + fieldSpex)));
        ret = fieldExists(tablename, fieldname);
      } else {
        ret = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return ret;
    }
  }

  // +++ use dbmd's getMaxColumnNameLength to see if the name is too long
  protected final boolean addField(TableProfile table, ColumnProfile column) {
    boolean ret = false;
    try {
      dbg.Enter("addField");
      if(!fieldExists(table.name(), column.name())) {
        dbg.ERROR("addField" + table.name() + " returned " + update(/* +++ use: db.generateColumnAdd(tp, cp) (or something similar) instead! */
          QueryString.Clause("ALTER TABLE " + table.name() + " ADD ").createFieldClause(column, true)));
        ret = fieldExists(table.name(), column.name());
      } else {
        ret = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return ret;
    }
  }

  // +++ use dbmd's getMaxColumnNameLength to see if the name is too long
  // +++ use dbmd's getMaxTableNameLength to see if the name is too long
  protected final boolean createTable(TableProfile tp) {
    boolean ret = false;
    try {
      dbg.Enter("createTable");
      if(!tableExists(tp.name())) {
        dbg.ERROR("createTable " + tp.name() + " returned " + update(QueryString.generateTableCreate(tp)));
        ret = tableExists(tp.name());
      } else {
        ret = true;
      }
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      dbg.Exit();
      return ret;
    }
  }

  public static final void closeStmt(Statement stmt) {
    if(stmt != null) {
      try {
        stmt.close();
      } catch (Exception t) {
        dbg.WARNING("Exception closing statement.");
      }
    }
  }

  /**
   * ONLY use with resultsets that have no statement (like are returned by DatabaseMetadata functions)
   */
  public static final void closeRS(ResultSet rs) {
    if(rs != null) {
      try {
        rs.close();
      } catch (Exception t) {
        dbg.WARNING("Exception closing result set.");
      }
    }
  }

  public static final Statement getStatement(ResultSet rs) {
    try {
      if(rs != null) {
        return rs.getStatement();
      }
    } catch (Exception t) {
      dbg.WARNING("Exception getting statement from resultset.");
    } finally {
      return null;
    }
  }

  public int tableRecordCount(String tableName) {
    int ret = getIntFromQuery(QueryString.Select("Count(*)").from(tableName), 0);
    dbg.WARNING("found " + ret + " records.");
    return ret;
  }
}

class NamedStatementList extends Vector {
  private ErrorLogStream dbg = null;
  public NamedStatementList(ErrorLogStream dbg) {
    this.dbg = dbg;
  }
  public NamedStatement itemAt(int index) {
    NamedStatement retval = null;
    if((index < size()) && (index >= 0)) {
      retval = (NamedStatement) elementAt(index);
    }
    return retval;
  }
  public void Remove(Statement stmt) {
    boolean removed = false;
    for(int i = size(); i -->0;) {
      NamedStatement ns = itemAt(i);
      if((ns != null) && (ns.stmt == stmt)) {
        removed=remove(ns);
        break;
      }
    }
    if(!removed && (dbg != null)) {
      dbg.WARNING("Statement NOT found for removal: " + stmt);
    } else {
      dbg.VERBOSE("Statement FOUND for removal: " + stmt);
    }
  }
}

class NamedStatement {
  public Statement stmt = null;
  public String name = "uninited"; // name is really the full query
  public NamedStatement(Statement stmt, String name) {
    this.stmt = stmt;
    this.name = Thread.currentThread().getName()+";"+name;
  }
}

class DBFunctionType extends TrueEnum {
  public final static int UPDATE =0;
  public final static int QUERY  =1;
  public final static int NEXT   =2;

  public int numValues(){
    return 3;
  }
  public static final String [] myTexts = {
    "Update: ",
    "Query:  ",
    "Next(): ",
  };
  public static final TextList myText = new TextList(myTexts);
  protected final TextList getMyText() {
    return myText;
  }
  public static final DBFunctionType Prop=new DBFunctionType();
  public DBFunctionType(){
    super();
  }
  public DBFunctionType(int rawValue){
    super(rawValue);
  }
  public DBFunctionType(String textValue){
    super(textValue);
  }
  public DBFunctionType(DBTypes rhs){
    this(rhs.Value());
  }
}
