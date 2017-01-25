/**
 * Title:        AnyDBTableGen<p>
 * Description:  Generic form of DBTableGen that can output any SQL query <p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: AnyDBTableGen.java,v 1.10 2001/10/11 04:34:03 mattm Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.*; // ErrorLogStream, Safe
import  net.paymate.web.color.*;
import  java.sql.*;
import  org.apache.ecs.*;
import  net.paymate.database.DBMacros;

public class AnyDBTableGen extends DBTableGen {
  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(AnyDBTableGen.class.getName());

  public AnyDBTableGen(String title, ColorScheme colors, ResultSet rs, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(title, colors, rs, headers, absoluteURL, howMany, sessionid);
  }

  public static final Element output(String title, ColorScheme colors, ResultSet rs, HeaderDef headers[], String sessionid) {
    return output(title, colors, rs, headers, null, -1, sessionid);
  }

  public static final Element output(String title, ColorScheme colors, ResultSet rs, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    return new AnyDBTableGen(title, colors, rs, headers, absoluteURL, howMany, sessionid);
  }

  protected int numCols = -1;
  public int numColumns() {
    if(numCols == -1) {
      numCols = 0;
      getRSMD();
      if(rsmd != null) {
        try {
          numCols = rsmd.getColumnCount();
        } catch(Exception caught) {
          dbg.Caught(caught);
        }
      }
    }
    return numCols;
  }

  public Element column(int col) {
    return new StringElement(Safe.TrivialDefault(DBMacros.getStringFromRS(col+1, rs), " "));
  }

  public void close() {
    Statement stmt = DBMacros.getStatement(rs);
    DBMacros.closeStmt(stmt);
    rs = null;
    super.close();
  }

}