package net.paymate.database;
/**
 * Title:        ColumnProfile
 * Description:  Contains the Column information used by database profiling, etc.
 *                These objects are fixed upon creation.  You can't change one once it is built.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ColumnProfile.java,v 1.28 2003/08/20 18:22:29 mattm Exp $
 */

import net.paymate.util.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public class ColumnProfile implements Comparable {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(
      ColumnProfile.class, ErrorLogStream.WARNING);

  // database metadata stuff:
  public String tableCat = "";
  public String tableSchem = "";
  private TableProfile table = null;
  private String columnName = "";
//  public String dataType        = "";
//  public String typeName       = "";
  private DBTypesFiltered type; //ugly name, when wrapping an enum give the enum an ugly name!
  private String columnSize = "";
  private int size = -1; // +++ enforce restrictions?
  public String decimalDigits = "";
  public String numPrecRadix = "";
  public String nullAble = "";
  public String remarks = "";
  public String columnDef = "";
  public String charOctetLength = "";
  public String ordinalPosition = "";
  private String isNullable = "";
  private boolean nullable = false;
  private String displayName = ""; // for what goes at the top of a column when displaying via webpages, etc
  private boolean autoIncrement = false;

  //////////////////////////
  private ColumnProfile() {
    //use create
  }

  /**
   * returns correctenss of strucutre, not whether it is a member of the enclosing database.
   */
  public boolean isViable() {
    return StringX.NonTrivial(columnName); //&&type in typelist&&nullable is valid ...
  }

  public String name() {
    return columnName;
  }

  public String type() {
    return type.Image();
  }

  public DBTypesFiltered numericType() {
    return type;
  }

  public boolean nullable() {
    return nullable;
  }

  public int size() {
    return size;
  }

  public String displayName() {
    return displayName;
  }

  public boolean autoIncrement() {
    return autoIncrement;
  }

  public TableProfile table() {
    return table;
  }

  public String fullName() {
    return ObjectX.NonTrivial(table) ? table.name() + '.' + name() : name();
  }

  public static final int     TEXTLEN = 0;//ObjectX.INVALIDINDEX;
  public static final int     INT4LEN = 4; // duh!
  public static final int     CHARLEN = 1; // special PG datatype that is one char
  public static final int     BOOLLEN = 1; // assume one byte
  private static final int sizeForType(DBTypes type) {
    int ret = 0;
    switch(type.Value()) {
      case DBTypes.BOOL: {
        ret = BOOLLEN;
      } break;
      case DBTypes.TEXT: {
        ret = TEXTLEN;
      } break;
      case DBTypes.INT4: {
        ret = INT4LEN;
      } break;
      case DBTypes.CHAR: {
        ret = CHARLEN;
      } break;
    }
    return ret;
  }

  // creator for code-based profiles
  public static final ColumnProfile create(TableProfile table, String name,
                                           int dbtype, int size,
                                           boolean nullable, String displayName,
                                           boolean autoIncrement,
                                           String columnDef) {
    name = StringX.TrivialDefault(name, "").toLowerCase();
    ColumnProfile cp = new ColumnProfile();
    cp.table = table;
    cp.columnName = name;
    cp.type = new DBTypesFiltered(dbtype);
    dbg.VERBOSE("Just set column " + cp.fullName() + "'s type = " +
                cp.type.Image() + " for dbtype = " + dbtype);
    cp.size = size > 0 ? size : sizeForType(cp.type);
    cp.nullable = nullable;
    cp.displayName = StringX.OnTrivial(displayName, name);
    cp.autoIncrement = autoIncrement;
    // the following needs to happen in the code that creates the column in the db
    // and the code that checks the default to see if it has changed,
    // and NOT here!
    cp.columnDef = /*(cp.type.is(DBTypesFiltered.TEXT) ||
                    cp.type.is(DBTypesFiltered.CHAR)) ?
        StringX.singleQuoteEscape(columnDef) : // quote it*/
        columnDef; // otherwise don't
    return cp;
  }

  // creator for existing tables
  public static final ColumnProfile create(TableProfile table, String name,
                                           String type, String size,
                                           String nullable) {
    return create(table, name, (new DBTypesFiltered(type)).Value(),
                  StringX.parseInt(size), nullable.equalsIgnoreCase("YES"), null, false,
                  "");
  }

  public int compareTo(Object o) {
    int i = 0;
    if (ObjectX.NonTrivial(o)) {
      try {
        ColumnProfile cp = (ColumnProfile) o;
        i = name().compareTo(cp.name());
        if (i == 0) {
          return table.compareTo(cp.table);
        }
      }
      catch (Exception e) {
        dbg.ERROR("Compared different types!");
      }
      return i;
    }
    else {
      return 1; // this is bigger than null
    }
  }

  public boolean is(ColumnProfile other) {
    return (this.compareTo(other) == 0);
  }

  public final String toString() {
    return name();
  }

  public String dbReadyColumnDef() {
    return dbReadyColumnDef(type, columnDef);
  }

  public static final String dbReadyColumnDef(DBTypesFiltered type, String rawDefault) {
    rawDefault = StringX.TrivialDefault(rawDefault);
    return (type.is(DBTypesFiltered.TEXT) ||
            type.is(DBTypesFiltered.CHAR)) ?
        StringX.singleQuoteEscape(rawDefault) :
        rawDefault;
  }

  public void setDefaultFromDB(String defaultFromDB) {
    columnDef = dbUnReadyColumnDef(type, defaultFromDB);
  }

  public static final String dbUnReadyColumnDef(DBTypesFiltered type, String defaultFromDB) {
    if(StringX.NonTrivial(defaultFromDB)) {
      if(type.is(DBTypesFiltered.TEXT) ||
         type.is(DBTypesFiltered.CHAR)) {
        return StringX.unSingleQuoteEscape(defaultFromDB);
      } else {
        return defaultFromDB;
      }
    } else {
      return "";
    }
  }

  // for them to have the same name, the names must match (sans case)
  public boolean sameNameAs(ColumnProfile other) {
    return StringX.equalStrings(name(), other.name(), true);
  }

  public boolean sameTypeAs(ColumnProfile other) {
    return this.type.is(other.type.Value());
  }

  public boolean sameNullableAs(ColumnProfile other) {
    return nullable() == other.nullable();
  }

  public boolean sameDefaultAs(ColumnProfile other) {
    return StringX.equalStrings(columnDef, other.columnDef);
  }

  public boolean isProbablyId() {
    return StringX.equalStrings(StringX.right(name(), 2), "ID", /* ignore case */ true);
  }
}
//$Id: ColumnProfile.java,v 1.28 2003/08/20 18:22:29 mattm Exp $
