/**
* Title:        BackupJobFormat<p>
* Description:  The canned query for the Appliances screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: BackupJobFormat.java,v 1.2 2001/11/16 01:34:33 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.data.*; // TimeRange
import  net.paymate.database.*; // db
import  net.paymate.database.ours.*; // DBConstants
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.*; // logininfo
import  net.paymate.web.page.*; // Acct
import  net.paymate.connection.*;
import  java.sql.*; // resultset
import  java.util.*; // ARRAYS for sorting
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements

public class BackupJobFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = new ErrorLogStream(BackupJobFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef[(new BackupJobFormatEnum()).numValues()];
  static { // order is in the .Enum file
    theHeaders[BackupJobFormatEnum.NumberCol]    = new HeaderDef(AlignType.RIGHT  , "#");
    theHeaders[BackupJobFormatEnum.StartTimeCol] = new HeaderDef(AlignType.LEFT   , "Enqueued");
    theHeaders[BackupJobFormatEnum.StartedByCol] = new HeaderDef(AlignType.LEFT   , "Started By");
    theHeaders[BackupJobFormatEnum.PriorityCol]  = new HeaderDef(AlignType.CENTER , "Pri");
    theHeaders[BackupJobFormatEnum.SleepCol]     = new HeaderDef(AlignType.CENTER , "Sleep");
    theHeaders[BackupJobFormatEnum.StatusCol]    = new HeaderDef(AlignType.CENTER , "Status");
    theHeaders[BackupJobFormatEnum.TableNameCol] = new HeaderDef(AlignType.LEFT   , "Table");
    theHeaders[BackupJobFormatEnum.WidthCol]     = new HeaderDef(AlignType.RIGHT  , "Width");
    theHeaders[BackupJobFormatEnum.RowCountCol]  = new HeaderDef(AlignType.RIGHT  , "Rows");
    theHeaders[BackupJobFormatEnum.BytesCol]     = new HeaderDef(AlignType.RIGHT  , "Read B");
    theHeaders[BackupJobFormatEnum.DurationCol]  = new HeaderDef(AlignType.RIGHT  , "Duration");
    theHeaders[BackupJobFormatEnum.FilenameCol]  = new HeaderDef(AlignType.LEFT   , "Filename");
    theHeaders[BackupJobFormatEnum.MessageCol]   = new HeaderDef(AlignType.LEFT   , "Errors");
    theHeaders[BackupJobFormatEnum.FileSizeCol]  = new HeaderDef(AlignType.RIGHT  , "Wrote B");
    theHeaders[BackupJobFormatEnum.PercentCol]   = new HeaderDef(AlignType.RIGHT  , "%");
  }

  Enumeration jobs = null;
  LocalTimeFormat ltf = null;

  public BackupJobFormat(LoginInfo linfo, BackupAgent agent, String title) {
    super(title, linfo.colors, theHeaders, null, -1, null);
    ltf = linfo.ltf;
    jobs = agent.jobs();
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

  /**
   * the next is a cursor through the set of appliances
   */
  BackupJob job = null;

  public Element column(int col) {
    String str = " ";
    if(job != null) {
      switch(col) {
        case BackupJobFormatEnum.NumberCol: {
          str = ""+job.jobNumber();
        } break;
        case BackupJobFormatEnum.StartTimeCol: {
          str = ltf.format(job.reqTime());
        } break;
        case BackupJobFormatEnum.StartedByCol: {
          str = job.reqWho();
        } break;
        case BackupJobFormatEnum.PriorityCol: {
          str = ""+job.threadPriority();
        } break;
        case BackupJobFormatEnum.SleepCol: {
          str = ""+job.perRowSleepMs();
        } break;
        case BackupJobFormatEnum.StatusCol: {
          str = job.done ? (job.succeeded ? "Y" : "!") :
                           (job.started   ? "@" : ".");
        } break;
        case BackupJobFormatEnum.DurationCol: {
          str = (job.done || job.started) ? Safe.millisToSecsPlus(job.duration()) : ".";
        } break;
        case BackupJobFormatEnum.TableNameCol: {
          str = job.table();
        } break;
        case BackupJobFormatEnum.MessageCol: {
          str = job.errors.asParagraph();
        } break;
        case BackupJobFormatEnum.RowCountCol: {
          str = (job.done || job.started) ? ""+job.wrote().getCount() : ".";
        } break;
        case BackupJobFormatEnum.WidthCol: {
          str = ""+job.width();
        } break;
        case BackupJobFormatEnum.BytesCol: {
          str = (job.done || job.started) ? Safe.sizeLong(job.wrote().getTotal()) : ".";
        } break;
        case BackupJobFormatEnum.FilenameCol: {
          str = job.filename();
        } break;
        case BackupJobFormatEnum.FileSizeCol: {
          str = ""+Safe.sizeLong(job.filesize());
        } break;
        case BackupJobFormatEnum.PercentCol: {
          str = ""+Safe.percent(job.filesize(), job.wrote().getTotal());
        } break;
      }
    }
    return new StringElement(str);
  }
  public boolean hasMoreRows() {
    return jobs.hasMoreElements();
  }
  private int currentRow = -1;
  public TableGenRow nextRow() {
    return ((job = (BackupJob)jobs.nextElement()) == null) ? null : this;
  }
}
//$Id: BackupJobFormat.java,v 1.2 2001/11/16 01:34:33 mattm Exp $
