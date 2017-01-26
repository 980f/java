/**
 * Title:        ArrayTableGen<p>
 * Description:  Generated an html table from an array<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: EasyCursorTableGen.java,v 1.13 2004/02/24 09:10:09 mattm Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.*;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  java.util.Enumeration;
import  java.util.Properties;
import net.paymate.lang.StringX;

// +_+ eventually make this extend/use ArrayTableGen.java by creating/using EasyProperties.toArray()

public class EasyCursorTableGen extends TableGen {
  // logging facilities
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(EasyCursorTableGen.class);

  protected EasyProperties data = null;
  protected boolean shouldwrap = false;
  protected int wrapwidth = -1;
  protected TextColumn tc = null;

  // wrapwidth <= 0 means no wrap
  public EasyCursorTableGen(String title, ColorScheme colors,
                            EasyProperties data, HeaderDef headers[],
                            String absoluteURL, int wrapwidth) {
    super(title, colors, headers, absoluteURL);
    this.data    = data;
    this.wrapwidth = wrapwidth;
    this.shouldwrap = (wrapwidth > 0);
    if(shouldwrap) {
      tc = new TextColumn(wrapwidth);
    }
  }

  public static final Element output(String title, ColorScheme colors,
                                     EasyProperties data) {
    return output(title, colors, data, -1);
  }

  public static final Element output(String title, ColorScheme colors,
                                     EasyProperties data, int wrapwidth) {
    return output(title, colors, data, null, wrapwidth);
  }

  public static final Element output(String title, ColorScheme colors, EasyProperties data, HeaderDef headers[], int wrapwidth) {
    return output(title, colors, data, headers, null, wrapwidth);
  }

  public static final Element output(String title, ColorScheme colors, EasyProperties data, HeaderDef headers[]) {
    return output(title, colors, data, headers, -1);
  }

  // wrapwidth < 0 means not to
  public static final Element output(String title, ColorScheme colors, EasyProperties data, HeaderDef headers[], String absoluteURL, int wrapwidth) {
    return new EasyCursorTableGen(title, colors, data, headers, absoluteURL, wrapwidth);
  }

  public RowEnumeration rows() {
    return new EasyCursorRowEnumeration(data, shouldwrap, wrapwidth);
  }

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = new HeaderDef[2];
    headers[0] = new HeaderDef(AlignType.LEFT, "Name");
    headers[1] = new HeaderDef(AlignType.LEFT, "Value");
    return headers;
  }

}

class EasyCursorRowEnumeration implements RowEnumeration {
  public EasyProperties rowSet = null;

  private Enumeration propertyNames = null;
  private boolean shouldwrap;
  private int wrapwidth;

  public EasyCursorRowEnumeration(EasyProperties rowSet, boolean shouldwrap, int wrapwidth) {
    this.rowSet = rowSet;
    this.propertyNames = rowSet.sorted();
    this.wrapwidth = wrapwidth;
    this.shouldwrap = shouldwrap;
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
    return (name != null) ? new EasyCursorTableGenRow(name, rowSet.getString(name), shouldwrap, wrapwidth) : null;
  }
}

class EasyCursorTableGenRow implements TableGenRow {
  String rowName;
  String rowValue;
  boolean shouldwrap;
  int wrapwidth;
  TextList ts = new TextList();

  public EasyCursorTableGenRow(String rowName, String rowValue, boolean shouldwrap, int wrapwidth) {
    this.rowName = rowName;
    this.rowValue = rowValue;
    this.shouldwrap = shouldwrap;
    this.wrapwidth = wrapwidth;
  }

  public int numColumns() {
    return 2;
  }

  private String split(String s) {
    if(shouldwrap) {
      ts.clear();
      ts.split(s, wrapwidth, false);
      s = ts.asParagraph("<BR>");
    }
    return s;
  }

  public Element column(int col) {
    // +_+ add better error reporting
    String s = "";
    switch(col) {
      case 0: {
        s = split(StringX.TrivialDefault(rowName, ""));

      } break;
      case 1: {
        s = split(StringX.TrivialDefault(rowValue, ""));
      } break;
    }
    return new StringElement(s);
  }
}
