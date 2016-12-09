/**
* Title:        LogFileFormat<p>
* Description:  The canned display format query for the LogFile screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: LogFileFormat.java,v 1.2 2001/11/03 13:16:44 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.color.*;
import  net.paymate.web.page.*; // Acct
import  net.paymate.connection.*;
import  java.sql.*; // resultset
import  java.util.*; // ARRAYS for sorting
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.authorizer.*;

public class LogFileFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = new ErrorLogStream(LogFileFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new LogFileFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[LogFileFormatEnum.nameCol]        = new HeaderDef(AlignType.LEFT   , "Name");
    theHeaders[LogFileFormatEnum.pendingLinesCol]= new HeaderDef(AlignType.RIGHT   , "Pending Lines");
    theHeaders[LogFileFormatEnum.statusCol]      = new HeaderDef(AlignType.LEFT   , "Filename");
  }

  LogFile [] logFiles = LogFile.listAll();

  public LogFileFormat(ColorScheme colors, String title) {
    super(title, colors, theHeaders, null, -1, null);
    headers = theHeaders;
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
  public boolean hasMoreRows() {
    return currentRow < (logFiles.length - 1);
  }
  private int currentRow = -1;

  public TableGenRow nextRow() {
    currentRow++;
    return this;
  }

  public Element column(int col) {
    LogFile logFile = logFiles[currentRow];
    String str = "";
    switch(col) {
      case LogFileFormatEnum.nameCol: {
        str = logFile.getName();
      } break;
      case LogFileFormatEnum.pendingLinesCol: {
        str = ""+logFile.pending();
      } break;
      case LogFileFormatEnum.statusCol: {
        str = logFile.filename();
      } break;
    }
    return new StringElement(str);
  }
}

//$Id: LogFileFormat.java,v 1.2 2001/11/03 13:16:44 mattm Exp $
