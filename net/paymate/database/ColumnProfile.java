package net.paymate.database;
/**
 * Title:        ColumnProfile
 * Description:  Contains the Column information used by database profiling, etc.
 *                These objects are fixed upon creation.  You can't change one once it is built.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: ColumnProfile.java,v 1.12 2001/11/16 01:34:28 mattm Exp $
 */

import net.paymate.util.*;

public class ColumnProfile implements Comparable {
  private static final ErrorLogStream dbg = new ErrorLogStream(ColumnProfile.class.getName());

/*
  * <P>Each column description has the following columns:
  * <OL>
  *   <LI><B>TABLE_CAT</B> String => table catalog (may be null)
  *   <LI><B>TABLE_SCHEM</B> String => table schema (may be null)
  *   <LI><B>TABLE_NAME</B> String => table name
  *   <LI><B>COLUMN_NAME</B> String => column name
  *   <LI><B>DATA_TYPE</B> short => SQL type from java.sql.Types
  *   <LI><B>TYPE_NAME</B> String => Data source dependent type name,
  *     for a UDT the type name is fully qualified
  *   <LI><B>COLUMN_SIZE</B> int => column size.  For char or date
  *     types this is the maximum number of characters, for numeric or
  *     decimal types this is precision.

  *   <LI><B>BUFFER_LENGTH</B> is not used.

  *   <LI><B>DECIMAL_DIGITS</B> int => the number of fractional digits
  *   <LI><B>NUM_PREC_RADIX</B> int => Radix (typically either 10 or 2)
  *   <LI><B>NULLABLE</B> int => is NULL allowed?
  *     <UL>
  *       <LI> columnNoNulls - might not allow NULL values
  *       <LI> columnNullable - definitely allows NULL values
  *       <LI> columnNullableUnknown - nullability unknown
  *     </UL>
  *   <LI><B>REMARKS</B> String => comment describing column (may be null)
  *   <LI><B>COLUMN_DEF</B> String => default value (may be null)

  *   <LI><B>SQL_DATA_TYPE</B> int => unused
  *   <LI><B>SQL_DATETIME_SUB</B> int => unused

  *   <LI><B>CHAR_OCTET_LENGTH</B> int => for char types the
  *     maximum number of bytes in the column
  *   <LI><B>ORDINAL_POSITION</B> int	=> index of column in table
  *     (starting at 1)
  *   <LI><B>IS_NULLABLE</B> String => "NO" means column definitely
  *     does not allow NULL values; "YES" means the column might
  *     allow NULL values.  An empty string means nobody knows.
  * </OL>
  */
  // database metadata stuff:
  public String tableCat        = "";
  public String tableSchem      = "";
  private String tableName       = "";
  private String columnName      = "";
  public String dataType        = "";
  private DBTypesFiltered type; //ugly name, when wrapping an enum give the enum an ugly name!
  private String columnSize      = "";
  private int    size = -1;  // +++ enforce restrictions?
  public String decimalDigits   = "";
  public String numPrecRadix    = "";
  public String nullAble        = "";
  public String remarks         = "";
  public String columnDef       = "";
  public String charOctetLength = "";
  public String ordinalPosition = "";
  private String isNullable      = "";
  private boolean nullable = false;
  private String displayName = ""; // for what goes at the top of a column when displaying via webpages, etc
  private boolean autoIncrement = false;

  // constraints
  public static final String _NOTNULL = "NOT NULL";
  public static final String _ALLOWNULL = "";
  public static final boolean NOTNULL = false;
  public static final boolean ALLOWNULL = true;
  public static final boolean NOAUTO = false;
  public static final boolean AUTO = true;

  private static final String YES = "YES";
  private static final String NO = "NO";

  //////////////////////////
  private ColumnProfile() {
    //use create
  }

  /**
   * returns correctenss of strucutre, not whether it is a member of the enclosing database.
   */
  public boolean isViable(){
    return Safe.NonTrivial(columnName);//&&type in typelist&&nullable is valid ...
  }

  public String name() {
    return columnName;
  }

  public String ofTable(TableProfile parent) {
    return parent.name()+'.'+columnName;
  }

  public String type() {
    return type.Image();
  }

  public boolean nullable() {
    return nullable;
  }

  public String nullImage() {
    return nullImage(nullable());
  }

  public static final String nullImage(boolean nullable) {
    return nullable?_ALLOWNULL:_NOTNULL;
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

  public String table() {
    return tableName;
  }

  public static final ColumnProfile create(String table, String name, int dbtype, int size, boolean nullable, String displayName, boolean autoIncrement) {
    ColumnProfile cp = new ColumnProfile();
    cp.tableName = table;
    cp.columnName = name;
    cp.type = new DBTypesFiltered(dbtype);
    cp.size = size;
    cp.isNullable = nullable ? YES : NO;
    cp.nullable = nullable;
    cp.displayName = Safe.OnTrivial(displayName,name);
    cp.autoIncrement = autoIncrement;
    return cp;
  }

  //creator for virtual tables (those built by queries)
  public static final ColumnProfile create(String table, String name, String dbtype, int size, boolean nullable) {
    return create(table,name,(new DBTypesFiltered(dbtype)).Value(),size,nullable/*.equals(ColumnProfile._ALLOWNULL)*/,null,false);
  }

  // creator for real tables
  public static final ColumnProfile create(String table, String name, String type, String size, String nullable) {
    return create(table,name,type,Safe.parseInt(size),nullable.equalsIgnoreCase(YES));
  }

  public int compareTo(Object o) {
    int i = 0;
    try {
      ColumnProfile cp = (ColumnProfile)o;
      i = name().compareTo(cp.name());
    } catch (Exception e) {
      // +++ bitch
    }
    return i;
  }

}
//$Id: ColumnProfile.java,v 1.12 2001/11/16 01:34:28 mattm Exp $
