package net.paymate.web.table.query;

/**
 * Title:        $Source: /cvs/src/net/paymate/web/table/query/RunTimeFormat.java,v $
 * Description:  The canned format of the RunTime output for the status screen<p>
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.12 $
 */

import  net.paymate.web.table.*;
import  net.paymate.*;
import  net.paymate.util.*; // ErrorlogStream
import  org.apache.ecs.*; // element
import  net.paymate.web.color.*;
import  java.util.*;
import net.paymate.lang.ThreadX;
import net.paymate.lang.ThreadReporter;

public class RunTimeFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(RunTimeFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef [(new RunTimeFormatEnum()).numValues()];
  static {
    theHeaders[RunTimeFormatEnum.nameCol]     = new HeaderDef(AlignType.LEFT, "Name");
    theHeaders[RunTimeFormatEnum.statusCol]   = new HeaderDef(AlignType.LEFT, "Status");
  }

  private Vector list = new Vector();

  public RunTimeFormat(ColorScheme colors, String title) {
    super(title, colors, theHeaders, "");
    printThreads();
    rowCount = (new RunTimeFormatRowEnum()).numValues()+list.size();
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

  private String name = "";
  private String status = "";

  public Element column(int col) {
    String str = "";
    switch(col) {
      case RunTimeFormatEnum.nameCol: {
        str = name;
      } break;
      case RunTimeFormatEnum.statusCol: {
        str = status;
      } break;
    }
    return new StringElement(str);
  }
  private int rowCount = 0;
  public boolean hasMoreRows() {
    return currentRow < (rowCount - 1);
  }
  private int currentRow = -1;

  public static String filemore = " - " + net.paymate.Revision.jarSize(); // only need to do this once

  public TableGenRow nextRow() {
    currentRow++;
    switch(currentRow) {
      case RunTimeFormatRowEnum.DFDataRow: {
        name = "Disk";
        TextList msgs = new TextList();
        int c = OS.diskfree("", msgs);
        status = "<PRE>"+msgs.asParagraph(BRLF.toString())+"</PRE>";
      } break;
      default: {
        // these are temporary until we find them another home
        Thread t = (Thread)list.elementAt(currentRow - (rowCount - list.size()));
        name = t.getName();
        status = ((ThreadReporter)t).status();
      } break;
    }
    return this;
  }

  private void printThreads() {
    ThreadGroup tg = ThreadX.RootThread();
    // print the threads
    int threadCount = tg.activeCount();
    Thread [] tlist = new Thread [threadCount * 2]; // plenty of room this way
    int count = tg.enumerate(tlist, true);
    for(int i = 0; i<count; i++) {
      Thread t = tlist[i];
      if(t instanceof ThreadReporter) {
        list.add(t);
      }
    }
  }

}

//$Id: RunTimeFormat.java,v 1.12 2003/10/30 21:05:18 mattm Exp $
