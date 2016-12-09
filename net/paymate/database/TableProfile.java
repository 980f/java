/**
 * Title:        TableProfile
 * Description:  Contains information for a table, as used by database profiling, etc.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: TableProfile.java,v 1.13 2001/11/16 01:34:30 mattm Exp $
 */

package net.paymate.database;
import  net.paymate.util.*;
import  java.util.Arrays;

public class TableProfile {
  private static final ErrorLogStream dbg = new ErrorLogStream(TableProfile.class.getName(), ErrorLogStream.WARNING);

  private TableInfo ti = null;
  private ColumnProfile columns[] = null;

  protected TableProfile(TableInfo ti, ColumnProfile [] columns) {
    this.ti = ti;
    this.columns = columns;
  }

  public static final TableProfile create(TableInfo ti, ColumnProfile [] columns) {
    TableProfile tp = new TableProfile(ti, columns);
    return tp;
  }

  public TableProfile join(ColumnProfile [] joiners) {
    ColumnProfile [] merger= new ColumnProfile [columns.length+joiners.length] ;
    System.arraycopy(columns,0,merger,0,columns.length);
    System.arraycopy(joiners,0,merger,columns.length,joiners.length);
    columns=merger;
    return this;
  }

  public String name() {
    return ti.name();
  }

  public String fieldname(String fname) {
//+++ verify name is of one of our columns...
    return ti.name()+'.'+fname;
  }

  public String allFields() {
    return fieldname("*");
  }

  public String fieldname(ColumnProfile colm) {
//+++ verify column is of one of our columns...
    return ti.name()+'.'+colm.name();
  }


  public TextList fieldNames() {
    TextList tl = new TextList();
    for(int i = 0; i < columns.length; i++) {
      tl.Add(columns[i].name());
    }
    return tl;
  }

  public ColumnProfile [] columns() {
    ColumnProfile [] cols = new ColumnProfile [columns.length];
    System.arraycopy(columns, 0, cols, 0, columns.length);
    return cols;
  }

  public int numColumns() {
    return columns.length;
  }

  public int width() {
    int ret = -1;
    if(columns != null) {
      ret = 0;
      for(int i = columns.length; i-->0;) {
        ret += columns()[i].size();
      }
    }
    return ret;
  }

  public ColumnProfile column(String fieldName) {
    ColumnProfile ret = null;
    if(fieldName != null) {
      for(int i = 0; i < columns.length; i++) {
        if(fieldName.equalsIgnoreCase(columns[i].name())) {
          ret = columns[i];
          break;
        }
      }
    } else {
      dbg.ERROR("fieldName = NULL !!!");
    }
    return ret;
  }

  public ColumnProfile column(int i) {
    // +++ bitch on errors
    return ((i >=0) && (i < columns.length)) ? columns[i] : null;
  }
  /**
   * SQL-92 says that database field names are NOT case-sensitive ...
   * American National Standard X3.135-1992, pg 69,
   * 5.2 Lexical elements / <token> and <separator>, Syntax Rules #10:
   * The <identifier body> of a <regular identifier> is equivalent to an <identifier body> in which
   * every letter that is a lower-case letter is replaced by the equivalent upper-case letter or letters.
   * This treatment includes determination of equivalence, representation in the Information and
   * Definition Schemas, representation in the diagnostics area, and similar uses.
   */
  public boolean fieldExists(String fieldName) {
    boolean ret = false;
    if(fieldName != null) {
      for(int i = 0; i < columns.length; i++) {
        if(fieldName.equalsIgnoreCase(columns[i].name())) {
          ret = true;
          break;
        }
      }
    } else {
      dbg.ERROR(fieldName + " = NULL !!!");
    }
    dbg.VERBOSE(name() + "." + fieldName + " does " + (ret ? "" : "NOT ") + "exist.");
    return ret;
  }

  public void sort() {
    Arrays.sort(columns);
  }

}
//$Id: TableProfile.java,v 1.13 2001/11/16 01:34:30 mattm Exp $