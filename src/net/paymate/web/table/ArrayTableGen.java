/**
 * Title:        ArrayTableGen<p>
 * Description:  Generated an html table from an array<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: ArrayTableGen.java,v 1.13 2004/03/13 01:29:32 mattm Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.ErrorLogStream;
import  net.paymate.web.color.*;
import  org.apache.ecs.*;

public class ArrayTableGen extends TableGen {
  // logging facilities
  private static final ErrorLogStream dbg=ErrorLogStream.getForClass(ArrayTableGen.class);

  protected String      data [/*row*/][/*col*/] = null;

  public ArrayTableGen(String title, ColorScheme colors, String data[][], HeaderDef headers[], String absoluteURL) {
    super(title, colors, headers, absoluteURL);
    this.data    = data;
  }

  public static final Element output(String title, ColorScheme colors, String data[][], HeaderDef headers[]) {
    return output(title, colors, data, headers, null);
  }

  public static final Element output(String title, ColorScheme colors, String data[][], HeaderDef headers[], String absoluteURL) {
    return new ArrayTableGen(title, colors, data, headers, absoluteURL);
  }

  public RowEnumeration rows() {
    return new ArrayRowEnumeration(data);
  }

  public HeaderDef[] fabricateHeaders() {
    HeaderDef headers[] = null;
    if(data.length > 0) {
      headers = new HeaderDef[data[0].length];
      for(int i = headers.length; i-->0; ) {
        headers[i] = new HeaderDef(AlignType.LEFT, Integer.toString(i));
      }
    }
    return headers;
  }

}

class ArrayRowEnumeration implements RowEnumeration {
  public String [][] rowSet = null;

  private int curRow = -1;

  public ArrayRowEnumeration(String [][] rowSet) {
    this.rowSet = rowSet;
  }

  public boolean hasMoreRows() {
    return (curRow+1) < rowSet.length;
  }

  public TableGenRow nextRow() {
    // +_+ maybe should let it throw?
    // +_+ add better error reporting
    return (++curRow < rowSet.length) ? new ArrayTableGenRow(rowSet[curRow]) : null;
  }
}

class ArrayTableGenRow implements TableGenRow {
  String [] row = null;

  public ArrayTableGenRow(String [] row) {
    this.row = row;
  }

  public int numColumns() {
    return row.length;
  }

  public Element column(int col) {
    // +++ add better error reporting
    return new StringElement((row != null) && (col < row.length) ? row[col] : "");
  }
}
