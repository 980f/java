package net.paymate.web.table;
import  net.paymate.database.*;
import  net.paymate.web.color.*;
import  java.sql.*;
import  org.apache.ecs.*;
import  net.paymate.util.*;

/**
 * Title:        ProfileTableGen
 * Description:  Generates an HTML table of the profile of a database table or tables
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ProfileTableGen.java,v 1.17 2003/10/30 23:06:12 mattm Exp $
 */

public class ProfileTableGen extends TableGen implements RowEnumeration, TableGenRow {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ProfileTableGen.class, ErrorLogStream.WARNING);

  private DatabaseProfile dbp = null;
  private int currentTable = -1;
  private int currentField = -1;
  private int tableSizeCount = 0;

  /**
   * @param tableName - if null means to profile ALL tables
   */
  public ProfileTableGen(PayMateDB con, ColorScheme colors, String tablename) {
    super("", colors, myheaders, null);
    dbp = con.profileDatabase("mainsail", tablename, true);
    this.title = "Profile: MAINSAIL" + (dbp.size() > 1 ? "" : ("."+dbp.itemAt(0).name()));
  }

  public static final Element output(PayMateDB con, ColorScheme colors, String tablename) {
    return new ProfileTableGen(con, colors, tablename);
  }

  // @EN@ possibly trueenum
  // put these in the order that you want the columns, BUT isNullableCol MUST BE LAST!
  private static final int columnNameCol      = 0;
  private static final int dataTypeCol        = 1;
  private static final int columnSizeCol      = 2;
  private static final int isNullableCol      = 3;
  private static final int decimalDigitsCol   = 4;
  private static final int numPrecRadixCol    = 5;
  private static final int nullAbleCol        = 6;
  private static final int remarksCol         = 7;
  private static final int columnDefCol       = 8;
  private static final int charOctetLengthCol = 9;
  private static final int ordinalPositionCol = 10;
  private static final int tableCatCol        = 11;
  private static final int tableSchemCol      = 12; // must be the last one, or else change theHeaders constructor, or else +++ use an Enumeration!

  protected static final HeaderDef[] myheaders = new HeaderDef[tableSchemCol+1]; // cause of this, VoidCol must be last!
  static {
    myheaders[tableCatCol]     = new HeaderDef(AlignType.LEFT, "tableCat");
    myheaders[tableSchemCol]     = new HeaderDef(AlignType.LEFT, "tableSchem");
    myheaders[columnNameCol]     = new HeaderDef(AlignType.LEFT, "columnName");
    myheaders[dataTypeCol]     = new HeaderDef(AlignType.LEFT, "dataType");
    myheaders[columnSizeCol]     = new HeaderDef(AlignType.LEFT, "columnSize");
    myheaders[decimalDigitsCol]     = new HeaderDef(AlignType.LEFT, "decimalDigits");
    myheaders[numPrecRadixCol]     = new HeaderDef(AlignType.LEFT, "numPrecRadix");
    myheaders[nullAbleCol]     = new HeaderDef(AlignType.LEFT, "nullAble");
    myheaders[remarksCol]     = new HeaderDef(AlignType.LEFT, "remarks");
    myheaders[columnDefCol]     = new HeaderDef(AlignType.LEFT, "Default");
    myheaders[charOctetLengthCol]     = new HeaderDef(AlignType.LEFT, "charOctetLength");
    myheaders[ordinalPositionCol]     = new HeaderDef(AlignType.LEFT, "ordinalPosition");
    myheaders[isNullableCol]     = new HeaderDef(AlignType.LEFT, "isNullable");
  };

  protected HeaderDef[] fabricateHeaders() {
    return myheaders;
  }

  public RowEnumeration rows() {
    return this;
  }

  public boolean hasMoreRows() {
    boolean ret = false;
    if(currentTable < (dbp.size()-1)) {
      ret = true;
    } else {
      TableProfile tp = dbp.itemAt(currentTable);
      if(currentField < (tp.numColumns()-1)) {
        ret = true;
      }
    }
    return ret;
  }


  private StringElement dataType = emptyElement;
  private StringElement columnSize = emptyElement;
  private StringElement decimalDigits = emptyElement;
  private StringElement numPrecRadix = emptyElement;
  private StringElement nullAble = emptyElement;
  private StringElement remarks = emptyElement;
  private StringElement columnDef = emptyElement;
  private StringElement charOctetLength = emptyElement;
  private StringElement ordinalPosition = emptyElement;
  private StringElement isNullable = emptyElement;
  private StringElement tableCat = emptyElement;
  private StringElement tableSchem = emptyElement;
  private StringElement columnName = emptyElement;
  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      dataType = emptyElement;
      columnSize = emptyElement;
      decimalDigits = emptyElement;
      numPrecRadix = emptyElement;
      nullAble = emptyElement;
      remarks = emptyElement;
      columnDef = emptyElement;
      charOctetLength = emptyElement;
      ordinalPosition = emptyElement;
      isNullable = emptyElement;
      tableCat = emptyElement;
      tableSchem = emptyElement;
      columnName = emptyElement;
      TableProfile tp = null;
      ColumnProfile cp = null;
      if(currentTable == -1) {
        currentTable = Math.min(dbp.size()-1, 0);
      }
      tp = dbp.itemAt(currentTable);
      if(tp != null) {
        // increment
        if((++currentField) >= tp.numColumns()) {
          // if we ran out of columns in this table, use the next table
          tp = dbp.itemAt(++currentTable);
          //tableSizeCount = 0;
          if(tp != null) {
            // and get the first record in that table
            // shouldn't be a problem since in most DBMS's a table is required to have at least one field
            currentField = Math.min(0, tp.numColumns()-1);
          }
        }
        if(tp != null) {
          tgr = this;
        }
      }
      if(tgr != null) {
        tp = dbp.itemAt(currentTable);
        cp = tp.column(currentField);
        tableSizeCount += cp.size();
        dataType = new StringElement(cp.type());
        columnSize = new StringElement(""+cp.size());
        decimalDigits = new StringElement(cp.decimalDigits);
        numPrecRadix = new StringElement(cp.numPrecRadix);
        nullAble = new StringElement(cp.nullAble);
        remarks = new StringElement(cp.remarks);
        columnDef = new StringElement(cp.columnDef);
        charOctetLength = new StringElement(cp.charOctetLength);
        ordinalPosition = new StringElement(cp.ordinalPosition);
        isNullable = new StringElement(""+cp.nullable());
        tableCat = new StringElement(cp.tableCat);
        tableSchem = new StringElement(cp.tableSchem);
        columnName = new StringElement(((dbp.size() > 1) ? (cp.table()+".") : "") + cp.name());
      }
    } catch (Exception e2) {
      dbg.Caught(e2);
    } finally {
      dbg.Exit();
    }
    return tgr;
  }

  public int numColumns() {
    return headers.length;
  }

  public Element column(int col) {
    Element el = emptyElement;
    // +_+ enumerate
    switch(col) {
      case columnSizeCol: {
        el = columnSize;
      } break;
      case decimalDigitsCol: {
        el = decimalDigits;
      } break;
      case numPrecRadixCol: {
        el = numPrecRadix;
      } break;
      case nullAbleCol: {
        el = nullAble;
      } break;
      case remarksCol: {
        el = remarks;
      } break;
      case columnDefCol: {
        el = columnDef;
      } break;
      case charOctetLengthCol: {
        el = charOctetLength;
      } break;
      case ordinalPositionCol: {
        el = ordinalPosition;
      } break;
      case isNullableCol: {
        el = isNullable;
      } break;
      case tableCatCol: {
        el = tableCat;
      } break;
      case tableSchemCol: {
        el = tableSchem;
      } break;
      case columnNameCol: {
        el = columnName;
      } break;
      case dataTypeCol: {
        el = dataType;
      } break;
    }
    return el;
  }

  protected int footerRows() {
    return 1; // means not inited; use defaults
  }

  protected Element footer(int row, int col) {
    Element footerCell = emptyElement;
    switch(col) {
      case columnNameCol: {
        footerCell = new StringElement("Count: " + rowsYet());
      } break;
      case columnSizeCol: {
        footerCell = (dbp.size() > 1) ? emptyElement : new StringElement("Width: "+tableSizeCount);
      } break;
    }
    return footerCell;
  }
}

