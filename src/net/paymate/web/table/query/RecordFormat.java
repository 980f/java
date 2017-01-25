package net.paymate.web.table.query;

import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.web.table.*;
import  net.paymate.database.*;
import  net.paymate.web.color.ColorScheme;
import  net.paymate.util.*;
import  java.sql.*;
import  java.util.*; // timezone
// --- remove?
import  net.paymate.web.page.*; // Acct
import  net.paymate.web.AdminOp;


/**
 * Title:        RecordFormat
 * Description:  The base class for all of the record formatters,
 *               extends the functionality of QueryGen to include a
 *               pre-calculated array of elements for output as a row of cells
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: RecordFormat.java,v 1.7 2001/07/19 01:06:59 mattm Exp $
 */

public abstract class RecordFormat extends QueryGen {
  private static final ErrorLogStream dbg = new ErrorLogStream(RecordFormat.class.getName());

  protected LocalTimeFormat ltf;

  private Element values [] = null;
  private static final Element EMPTY_STRING_ELEMENT = new StringElement("");

  public RecordFormat(ColorScheme colors, String title, Query q, String absoluteURL, int howMany, String sessionid, LocalTimeFormat ltf) {
    super(null, colors, q, null, absoluteURL, howMany, sessionid); // and set what these null's are filling to something in the extended class (will have to be AFTER construction)
    this.title = Safe.TrivialDefault(title, "untitledRecordFormat");
    this.ltf=ltf;
  }

  protected void zeroValues() {
    if(values == null) {
      values = new Element[numColumns()];
    }
    // zero the values
    for(int i = values.length; i-->0;) {
      values[i] = EMPTY_STRING_ELEMENT;
    }
  }

  public int numColumns() {
    return (headers != null) ? headers.length : 0;
  }

  protected void setColumn(int col, Element value) {
    if(col < values.length) {
      values[col] = value;
    } else {
      dbg.ERROR("setColumn(): Index '" + col + "' out of bounds for array 'values[" + values.length + "]'.");
    }
  }

  protected void setColumn(int col, String value) {
    setColumn(col, new StringElement(value));
  }

  public Element column(int col) {
    if(col < values.length) {
      return values[col];
    } else {
      dbg.ERROR("Programmer error.  Column " + col + " does not exist.");
      return EMPTY_STRING_ELEMENT;
    }
  }

  /**
   * This function could be moved to a higher-level class +_+
   */
  protected Element headerLink(AdminOp ao) {
    return headerLink(Acct.key()+"?"+ao.url(), ao.name());
  }

  protected Element headerLink(String url, String name) {
    return /*new StringElement("[").addElement(*/new A(url).addElement((new Font()).setColor(colors.DARK.FG).addElement(/*"[" + */name/* + "]"*/))/*).addElement(new StringElement("]"))*/;
  }

  protected static final Element strikeText(String text, boolean isStrike) {
    StringElement se = new StringElement(text);
    if(isStrike) {
      return new Strike(se);
    } else {
      return se;
    }
  }

  public void close() {
    q.close();
    q = null;
    super.close();
  }

}