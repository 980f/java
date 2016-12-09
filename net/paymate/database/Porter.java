/**
 * Title:        Porter
 * Description:  Acts a transport for moving data between databases
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: Porter.java,v 1.28 2001/11/16 01:34:29 mattm Exp $
 */

package net.paymate.database;
import  net.paymate.util.Safe;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.util.TextList;
import  net.paymate.net.URLEncoderFilterOutputStream;
import  net.paymate.net.URLDecoderFilterInputStream;
import  java.sql.*;
import  java.io.*;
import  java.util.*;

// +++ Build database reconstruction into this class?  Do separately?
// +++ USE THE NEW PROFILE CLASSES !!!
// Usage: The easiest method is to call Profile.transfer(port1address1, port2address2);

public class Porter extends Vector {
  private static final ErrorLogStream dbg = new ErrorLogStream(Porter.class.getName());
  static {
    dbg.bare=true;
  }
////////////////
// static functions (using DBMacros's)

  public static final void transfer(DBMacros fromCon, DBMacros toCon, boolean simulate) {
    Porter porter = export(fromCon);
    imPort(toCon, porter, simulate);
  }

  // these functions port between DBMacross and files
  public static final void exportToFile(DBMacros fromCon, String toFilename) {
    exportToFile(export(fromCon), toFilename);
  }
  public static final void importFromFile(DBMacros toCon, String fromFilename, boolean simulate) {
    imPort(toCon, importFromFile(fromFilename), simulate);
  }

  // these functions port between DBMacross, export files, and raw table text files
  public static final void exportToFiles(DBMacros fromCon, String toFilenameBase) {
    // this exports the data from the DBMacros to separate CSV files
    Porter porter = export(fromCon);
    exportToFiles(porter, toFilenameBase);
  }
  public static final void importToFiles(String fromFilename, String toFilenameBase) {
    // this exports the data from the file fromFilename to separate CSV files
    Porter porter = importFromFile(fromFilename);
    exportToFiles(porter, toFilenameBase);
  }
  public static final void exportToFiles(Porter porter, String toFilename) {
    // this exports all tables separately to toFilename_tablename files
    // in comma-delimited format.
    for(int tableIndex = porter.size(); tableIndex-->0;) {
      TableMover tm = porter.itemAt(tableIndex);
      String filename = toFilename + "_" + tm.tableName + ".csv";
      FileOutputStream fos = genFOS(filename);
      // write it out
      if(fos != null) {
        dbg.VERBOSE( "Writing table '" + tm.tableName + "' to file '" + filename + "' ... ");
        tm.csvToStream(fos);
        try {
          fos.flush();
          fos.close();
        } catch (Exception e) {
          dbg.ERROR("Porter.exportToFile(): Closing file '" + filename + "' failed! Excepted closing the FileOutputStream.");
          dbg.Caught(e);
        }
      }
    }
  }

