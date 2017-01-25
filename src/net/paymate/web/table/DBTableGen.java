/**
 * Title:        DBTableGen<p>
 * Description:  generates an html table from a sql resultset <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: DBTableGen.java,v 1.19 2001/10/24 04:14:19 mattm Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.database.DBMacros;
import  net.paymate.web.color.*;
import  java.sql.*;
import  org.apache.ecs.*;

public abstract class DBTableGen extends TableGen implements TableGenRow, RowEnumeration {
  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(DBTableGen.class.getName());

  protected ResultSet rs        = null;

  public DBTableGen(String title, ColorScheme colors, ResultSet rs, String absoluteURL, int howMany, String sessionid) {
    this(title, colors, rs, /*headers*/ null, absoluteURL, howMany, sessionid);
  }

  public DBTableGen(String title, ColorScheme colors, ResultSet rs, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(title, colors, headers, absoluteURL, howMany, sessionid);
    this.rs = rs;
  }

  public RowEnumeration rows() {
    return this;
  }

  protected ResultSetMetaData rsmd = null;
  protected void getRSMD() {
    if(rsmd == null) {
      rsmd = DBMacros.getRSMD(rs);
    }
  }

  // overload these to extend the class ...
  public abstract int numColumns();
  public abstract Element column(int col);

  public void close() {
    super.close();
    DBMacros.closeStmt(DBMacros.getStatement(rs));
  }

  public TableGenRow nextRow() {
    // +_+ maybe should let it throw?
    // +_+ add better error reporting
    try {
      if(DBMacros.next(rs)) {
        return this;
      }
    } catch (Exception t) {
      dbg.ERROR("nextRow() Exception skipping to the next record!");
      dbg.Caught(t);
    }
    return null;
  }

  public boolean hasMoreRows() {
    boolean ret = false;
    try {
      if(rs!=null) {
        if(rs.getType() != ResultSet.TYPE_FORWARD_ONLY) {
          ret = rs.isAfterLast() || rs.isLast(); // isAfterLast() is less expensive
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

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = null;
    getRSMD();
    if(numColumns() > 0) {
      headers = new HeaderDef[numColumns()];
      for(int col = headers.length; col-->0; ) {
        int rsCol = col+1;
        String label = "<sqlerror>";
        if(rsmd != null) {
          try {
            label = rsmd.getColumnLabel(rsCol);
          } catch (SQLException e) {
            dbg.Enter("fabricateHeaders");
            dbg.Caught(e);
            dbg.Exit();
          } finally {
          }
        }
        headers[col] = new HeaderDef(AlignType.LEFT, new StringElement(label));
      }
    }
    return headers;
  }

}


