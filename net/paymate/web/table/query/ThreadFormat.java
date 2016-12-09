/**
* Title:        ThreadFormat<p>
* Description:  The canned query for the Thread table on the status screen<p>
* Copyright:    2000, PayMate.net<p>
* Company:      PayMate.net<p>
* @author       PayMate.net
* @version      $Id: ThreadFormat.java,v 1.6 2001/11/17 20:06:38 mattm Exp $
*/

package net.paymate.web.table.query;
import  net.paymate.web.table.*;
import  net.paymate.util.*; // ErrorlogStream
import  net.paymate.web.page.*; // Acct
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // various html elements
import  net.paymate.web.color.*;
import  java.util.*;

public class ThreadFormat extends TableGen implements TableGenRow, RowEnumeration {
  private static final ErrorLogStream dbg = new ErrorLogStream(ThreadFormat.class.getName(), ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef [(new ThreadFormatEnum()).numValues()];
  static {
    theHeaders[ThreadFormatEnum.nameCol]     = new HeaderDef(AlignType.LEFT  , "Name");
    theHeaders[ThreadFormatEnum.priorityCol] = new HeaderDef(AlignType.RIGHT , "Priority");
    theHeaders[ThreadFormatEnum.daemonCol]   = new HeaderDef(AlignType.LEFT  , "Type");
    theHeaders[ThreadFormatEnum.thisCol]     = new HeaderDef(AlignType.CENTER, "*");
  }

  PathedThreadList list = new PathedThreadList();

  public ThreadFormat(ColorScheme colors, String title) {
    super(title, colors, theHeaders, "", -1, "");
    printGroup(null, null, list);
    // +_+ sort alphabetically
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

  private static final void printGroup(ThreadGroup tg, StringStack stack, PathedThreadList ptlist) {
    if(tg == null) {
      tg = ThreadX.RootThread();
    }
    if(stack == null) {
      stack = new StringStack();
    }
    stack.push(tg.getName());
    // print the threads
    int threadCount = tg.activeCount();
    Thread [] list = new Thread [threadCount * 2]; // plenty of room this way
    int count = tg.enumerate(list);
    for(int i = 0; i<count; i++) {
      Thread t = list[i];
      if(t.getThreadGroup() == tg) {
        stack.push(t.getName());
        ptlist.add(new PathedThread(stack.toString(), t));
        stack.pop();
      }
    }
    // get the groups
    int groupCount = tg.activeGroupCount();
    ThreadGroup [] glist = new ThreadGroup [groupCount * 2]; // plenty of room this way
    groupCount = tg.enumerate(glist);
    for(int i = 0; i<groupCount; i++) {
      ThreadGroup g = glist[i];
      if(g.getParent() == tg) {
        printGroup(g, stack, ptlist);
      }
    }
    stack.pop();
  }

  public Element column(int col) {
    PathedThread t = list.itemAt(currentRow);
    String str = "";
    switch(col) {
      case ThreadFormatEnum.nameCol: {
        str = t.path;
      } break;
      case ThreadFormatEnum.priorityCol: {
        ThreadGroup tg2 = t.thread.getThreadGroup();
        str = ""+t.thread.getPriority()+"/"+((tg2 != null) ? tg2.getMaxPriority() : -1);
      } break;
      case ThreadFormatEnum.daemonCol: {
        str = (t.thread.isDaemon() ? "Daemon" : "User");
      } break;
      case ThreadFormatEnum.thisCol: {
        str = (t.thread == Thread.currentThread() ? "*" : " ");
      } break;
    }
    return new StringElement(str);
  }
  public boolean hasMoreRows() {
    return currentRow < (list.size() - 1);
  }
  private int currentRow = -1;

  public TableGenRow nextRow() {
    currentRow++;
    return this;
  }
}

class PathedThread {
  public String path = "";
  public Thread thread = null;
  public PathedThread(String path, Thread thread) {
    this.path = path;
    this.thread = thread;
  }
}

class PathedThreadList extends Vector {
  public PathedThread itemAt(int index) {
    return (index < size()) ? (PathedThread)this.elementAt(index) : (PathedThread)null;
  }
}

//$Id: ThreadFormat.java,v 1.6 2001/11/17 20:06:38 mattm Exp $
