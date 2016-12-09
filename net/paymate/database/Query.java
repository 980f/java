package net.paymate.database;

/**
 * Title:        $Source $ <BR>
 * Description:  Base class for stepping through a resultSet & filling a table object.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Query.java,v 1.12 2001/10/02 17:06:38 mattm Exp $
 */

import  java.sql.*;
import  java.lang.reflect.Field;
import  net.paymate.util.*;
import  net.paymate.database.DBMacros;

public abstract class Query {

  private static final ErrorLogStream dbg = new ErrorLogStream(Query.class.getName(), ErrorLogStream.WARNING);

  private ResultSet rs = null;
  private Statement stmt = null;

  public Query(Statement stmt) {
    this.stmt = stmt;
    if(stmt == null) {
      dbg.VERBOSE("Query(): stmt == NULL. (Not necessarily an error.)");
    }
    rs = DBMacros.getResultSet(stmt);
  }

  protected ResultSet rs() {
    return rs;
  }

  public boolean next() {
    boolean ret = false;
    if(DBMacros.next(rs)) {
      fromResultSet(rs);
      ret = true;
    }
    return ret;
  }
  public boolean hasMoreRows() {
    boolean ret = false;
    try {
      if(rs!=null) {
        if(rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
          ret = /*rs.isLast() ||*/ rs.isAfterLast();
        } else {
          ret = false; // need a MAYBE! (this shouldn't work, but does)
        }
      } else {
dbg.ERROR("hasMoreRows() rs is NULL!");
      }
    } catch (Exception t) {
      dbg.ERROR("hasMoreRows() Exception checking for more records!");
      dbg.Caught(t);
    }
    return !ret;
  }

  protected void fromResultSet(ResultSet rs) {
    if(rs != null) {
      Object record = this;
      // create the object from a record in the resultset
      java.lang.reflect.Field[] field = record.getClass().getFields();
      for(int i = field.length; i-->0;) {
        java.lang.reflect.Field f = field[i];
        String fName = f.getName();
        try {
          String value = DBMacros.getStringFromRS(fName, rs);
          f.set(record, value);
          dbg.VERBOSE("fromResultSet: attempted to set " + record.getClass().getName() + "." + fName + "=" + value);
        } catch(Exception t) {
          dbg.ERROR("fromResultSet: Error mapping value for field " + fName + ".");
        }
      }
    }
  }

  public EasyCursor toProperties() {
    // create a list of all of the fields and their values from the object
    Object record = this;
    java.lang.reflect.Field[] field = record.getClass().getFields();
    EasyCursor ezp = new EasyCursor();
    for(int i = field.length; i-->0;) {
      java.lang.reflect.Field f = field[i];
      String fName = f.getName();
      String fValue = "";
      try {
        fValue = (String) f.get(record);
        ezp.setString(fName, fValue);
      } catch(Exception t) {
        dbg.ERROR("toProperties: Error getting value for field " + fName + ".");
      }
    }
    return ezp;
  }

  /**
   * null is acceptable here
   */
  public void setAllTo(String value) {
    Object record = this;
    java.lang.reflect.Field[] field = record.getClass().getFields();
    for(int i = field.length; i-->0;) {
      java.lang.reflect.Field f = field[i];
      String fName = f.getName();
      String fValue = "";
      try {
        f.set(record, value);
      } catch(Exception t) {
        dbg.ERROR("setAllTo: Error setting value for field " + fName + ".");
      }
    }
  }

  public void close() {
    DBMacros.closeStmt(stmt);
  }

  public void finalize() {
    close();
  }
}
