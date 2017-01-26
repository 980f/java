/**
* Title:        AuthorizersFormat<p>
* Description:  The canned query for the Authorizers screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: AuthorizersFormat.java,v 1.15 2001/11/17 20:06:37 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.net.*; // SendMail
import  net.paymate.web.*; // logininfo
import  net.paymate.web.color.*;
import  net.paymate.web.page.*; // Acct
import  net.paymate.servlet.*;
import  net.paymate.connection.*;
import  java.sql.*; // resultset
import  java.util.*; // ARRAYS for sorting
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.authorizer.*;
import  javax.servlet.http.*;

public class AuthorizersFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = new ErrorLogStream(AuthorizersFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new AuthorizersFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[AuthorizersFormatEnum.nameCol]        = new HeaderDef(AlignType.LEFT   , "Name");
    theHeaders[AuthorizersFormatEnum.statusCol]      = new HeaderDef(AlignType.LEFT   , "^?");
    theHeaders[AuthorizersFormatEnum.connectionsCol] = new HeaderDef(AlignType.RIGHT  , "Cnxs");
    theHeaders[AuthorizersFormatEnum.txnsCol]        = new HeaderDef(AlignType.RIGHT  , "Txns");
    theHeaders[AuthorizersFormatEnum.pendCol]        = new HeaderDef(AlignType.RIGHT  , "Pend");
    theHeaders[AuthorizersFormatEnum.avgTimeCol]     = new HeaderDef(AlignType.RIGHT  , "Avg Txn");
    theHeaders[AuthorizersFormatEnum.timeOutsCol]    = new HeaderDef(AlignType.RIGHT  , "TimeX");
    theHeaders[AuthorizersFormatEnum.writeCol]       = new HeaderDef(AlignType.RIGHT  , "Wrote");
    theHeaders[AuthorizersFormatEnum.readCol]        = new HeaderDef(AlignType.RIGHT  , "Read");
    theHeaders[AuthorizersFormatEnum.logFileCol]     = new HeaderDef(AlignType.LEFT   , "LogFile");
    theHeaders[AuthorizersFormatEnum.notesCol]       = new HeaderDef(AlignType.LEFT   , "Notes");
  }

  Authorizer [] auths = null;
  private int count = 0;
  private static final int SPECIALROWS = 9;
  private HttpSessionContext scontext = null;
  private BackupAgent backupAgent = null;
  private SendMail mailer = null;
  private StatusClient statter = null;
  private String receiptFilePath = "";
  public AuthorizersFormat(ColorScheme colors, AuthManager mgr, BackupAgent backupAgent, SendMail mailer, StatusClient statter, String receiptFilePath, String title, HttpSessionContext scontext) {
    super(title, colors, theHeaders, null, -1, null);
    auths = mgr.listAsArray();
    count = auths.length+SPECIALROWS;
    this.scontext = scontext;
    this.receiptFilePath = receiptFilePath;
    this.backupAgent = backupAgent;
    this.mailer = mailer;
    this.statter = statter;
    jobs = backupAgent.jobs();
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
    return currentRow < (count - 1);
  }
  private int currentRow = -1;

  public TableGenRow nextRow() {
    currentRow++;
    return this;
  }

  private Enumeration jobs = null;

  public String statusClientColumn(int col) {
    String str = "";
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "IPStatClient";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(statter.isUp());
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = statter.doing() ? "1" : "0";
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = of(statter.sentBytes().getCount(), statter.sendAttempts().value());
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = ""+statter.connections().value();
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = Safe.millisToSecsPlus(statter.sendDuration().getAverage());
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = printByteStats(statter.sentBytes());
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = "+++ NEED a logfile?";
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = statter.status();
      } break;
    }
    return str;
  }

  public String mailerColumn(int col) {
    String str = "";
    // put this in its own table?
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "SendMail";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(true); // +++ check the actual connection
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = "-"; // +++
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = "<-- +++ DO THESE !!!";
      } break;
    }
    return str;
  }

  public String dbBackupColumn(int col) {
    String str = "";
    // put this in its own table?
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "DbBackup";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(true); // +++ check the actual connection
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = ""+backupAgent.pendingCount();
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        long ttl = backupAgent.ttlJobCount.value();
        str = of(ttl - backupAgent.pendingCount(), ttl);
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-"; // ? +++
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = Safe.millisToSecsPlus(backupAgent.times.getAverage());
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = printByteStats(backupAgent.bytesWritten);
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = printByteStats(backupAgent.bytesRead);
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = Safe.TrivialDefault(backupAgent.toString(),"-");
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = "ComprXn ~ " + Safe.percent(backupAgent.bytesWritten.getTotal(), backupAgent.bytesRead.getTotal()) + "["+backupAgent.bytesWritten.getTotal()+"/"+backupAgent.bytesRead.getTotal()+"]" +"%";
      } break;
    }
    return str;
  }

  public String dbColumn(int col) {
    String str = "";
    // put this in its own table?
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "Database";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(true); // +++ check the actual connection
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = ""+(DBMacros.queryStats.getCount()+DBMacros.updateStats.getCount());
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = ""+0; // ? +++
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = ""+DBMacros.connectionCount();
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = Safe.millisToSecsPlus((DBMacros.queryStats.getTotal()+DBMacros.updateStats.getTotal()) / (DBMacros.queryStats.getCount()+DBMacros.updateStats.getCount()));
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = printStats(DBMacros.updateStats);
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = printStats(DBMacros.queryStats);//printStats(DBMacros.nextStats);
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = DBMacros.logFile.status();
      } break;
      case AuthorizersFormatEnum.notesCol: {
        long min = Math.min(DBMacros.queryStats.getMin(),DBMacros.updateStats.getMin());
        long max = Math.max(DBMacros.queryStats.getMax(),DBMacros.updateStats.getMax());
        str = "TxnTimes ["+min+"-"+max+"]";
      } break;
    }
    return str;
  }

  public String userSessionColumn(int col) {
    String str = "";
    // put this in its own table?
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "UserSession";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(!UserSession.isDown());
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = ""+TableGen.getUnclosedStatements().size();
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = ErrorLogStream.fpf.status();// even though ErrorLogStream handles it, UserSession will report on the logfile, since it started it.
      } break;
      case AuthorizersFormatEnum.notesCol: {
//        str = "-";
        long freemem = Runtime.getRuntime().freeMemory();
        long ttlmem = Runtime.getRuntime().totalMemory();
        str = "" + (freemem * 100 / ttlmem) + "% freemem [" + Safe.sizeLong(freemem) + "/" + Safe.sizeLong(ttlmem) + " bytes]";
      } break;
    }
    return str;
  }

  public String connectionServerColumn(int col) {
    String str = "";
    // put this in its own table?
    ConnectionServer cs = UserSession.connectionServer;
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "ConnectionServer";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(cs.isUp());
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = of(cs.completes.value(), cs.attempts.value());
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = ""+(cs.completes.value()-cs.attempts.value());
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = cs.logFile.status();
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = "-";
      } break;
    }
    return str;
  }

  public String logFileColumn(int col) {
    String str = "";
    int logFileLengths = LogFile.listAll().length;
    // put this in its own table?
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "LogFile";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(true); // +++ check the actual connection
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = ""+LogFile.writes.getCount();
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = ""+LogFile.allPending();
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = ""+logFileLengths;
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = Safe.millisToSecsPlus(LogFile.writeTimes.getTotal() / LogFile.writes.getCount());
      } break;
      case AuthorizersFormatEnum.writeCol: {
        str = printByteStats(LogFile.writes);
      } break;
      case AuthorizersFormatEnum.readCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = diskSpaceFree(LogFile.getPath())+" ["+LogFile.getPath()+"]";
      } break;
    }
    return str;
  }

  private static final String diskSpaceFree(String path) {
    TextList msgs = new TextList();
    int c = Safe.diskfree(path, msgs);
    String output = msgs.itemAt(1);
    String percentUsed = parseForPercent(output);
    return ""+(100 - Safe.parseInt(percentUsed)) + "% disk space free";
  }

  private static final String parseForPercent(String toParse) {
    int i = toParse.indexOf("%");
    String percent = "";
    while(i > -1) {
      char c = toParse.charAt(--i);
      if(c == ' ') {
        break;
      }
      percent = "" + c + percent;
    }
    return percent;
  }

  public String receiptStorageColumn(int col) {
    String str = "";
    // put this in its own table?
    ConnectionServer cs = UserSession.connectionServer;
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "ReceiptStore";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(true); // +++ check the actual connection
      } break;
      case AuthorizersFormatEnum.txnsCol: {
        str = ""+(cs.receiptsRead.getCount()+cs.receiptsWritten.getCount()); // how many read & written to disk
      } break;
      case AuthorizersFormatEnum.pendCol: {
        str = "0";  // +++ eventualy, this will list the qty of unreconciled receipts, which will occur on-the-fly in the background
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.connectionsCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.avgTimeCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.writeCol: { // how many written to disk
        str = printByteStats(cs.receiptsWritten);
      } break;
      case AuthorizersFormatEnum.readCol: { // how may read from disk
        str = printByteStats(cs.receiptsRead);
      } break;
      case AuthorizersFormatEnum.logFileCol: {
        str = "-";
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = diskSpaceFree(receiptFilePath)+" ["+receiptFilePath+"]";
      } break;
    }
    return str;
  }

  public String httpSessionsColumn(int col) {
    String str = "";
    // put this in its own table?
    switch(col) {
      case AuthorizersFormatEnum.nameCol: {
        str = "Http";
      } break;
      case AuthorizersFormatEnum.statusCol: {
        str = upText(true); // +++ check the actual connection
      } break;
      case AuthorizersFormatEnum.txnsCol: { // how many txns handled through servlets
        str = ""+SessionedServlet.httptimer.getCount();
      } break;
      case AuthorizersFormatEnum.pendCol: { // how many txns still being handled
        str = ""+SessionedServlet.pending.value();
      } break;
      case AuthorizersFormatEnum.timeOutsCol: {
        str = ""+SessionCleaner.count.value();//"Invalidated " + SessionCleaner.count.value() + " stale sessions.";
      } break;
      case AuthorizersFormatEnum.connectionsCol: { // how many open httpSessions
        int i = 0;
        for(Enumeration ennum = scontext.getIds(); ennum.hasMoreElements();i++) {
          ennum.nextElement();
        }
        str = ""+i;
      } break;
      case AuthorizersFormatEnum.avgTimeCol: { // avg time to respond to an http request
        str = Safe.millisToSecsPlus(SessionedServlet.httptimer.getAverage());
      } break;
      case AuthorizersFormatEnum.writeCol: { // bytes written
        str = printByteStats(SessionedServlet.outgoing);
      } break;
      case AuthorizersFormatEnum.readCol: { // est of bytes read
        str = "~ "+printByteStats(SessionedServlet.incoming);
      } break;
      case AuthorizersFormatEnum.logFileCol: { // ???
        str = "-";
      } break;
      case AuthorizersFormatEnum.notesCol: {
        str = "-";
      } break;
    }
    return str;
  }

  // @EN@ enumerate the "special" rows

  public Element column(int col) {
    String str = "";
    switch(currentRow) {
      case 4: { // database
        str = dbColumn(col);
      } break;
      case 5: { // databasebackup
        str = dbBackupColumn(col);
      } break;
      case 8: { // logfile
        str = logFileColumn(col);
      } break;
      case 7: { // statusclient
        str = statusClientColumn(col);
      } break;
      case 6: { // sendmail
        str = mailerColumn(col);
      } break;
      case 3: { // receiptStorage
        str = receiptStorageColumn(col);
      } break;
      case 2: { // connectionserver
        str = connectionServerColumn(col);
      } break;
      case 1: { // usersession
        str = userSessionColumn(col);
      } break;
      case 0: { // httpSessions
        str = httpSessionsColumn(col);
      } break;
      default : {
        Authorizer auth = auths[currentRow-SPECIALROWS];
        switch(col) {
          case AuthorizersFormatEnum.nameCol: {
            str = auth.name+"Auth";
          } break;
          case AuthorizersFormatEnum.statusCol: {
            str = upText(auth.isup());
          } break;
          case AuthorizersFormatEnum.txnsCol: {
            str = ""+auth.txnTimes.getCount();
          } break;
          case AuthorizersFormatEnum.pendCol: {
            str = ""+auth.queuedStandins();
          } break;
          case AuthorizersFormatEnum.timeOutsCol: {
            str = ""+auth.timeouts.value();
          } break;
          case AuthorizersFormatEnum.connectionsCol: {
            str = of(auth.connections.value(), auth.connectionAttempts.value());
          } break;
          case AuthorizersFormatEnum.avgTimeCol: {
            str = Safe.millisToSecsPlus(auth.txnTimes.getAverage());
          } break;
          case AuthorizersFormatEnum.writeCol: {
            str = printByteStats(auth.writes);
          } break;
          case AuthorizersFormatEnum.readCol: {
            str = printByteStats(auth.reads);
          } break;
          case AuthorizersFormatEnum.logFileCol: {
            str = auth.logFile.status();
          } break;
          case AuthorizersFormatEnum.notesCol: {
            str = "-";
          } break;
        }
      } break;
    }
    return new StringElement(str);
  }

  private static final String upText(boolean isUp) {
    return isUp ? "UP" : "DOWN";
  }

  private static final String printStats(Accumulator ua) {
    return "" + ua.getCount() + " @ " + ua.getAverage() + " ms ea";
  }

  private static final String printByteStats(Accumulator ua) {
    return of(Safe.sizeLong(ua.getTotal()), ua.getCount())+" = "+Safe.sizeLong(ua.getAverage()) + " B";
  }

  private static final String of(long This, long That) { // aka div
    return of(""+This, That);
  }

  private static final String of(String This, long That) { // aka div
    return ""+This+" / "+That;
  }
}

//$Id: AuthorizersFormat.java,v 1.15 2001/11/17 20:06:37 mattm Exp $
