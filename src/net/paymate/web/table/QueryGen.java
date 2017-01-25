package net.paymate.web.table;

/**
 * Title:        $Source $<p>
 * Description:  generates an html table from a net.paymate.database.Query <p>
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: QueryGen.java,v 1.5 2001/09/07 23:33:08 mattm Exp $
 */

import  net.paymate.util.ErrorLogStream;
import  net.paymate.database.*;
import  net.paymate.web.color.*;
import  java.sql.*;
import  org.apache.ecs.*;

public abstract class QueryGen extends TableGen implements TableGenRow, RowEnumeration {
  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(QueryGen.class.getName());

  protected Query q = null;

  public QueryGen(String title, ColorScheme colors, Query q, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(title, colors, headers, absoluteURL, howMany, sessionid);
    this.maxRows = howMany;
    this.q = q;
  }

  public RowEnumeration rows() {
    return this;
  }

  // overload these to extend the class ...
  public abstract int numColumns();
  public abstract Element column(int col);

  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    if(q!=null) {
      if(q.next()) {
        tgr = this;
      } else {
dbg.ERROR("nextRow(): q has no more records!");
      }
    } else {
dbg.ERROR("nextRow(): q is NULL!");
    }
    return tgr;
  }

  public boolean hasMoreRows() {
    boolean ret = false;
    if(q!=null) {
      ret = q.hasMoreRows();
    } else {
dbg.ERROR("nextRow(): q is NULL!");
    }
    return ret;
  }

  /**
   * OVer load this!
   */
  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = null;
    headers = new HeaderDef[numColumns()];
    for(int col = headers.length; col-->0; ) {
      headers[col] = new HeaderDef(AlignType.LEFT, new StringElement("[unspecified]"));
    }
    return headers;
  }

}