  // these functions port between files and TableMovers (all data is stored in RAM!)
  public static final void exportToFile(Porter porter, String toFilename) {
    FileOutputStream fos = genFOS(toFilename);
    // write it out
    if(fos != null) {
      dbg.VERBOSE( "Writing to file ...");
      porter.toStream(fos);
      dbg.VERBOSE( "File written.");
      try {
        fos.close();
      } catch (Exception e) {
        dbg.ERROR("failed! Porter.exportToFile(): Excepted closing the FileOutputStream.");
        dbg.Caught(e);
      }
    }
  }
  public static final Porter importFromFile(String fromFilename) {
    Porter movers = new Porter(100, 10); // guessing at decent presizing
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fromFilename);
    } catch (FileNotFoundException fnfe) {
      dbg.ERROR("Porter.importFromFile(): Opening '" + fromFilename + "' for import failed!  FileNotFoundException (" + fromFilename + ")");
      dbg.Caught(fnfe);
      return movers;
    }
    // read it in
    if(fis != null) {
      movers.fromStream(fis);
      try {
        fis.close();
      } catch (Exception e) {
        // who cares
        dbg.ERROR("Porter.importFromFile(): Closing file '" + fromFilename + "' failed!  Excepted closing the FileInputStream.");
        dbg.Caught(e);
      }
    }
    return movers;
  }

  // these functions port between DBMacross and TableMovers (all data is stored in RAM!)
  public static final Porter export(DBMacros fromCon) {
    TableInfoList tables = fromCon.getTableList();
    Porter movers = new Porter(100, 10); // guessing at decent presizing
    for(int i = tables.size(); i-->0;) {
      movers.add(exportTable(fromCon, tables.itemAt(i).name()));
    }
    return movers;
  }
  public static final void imPort(DBMacros toCon, Porter fromMover, boolean simulate) {
    TableInfoList tables = toCon.getTableList();
    for(int i = tables.size(); i-->0;) {
      String tableName = tables.itemAt(i).name();
      TableMover mover = fromMover.itemByName(tableName); // get the mover for this table
      if(mover == null) {
        dbg.WARNING( "Porter.imPort(): could not find mover for table '" + tableName + "'");
      } else {
        importTable(toCon, mover, simulate);
      }
    }
  }

  // these functions port between a single table and a TableMover
  public static final TableMover exportTable(DBMacros db, String tableName) {
    TableMover mover = new TableMover(tableName);
    // do the query so we can get the fields and the data
    ResultSet rs = selectAll(db, tableName);
    int colCount = 0;
    if(rs != null) {
      // get a list of all of fields in the table and insert into header
      dbg.VERBOSE( "Exporting table: " + tableName + " ... ");
      ResultSetMetaData rsmd = null;
      try {
        rsmd = rs.getMetaData();
      } catch (Exception e) {
        dbg.ERROR("Porter.exportTable(): Getting result set metadata for table " + tableName + " failed (excepted)!");
        dbg.Caught(e);
      }
      if(rsmd != null) {
        try {
          colCount = rsmd.getColumnCount();
        } catch (Exception e) {
          dbg.ERROR("Porter.exportTable(): Getting column count from the result set metadata for table " + tableName + " failed (excepted)!");
          dbg.Caught(e);
        }
        mover.header = new String[colCount];
        for(int i = colCount; i-->0;) {
          mover.header[i] = "EXCEPTION";
          try {
            mover.header[i] = rsmd.getColumnLabel(i+1);
          } catch(Exception e) {
            dbg.ERROR("Porter.exportTable(): Getting column header " + tableName + "[" + i + "] failed!  Excepted attempting to get column label.");
            dbg.Caught(e);
          }
        }
      }
      // insert all of the data into the data array
      Vector rows = new Vector(100, 10);
      try {
        while(DBMacros.next(rs)) {
          String contents [] = new String[colCount];
          for(int i = colCount; i-->0;) {
            contents[i] = "";
            try {
              contents[i] = rs.getString(i+1);
            } catch (Exception e) {
              dbg.ERROR("Porter.exportTable(): excepted attempting to get String value for column " + i);
              dbg.Caught(e);
            }
          }
          rows.add(contents);
        }
      } catch (Exception e2) {
        dbg.ERROR("Porter.exportTable(): excepted attempting to scroll through the result set");
        dbg.Caught(e2);
      } finally {
        db.closeRS(rs);
      }
      // mover.data = (String[][])rows.toArray(); // excepts!  ClassCastException!  STUPID!
      //mover.data = (String[][])((Object[])rows.toArray()); // excepts!  ClassCastException!  STUPID!
      // so ...
      mover.data = new String[rows.size()][colCount];
      for(int i = rows.size(); i-->0;) {
        mover.data[i] = (String[])rows.elementAt(i);
      }
    }
    return mover;
  }
  public static final void importTable(DBMacros db, TableMover mover, boolean simulate) {
    // +++ how to get data out of the TableMover and match up field names and add data to the table?
    ResultSet rs = selectAll(db, mover.tableName); // +++ probably not a good idea.  Need to find a way to select all and get nothing every time!
    mover.map = new boolean[mover.header.length];
    // fill with falses first
    for(int i = mover.map.length; i-->0;) {
      mover.map[i] = false;
    }
    int colCount = 0;
    int mapCount = 0;
    if(rs != null) {
      dbg.VERBOSE("Importing table " + mover.tableName + " ...");
      // get a list of all of fields in the INTO table
      ResultSetMetaData rsmd = null;
      try {
        rsmd = rs.getMetaData();
      } catch (Exception e) {
        dbg.ERROR("Porter.importTable(): Getting the result set metadata for table " + mover.tableName + " failed!  excepted attempting to get ResultSetMetaData from ResultSet");
        dbg.Caught(e);
      }
      if(rsmd != null) {
        try {
          colCount = rsmd.getColumnCount();
        } catch (Exception e) {
          dbg.ERROR("Porter.importTable(): Getting the column count from the result set metadata for table " + mover.tableName + " failed!  excepted attempting to get column count from the ResultSetMetaData");
          dbg.Caught(e);
        }
        // set all types to 0
        mover.type = new String[mover.header.length]; //new int[mover.header.length];
        for(int i = mover.header.length; i-->0;) {
          mover.type[i] = "";//0;
        }
        // match up the fields using the map
        for(int i = colCount; i-->0;) {
          String fieldName = "";
          try {
            fieldName = rsmd.getColumnLabel(i+1);
          } catch(Exception e) {
            dbg.ERROR("Porter.importTable(): Getting column header " + mover.tableName + "[" + i + "] failed!  excepted attempting to get column label for col # " + i);
            dbg.Caught(e);
          }
          String type = "";//int type = 0;
          try {
            type = rsmd.getColumnTypeName(i+1); // +++ change to text and use getColumnTypeName, then appropriately handle the type ???
            // +++ use db.getColumnAttr(ResultSetMetaData rsmd, int what, int rsCol)
          } catch(Exception e) {
            dbg.ERROR("Porter.importTable(): Getting column type " + mover.tableName + "[" + i + "] failed!  excepted attempting to get column type for col # " + i);
            dbg.Caught(e);
          }
          int col = mover.fieldIndexFromName(fieldName);
          if(col != -1) {
            mover.map[col] = true;
            mover.type[col] = type;
            mapCount++;
          }
        }
      }
      db.closeStmt(db.getStatement(rs));
    }
    // insert all of the data into the table
    // first, build the fieldList for the query
    TextList fieldList = new TextList(mapCount);
    for(int i = mover.map.length; i-->0;) {
      if(mover.map[i]) {
        fieldList.add(mover.header[i]);
      }
    }

    // for every row
    TextList values = new TextList(fieldList.size());
    String mappedFields [] = new String [mapCount];
    for(int row = mover.data.length; row-->0;) {
      // build the values list
      for(int col = mover.map.length; col-->0;) {
        // +++ we really ought to find out more about the field, and properly format it based on the type
        if(mover.map[col]) {
          String theValue = "";
          DBTypesFiltered typer = new DBTypesFiltered(mover.type[col]);
          switch(typer.Value()) {
            // numeric considerations
            case DBTypesFiltered.DECIMAL:
            case DBTypesFiltered.DEC:
            case DBTypesFiltered.INT:
            case DBTypesFiltered.INTEGER:
            case DBTypesFiltered.SMALLINT:
            case DBTypesFiltered.BIGINT:
            case DBTypesFiltered.DOUBLE:
            case DBTypesFiltered.FLOAT:
            case DBTypesFiltered.TINYINT:
            case DBTypesFiltered.REAL:
            case DBTypesFiltered.NUMERIC: {
              theValue = Safe.TrivialDefault(mover.data[row][col], "0");
            } break;
            // datetime considerations
            case DBTypesFiltered.DATETIME: {
              // +++ maybe generate the format of the string earlier and stuff into the mover.type for this field, then just use it here?
              theValue = QueryString.Quoted(typer.generateDate(Safe.TrivialDefault(mover.data[row][col], ""), mover.type[col])).toString();
            } break;
            // date or time considerations
            case DBTypesFiltered.TIME:
            case DBTypesFiltered.TIMESTAMP:
            case DBTypesFiltered.DATE: {
              String tmp = Safe.TrivialDefault(mover.data[row][col], "");
              int idx = tmp.indexOf('.');
              theValue = QueryString.Quoted(((idx > -1) ? tmp.substring(0, idx) : tmp)).toString(); // ??? +++ ---
            } break;
            // everything else
            case DBTypesFiltered.CHAR:
            case DBTypesFiltered.VARCHAR:
            case DBTypesFiltered.BYTE:
            case DBTypesFiltered.SERIAL:
            case DBTypesFiltered.TEXT:
            case DBTypesFiltered.ARRAY:
            case DBTypesFiltered.BINARY:
            case DBTypesFiltered.BIT:
            case DBTypesFiltered.BLOB:
            case DBTypesFiltered.CLOB:
            case DBTypesFiltered.DISTINCT:
            case DBTypesFiltered.JAVA:
            case DBTypesFiltered.LONGVARBINARY:
            case DBTypesFiltered.LONGVARCHAR:
            case DBTypesFiltered.NULL:
            case DBTypesFiltered.OTHER:
            case DBTypesFiltered.REF:
            case DBTypesFiltered.STRUCT:
            case DBTypesFiltered.VARBINARY:
            default: {
              theValue = QueryString.Quoted(mover.data[row][col]).toString();
            } break;
          }
          values.add(theValue);
        }
      }
      // and insert it
      QueryString query = QueryString.Insert(mover.tableName, fieldList, values);
      if(simulate) {
        dbg.WARNING( "Would have inserted: \n" + query);
      } else {
        dbg.VERBOSE( "Inserting: \n" + query);
        try {
          db.update(query, true);
        } catch(SQLException sqle) {
          // this means we couldn't insert.
          // If we couldn't insert cause of a Duplicate, say so
          String xnstr = Safe.TrivialDefault(sqle.toString(), "");
          if(((xnstr.indexOf("Unique constraint (") > -1) && (xnstr.indexOf(") violated.") > -1)) ||
              (xnstr.indexOf("Could not insert new row - duplicate value in a UNIQUE INDEX column.") > -1) ||
              (xnstr.indexOf("ISAM error: duplicate value for a record with unique key") > -1)) {
            //Unique constraint (mainsail.pk_ach_codes) violated.
            dbg.WARNING( "Porter.importTable(): could not insert row " + row + " into table " + mover.tableName + " (would be a duplicate record under the unique index).");
          } else { // otherwise just bitch
            dbg.ERROR("Porter.importTable(): Unexpected exception attempting to insert row " + row + " into table " + mover.tableName);
            dbg.Caught(sqle);
          }
        } catch(Exception e) {
          dbg.ERROR("Porter.importTable(): Unexpected exception attempting to insert row " + row + " into table " + mover.tableName + ";" + e);
          dbg.Caught(e);
        }
      }
    }
//    dbg.VERBOSE( "Done with table " + mover.tableName + ".");
  }
  public static final ResultSet selectAll(DBMacros con, String tableName) {
    // +++ put these guts in db.
    ResultSet rs = null;
    Statement stmt = con.query(QueryString.SelectAllFrom(tableName));
    rs = con.getResultSet(stmt);
    if(rs == null) {
      con.closeStmt(stmt);
    }
    return rs;
  }


