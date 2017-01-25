package net.paymate.web.table;
import  net.paymate.database.*;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  net.paymate.util.*;
import  java.util.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: MonitorTableGen.java,v 1.5 2001/10/11 04:34:03 mattm Exp $
 */

public class MonitorTableGen extends TableGen implements RowEnumeration, TableGenRow {
  private static final ErrorLogStream dbg = new ErrorLogStream(MonitorTableGen.class.getName(), ErrorLogStream.WARNING);

  private static final HeaderDef headers [] = {
    new HeaderDef(AlignType.LEFT, "Name"),
    new HeaderDef(AlignType.LEFT, "Owner"),
    new HeaderDef(AlignType.LEFT, "Waiting"),
  };

  private Vector monitors = null;
  private int currentMonitor = -1;

  public MonitorTableGen(String title, ColorScheme color, Vector monitors, String sessionid) {
    super(title, color, headers, null, -1, sessionid);
    this.monitors = monitors;
  }

  public static final Element output(String title, ColorScheme color, Vector monitors, String sessionid) {
    return new MonitorTableGen(title, color, monitors, sessionid);
  }

  public RowEnumeration rows() {
    return this;
  }
  public HeaderDef[] fabricateHeaders() {
    return headers;
  }
  public boolean hasMoreRows() {
    return (currentMonitor < (monitors.size()-1));
  }

  private StringElement nameField = emptyElement;
  private StringElement ownerField = emptyElement;
  private StringElement waitingField = emptyElement;
  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      nameField = emptyElement;
      ownerField = emptyElement;
      waitingField = emptyElement;
      Monitor m = (Monitor) monitors.elementAt(++currentMonitor);
      if(m != null) {
        nameField = new StringElement(m.name);
        Thread owner = m.getMonitorOwner();
        ownerField = new StringElement((owner == null) ? "" : owner.getName());
        waitingField = new StringElement(m.dump().asParagraph("<BR>"+Entities.NBSP));
        tgr = this;
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
    switch(col) {
      case 0: {
        el = nameField;
      } break;
      case 1: {
        el = ownerField;
      } break;
      case 2: {
        el = waitingField;
      } break;
    }
    return el;
  }

  public void close() {
    super.close();
  }
}