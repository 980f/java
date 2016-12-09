/**
* Title:        StatementsFormat<p>
* Description:  The canned query for the Appliances screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: StatementsFormat.java,v 1.1 2001/10/24 11:00:13 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.util.*; // ErrorlogStream
import  java.sql.*; // resultset
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.web.color.*;

// +++ Use ArrayTableGen for this instead of a whole other class?

public class StatementsFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = new ErrorLogStream(StatementsFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new StatementsFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[StatementsFormatEnum.numCol]  = new HeaderDef(AlignType.LEFT   , "#");
    theHeaders[StatementsFormatEnum.nameCol] = new HeaderDef(AlignType.LEFT   , "Statement");
  }

  private TextList tl = null;
  public StatementsFormat(ColorScheme colors, String title) {
    super(title, colors, theHeaders, null, -1, null);
    tl = TableGen.getUnclosedStatements();
  }

  protected RowEnumeration rows() {
    return this;
  }
  protected HeaderDef[] fabricateHeaders() {
    return headers;
  }
  public int numColumns() {
    return headers.length;
  }

  String stmtNamed = " ";

  public Element column(int col) {
    String str = " ";

    switch(col) {
      case StatementsFormatEnum.numCol: {
        str = ""+currentRow;
      } break;
      case StatementsFormatEnum.nameCol: {
        str = ""+stmtNamed;
      } break;
    }
    return new StringElement(str);
  }
  public boolean hasMoreRows() {
    return currentRow < (tl.size() - 1);
  }
  private int currentRow = -1;
  public TableGenRow nextRow() {
    stmtNamed = tl.itemAt(++currentRow);
    return this;
  }
}
//$Id: StatementsFormat.java,v 1.1 2001/10/24 11:00:13 mattm Exp $