////////////////
// public functions
  public TableMover itemAt(int i) {
    return (TableMover) elementAt(i);
  }

  public TableMover itemByName(String tableName) {
    for(int i = size(); i-->0;) {
      TableMover mover = itemAt(i);
      if(mover.tableName.equalsIgnoreCase(tableName)) {
        return mover;
      }
    }
    return null;
  }

  public void toStream(OutputStream out) {
    URLEncoderFilterOutputStream uefos = new URLEncoderFilterOutputStream(out);
    // write the TableMovers as Properties lists
    Properties props = new Properties();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    for(int i = size(); i-->0;) {
      baos.reset();
      dbg.VERBOSE( "Writing the " + itemAt(i).tableName + " table to the buffer ...");
      itemAt(i).toStream(baos);
      dbg.VERBOSE( "Done.");
      props.setProperty("" + i, baos.toString());
    }
    dbg.VERBOSE( "Writing out all tables to the output stream ...");
    try {
      props.store(out, "");
    } catch (IOException ioe) {
      dbg.ERROR("Porter.toStream(): IOException");
      dbg.Caught(ioe);
    }
    dbg.VERBOSE( "Done.");
  }

  public void fromStream(InputStream in) {
    URLDecoderFilterInputStream udfis = new URLDecoderFilterInputStream(in);
    // read the TableMovers as Properties lists
    Properties props = new Properties();
    dbg.VERBOSE( "Reading all all tables from the input stream ...");
    try {
      props.load(in);
    } catch (IOException ioe) {
      dbg.ERROR("Porter.fromStream(): IOException attempting to load properties");
      dbg.Caught(ioe);
    }
    dbg.VERBOSE( "Done.");
    Enumeration enum = props.propertyNames();
    while(enum.hasMoreElements()) {
      String indexName = (String) enum.nextElement();
      dbg.VERBOSE( "Reading the " + indexName + " table from the buffer ...");
      int index = Integer.parseInt(indexName);
      String item = props.getProperty(indexName);
      if(item != null) {
        ByteArrayInputStream bais = new ByteArrayInputStream(item.getBytes());
        if(index > (size() - 1)) {
          setSize(index+1);
        }
        TableMover mover = new TableMover();
        setElementAt(mover, index);
        mover.fromStream(bais);
      }
      dbg.VERBOSE( "Done.");
    }
  }

  private static final FileOutputStream genFOS(String filename) {
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(filename);
    } catch (FileNotFoundException fnfe) {
      dbg.ERROR("Porter.exportToFile(): Opening '" + filename + "' for writing failed!  FileNotFoundException (" + filename + ")");
      dbg.Caught(fnfe);
    }
    return fos;
  }

