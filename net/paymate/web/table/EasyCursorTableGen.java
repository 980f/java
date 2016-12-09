/**
 * Title:        ArrayTableGen<p>
 * Description:  Generated an html table from an array<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: EasyCursorTableGen.java,v 1.5 2001/10/15 22:41:38 andyh Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.*;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  java.util.Enumeration;
import  java.util.Properties;

// +_+ eventually make this extend/use ArrayTableGen.java by creating/using EasyCursor.toArray()

public class EasyCursorTableGen extends TableGen {
  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(EasyCursorTableGen.class.getName());

  protected EasyCursor data = null;

  public EasyCursorTableGen(String title, ColorScheme colors, EasyCursor data, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(title, colors, headers, absoluteURL, howMany, sessionid);
    this.data    = data;
  }

  public static final Element output(String title, ColorScheme colors, EasyCursor data, String sessionid) {
    return output(title, colors, data, null, sessionid);
  }

  public static final Element output(String title, ColorScheme colors, EasyCursor data, HeaderDef headers[], String sessionid) {
    return output(title, colors, data, headers, null, -1, sessionid);
  }

  public static final Element output(String title, ColorScheme colors, EasyCursor data, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    return new EasyCursorTableGen(title, colors, data, headers, absoluteURL, howMany, sessionid);
  }

  public RowEnumeration rows() {
    return new EasyCursorRowEnumeration(data);
  }

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = new HeaderDef[2];
    headers[0] = new HeaderDef(AlignType.LEFT, "Name");
    headers[1] = new HeaderDef(AlignType.LEFT, "Value");
    return headers;
  }

  public void close() {
    super.close();
  }

}

class EasyCursorRowEnumeration implements RowEnumeration {
  public EasyCursor rowSet = null;

  private Enumeration propertyNames = null;

  public EasyCursorRowEnumeration(EasyCursor rowSet) {
    this.rowSet = rowSet;
    this.propertyNames = rowSet.sorted();
  }

  public boolean hasMoreRows() {
    return propertyNames.hasMoreElements();
  }

  public TableGenRow nextRow() {
    String name = null;
    try {
      name = (String)propertyNames.nextElement();
    } catch (Exception t) {
      //dbg.Message("nextRow: No more elements in enumeration");
    }
    return (name != null) ? new EasyCursorTableGenRow(name, rowSet.getString(name)) : null;
  }
}

class EasyCursorTableGenRow implements TableGenRow {
  String rowName;
  String rowValue;

  public EasyCursorTableGenRow(String rowName, String rowValue) {
    this.rowName = rowName;
    this.rowValue = rowValue;
  }

  public int numColumns() {
    return 2;
  }

  public Element column(int col) {
    // +_+ add better error reporting
    String s = "";
    switch(col) {
      case 0: {
        s = Safe.TrivialDefault(rowName, "");
      } break;
      case 1: {
        s = Safe.TrivialDefault(rowValue, "");
      } break;
    }
    return new StringElement(s);
  }
}
