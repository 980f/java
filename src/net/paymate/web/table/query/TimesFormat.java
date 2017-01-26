package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/TimesFormat.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.10 $
 */

import  net.paymate.web.table.*;
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.page.*; // Acct
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.web.*;
import  net.paymate.web.color.*;
import  java.util.*;
import  net.paymate.servlet.*;

public class TimesFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(TimesFormat.class, ErrorLogStream.WARNING);
  // not really enough for an enumeration
  private static final int ZONECOL = 0;
  private static final int TIMECOL = 1;
  private static final HeaderDef [] headers = {
    new HeaderDef(AlignType.LEFT, "Zone/Info"),
    new HeaderDef(AlignType.LEFT, "Time"),
  };

  TimeZone times = null;
  Date date = null;

  public TimesFormat(ColorScheme colors, String title, TimeZone tz) {
    super(title, colors, headers, "");
    this.times = tz;
    date = DateX.Now();
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

  private static final String UTC = "UTC";
  private static final String CDT = "America/Chicago";
  private static final String format = "yyyyMMdd.HHmmss.SSS z";

  private static final int DEFAULTROW = 0;
  private static final int UTCTIMEROW = 1;
  private static final int CDTTIMEROW = 2;
  private static final int YOUTIMEROW = 3;
  private static final int UPTIMEROW  = 4;
  private static final int TTLTIMEROW = 5;

  public Element column(int col) {
    String str = "";
    switch(col) {
      case ZONECOL: {
        switch(currentRow) {
          case DEFAULTROW: {
            str = "Default [" + TimeZone.getDefault().getDisplayName() + "]";
          } break;
          case UTCTIMEROW: {
            str = UTC;
          } break;
          case CDTTIMEROW: {
            str = CDT;
          } break;
          case YOUTIMEROW: {
            str = "Your terminal [" + times.getDisplayName() + "]";
          } break;
          case UPTIMEROW: {
            str = "Up Since";
          } break;
          case TTLTIMEROW: {
            str = "Total uptime";
          } break;
        }
      } break;
      case TIMECOL: {
        switch(currentRow) {
          case DEFAULTROW: {
            str = LocalTimeFormat.New(TimeZone.getDefault(), format).format(date);
          } break;
          case UTCTIMEROW: {
            str = LocalTimeFormat.New(TimeZone.getTimeZone(UTC), format).format(date);
          } break;
          case CDTTIMEROW: {
            str = LocalTimeFormat.New(TimeZone.getTimeZone(CDT), format).format(date);
          } break;
          case YOUTIMEROW: {
            str = LocalTimeFormat.New(times, format).format(date);
          } break;
          case UPTIMEROW: {
            str = LocalTimeFormat.New(times, format).format(new Date(Service.system.upsince()));
          } break;
          case TTLTIMEROW: {
            str = Service.system.svcAvgTime();
          } break;
        }
      } break;
    }
    return new StringElement(str);
  }

  private static final int NUMROWS = 6;
  public boolean hasMoreRows() {
    return currentRow < (NUMROWS-1);
  }
  private int currentRow = -1;
  public TableGenRow nextRow() {
    currentRow++;
    return this;
  }

}
//$Id: TimesFormat.java,v 1.10 2003/10/30 21:05:19 mattm Exp $