/////////////////
// CONSTRUCTORS
  public Porter(int presize, int incr) {
    super(presize, incr);
  }

  public static final void main(String [] args) {
    // +++ get these from the commandline
    boolean simulate = false;
//    String from = /*gandalf*/ "jdbc:informix-sqli://127.0.0.1:9999/mainsail:INFORMIXSERVER=mainsail";// null;
//    String to   = /*helios */ "jdbc:informix-sqli://127.0.0.1:8889/mainsail:INFORMIXSERVER=mainsail";// null;
    DBConnInfo from = new DBConnInfo("jdbc:informix-sqli://127.0.0.1:8080/mainsail:INFORMIXSERVER=mainsail", "mainsail", "1strauss");//was tunnelling through ssh
    DBConnInfo to = null;
    PayMateDB fromDB = ((from != null) ? new PayMateDB(from) : null);
    PayMateDB toDB = ((to != null) ? new PayMateDB(to) : null);
    // do it
    if(fromDB != null) {
      fromDB.getCon();
      if(toDB != null) {
        toDB.getCon();
        Porter.transfer(fromDB, toDB, simulate);
        //toDB.close();
      } else {
        Porter.exportToFiles(fromDB, "c:\\temp\\alltables.txt");
        // +++ tell where they exported to
      }
      //fromDB.close();
    } else {
      dbg.ERROR("You must at least specify what database to take data from!");
    }
  }

}

class TableMover {

  private static final ErrorLogStream dbg = new ErrorLogStream(TableMover.class.getName());

////////////////
// DATA
  String          tableName = null; // table name
  String     [][] data      = null; // [record][field]
  String header []          = null; // [field]
  /*int*/ String type []               = null; // [field] // only used when transferring, not when writing out and reading in
// for dest: Leave in for debugging
  boolean      [] map       = null; // map of fields to include in the import

  private static final String TABLENAMEKEY = "tableName";
  private static final String DATAKEY = "data";
  private static final String HEADERKEY = "header";


////////////////
// CONSTRUCTORS

  public TableMover(String tableName, int numFields) {
    this.tableName = tableName;
    setFieldCount(numFields);
  }
  public TableMover(String tableName) {
    this(tableName, 0);
  }
  public TableMover() {
    this(null, 0);
  }

////////////////
// DATA MANIPULATORS
  public void setFieldCount(int numFields) {
    this.data   = new String [0][numFields];
    this.header = new String[numFields];
    this.map    = new boolean[numFields]; // ???
    this.type   = new String[numFields];//int[numFields];
  }

  public int fieldIndexFromName(String name) {
    int ret = -1;
    if(header != null) {
      for(int i = header.length; i-->0;) {
        if(header[i].equalsIgnoreCase(name)) {
          ret = i;
          break;
        }
      }
    }
    return ret;
  }

////////////////
// STORAGE FUNCTIONS

  private String dataToString() {
    // +++ move this stuff into EasyCursor and go ahead and use it here!
    Properties props = new Properties();
    props.setProperty("dataRows", "" + data.length);
    props.setProperty("dataCols", "" + ((data.length > 0) ? data[0].length : 0));
    for(int row = data.length; row-->0;) {
      for(int col = data[0].length; col-->0;) {
        String value = data[row][col];
        if(value != null) {
          props.setProperty("data " + row + "," + col, value);
        }
      }
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    URLEncoderFilterOutputStream uefos = new URLEncoderFilterOutputStream(baos);
    try {
      props.store(uefos, "");
    } catch (Exception e) {
      dbg.ERROR("TableMover.dataToString(): Excepted attempting to store the properties.");
      dbg.Caught(e);
    }
    return baos.toString();
  }

  private String headerToString() {
    // +++ move this stuff into EasyCursor and go ahead and use it here!
    Properties props = new Properties();
    props.setProperty("headerRows", "" + header.length);
    for(int row = header.length; row-->0;) {
      props.setProperty("header " + row, header[row]);
    }
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    URLEncoderFilterOutputStream uefos = new URLEncoderFilterOutputStream(baos);
    try {
      props.store(uefos, "");
    } catch (Exception e) {
      dbg.ERROR("TableMover.headerToString(): Excepted attempting to store the properties.");
      dbg.Caught(e);
    }
    return baos.toString();
  }

  private void stringTodata(String dataStr) {
    // +++ move this stuff into EasyCursor and go ahead and use it here!
    Properties props = new Properties();
    ByteArrayInputStream bais = new ByteArrayInputStream(dataStr.getBytes());
    URLDecoderFilterInputStream udfis = new URLDecoderFilterInputStream(bais);
    try {
      props.load(udfis);
    } catch (Exception e) {
      dbg.ERROR("TableMover.stringTodata(): Excepted attempting to load the properties.");
      dbg.Caught(e);
    }
    String temp = props.getProperty("dataRows", "");
    int rows = Integer.parseInt(temp);
    temp = props.getProperty("dataCols", "");
    int cols = Integer.parseInt(temp);
    data = new String[rows][cols];
    for(int row = rows; row-->0;) {
      for(int col = cols; col-->0;) {
        data[row][col] = props.getProperty("data " + row + "," + col, "");
      }
    }
  }

  private void stringToHeader(String headerStr) {
    // +++ move this stuff into EasyCursor and go ahead and use it here!
    Properties props = new Properties();
    ByteArrayInputStream bais = new ByteArrayInputStream(headerStr.getBytes());
    URLDecoderFilterInputStream udfis = new URLDecoderFilterInputStream(bais);
    try {
      props.load(udfis);
    } catch (Exception e) {
      dbg.ERROR("TableMover.stringToHeader(): Excepted attempting to load the properties.");
      dbg.Caught(e);
    }
    int rows = Integer.parseInt(props.getProperty("headerRows"));
    header = new String[rows];
    type   = new String[rows];//int[rows];
    for(int row = rows; row-->0;) {
      header[row] = props.getProperty("header " + row);
      type[row] = "";//0;
    }
  }

  public void toStream(OutputStream out) {
    Properties props = new Properties();
    props.setProperty(TABLENAMEKEY, tableName);
    props.setProperty(DATAKEY, dataToString());
    props.setProperty(HEADERKEY, headerToString());
    try {
      props.store(out, "");
    } catch (Exception e) {
      dbg.ERROR("TableMover.toStream(): Excepted attempting to store the properties.");
      dbg.Caught(e);
    }
  }

  public void fromStream(InputStream in) {
    Properties props = new Properties();
    try {
      props.load(in);
    } catch (Exception e) {
      dbg.ERROR("TableMover.fromStream(): Excepted attempting to load the properties.");
      dbg.Caught(e);
    }
    tableName = props.getProperty(TABLENAMEKEY);
    stringTodata(props.getProperty(DATAKEY));
    stringToHeader(props.getProperty(HEADERKEY));
  }

  public void csvToStream(OutputStream out)   {
    PrintWriter pw = new PrintWriter(out, true);
    csvRowToStream(pw, header);
    for(int row = 0; row < data.length; row++) {
      csvRowToStream(pw, data[row]);
    }
    pw.flush();
    pw.close();
  }

  public static final void csvRowToStream(PrintWriter pw, String someData[]) {
    boolean first = true;
    for(int i = 0; i < someData.length; i++) {
      pw.print(((first) ? "": ",") + "\"" + someData[i] + "\"");
      first = false;
    }
    pw.println("");
  }

}

