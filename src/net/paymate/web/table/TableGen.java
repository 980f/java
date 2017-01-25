/**
 * Title:        HTMLTable<p>
 * Description:  HTML Table generator<p>
 * Copyright:    2000, PayMate.net<p>
 * Company:      PayMate.net<p>
 * @author       PayMate.net
 * @version      $Id: TableGen.java,v 1.36 2001/10/24 11:01:17 mattm Exp $
 */

package net.paymate.web.table;
import  net.paymate.util.*; // safe, monitor, errologstream
import  net.paymate.web.color.*;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  java.io.*;
import  java.util.*;
// for the db portion:
import  java.sql.*;
// for the constants
import  net.paymate.web.page.PayMatePage;

public abstract class TableGen extends GenericDBTableElement {
  // logging facilities
  private static final ErrorLogStream dbg=new ErrorLogStream(TableGen.class.getName(), ErrorLogStream.WARNING);
  // constants ...
  protected static final Element LF   = PayMatePage.LF; // makes reading the generated files easier
  protected static final Element br   = PayMatePage.br; // +_+ put these in a class designed just for this stuff, not in PaymatePage or here
  protected static final Element BRLF = PayMatePage.BRLF;
  // variables ...
  public    ColorScheme colors    = ColorScheme.MONOCHROME; // default
  // these can only be added in the constructor ...
  protected String      title     = null; // +++ aren't dealing with yet !!!!  DO ME !!!!
  protected HeaderDef   headers[] = null; // !!! (headers.length == data[i].length) !!!
  protected RowEnumeration rowEnum = null;
  protected String absoluteURL = null;
  // this is for keeping track of queries that have more records than can be displayed
  // +_+ develop a separate class for this, which extends the HashTable.  Then, instantiate one instance of that class?
  public static final Hashtable queries = new Hashtable(); // status page needs to get to these
  public static final TableGen getFromKey(String key) {
    return (TableGen) queries.remove(key);
  }
  private String sessionid = "";
  public static final void cleanup(String sessionid) {
    for(Enumeration enum = queries.elements(); enum.hasMoreElements();) {
      try {
        Object o = enum.nextElement();
        if((o != null) && (o instanceof TableGen)) { // should always be
          TableGen tg = (TableGen)o;
          if(tg.sessionid.equals(sessionid)) {
            tg.close();
          }
        }
      } catch (Exception e) {
        dbg.Caught(e);
        enum = queries.elements(); // try again
      }
    }
  }

  public static final TextList getUnclosedStatements() {
    TextList tl = new TextList();
    for(Enumeration enum = queries.elements(); enum.hasMoreElements();) {
      try {
        Object o = enum.nextElement();
        if((o != null) && (o instanceof TableGen)) { // should always be
          TableGen tg = (TableGen)o;
          tl.add(tg.sessionid+"/"+tg.title);
        }
      } catch (Exception e) {
        dbg.Caught(e);
        enum = queries.elements(); // try again
      }
    }
    return tl;
  }

  public TableGen(String title, ColorScheme colors, HeaderDef headers[], String absoluteURL, int howMany, String sessionid) {
    super(null);
    this.title   = title;
    this.colors  = colors;
    this.headers = headers;
    this.maxRows = howMany;
    this.absoluteURL = absoluteURL;
    this.sessionid = sessionid;
  }

  protected int maxRows = -1;
  private boolean isMore = false;
  public boolean hasMore() {
    return isMore;
  }

  private int totalRows = 0;
  public int rowsYet() {
    return totalRows;
  }

  private String key = null;
  public String key() {
    return key;
  }

  private int page = 0;
  public String page() {
    return ""+page;
  }

  public void close() {
    queries.remove(key);
  }

  private static long keyIncr = System.currentTimeMillis(); // this should make these very unique (+_+ find a different non-static method for doing this?)
  private static final Monitor keyMon = new Monitor("TableGenKeyGenerator"); // monitor required since a long is not atomic
  private static final long genKey() {
    long ret = -1;
    try{
      dbg.Enter("genKey");
      keyMon.getMonitor();
      ret = keyIncr++;
    } catch (Exception e) {
      dbg.Caught(e);
    } finally {
      keyMon.freeMonitor();
      dbg.Exit();
      return ret;
    }
  }

  public void output(PrintWriter out) {
    ElementContainer result = null;
    Table t = null;
    TBody body = null;
    key = BaseConverter.itoa(TableGen.genKey(), 16);
    try {
      dbg.Enter("generate");
      result = new ElementContainer();
      // header
      t = new Table().setWidth(PayMatePage.PERCENT100).setBorder("0").setCellSpacing(0).setCellPadding(0);
      // add title
      t.addElement((new Caption()).addElement(new Center(new H3(title))));
      t.addElement(new DBTableHeaderElement(this));
      result.addElement(t);
      // fill the body
      body = new TBody();
      body.addElement(new DBTableBodyElement(this)).addElement(LF);
      t.addElement(body).addElement(LF);
      // footer
      body.addElement(new DBTableFooterElement(this));
      // return the table
//      long starttime = Safe.utcNow();
      result.output(out);
//      long endtime = Safe.utcNow();
//      out.println("tgt:" + (endtime - starttime) + "ms");
      out.flush();
      if(!hasMore()) {
        close();
      } else {
        queries.put(key, this);
      }
    } catch (Exception t5) {
      dbg.Caught("Exception generating table.", t5);
    } finally {
      dbg.Exit();
    }
  }

  /*package*/ void fillAllFlesh(PrintWriter out) {
    //Only generates maxRows rows.
    //Sets the isMore flag so more can be displayed later.
    int rowCount = 0;
    if(rowEnum == null) {
      rowEnum = rows();
    }
    TableGenRow row = null;
    while((isMore = rowEnum.hasMoreRows()) && ((maxRows < 0) || (rowCount<maxRows))) {
      row = rowEnum.nextRow();
      if(row == null) {
        isMore = false;
        dbg.ERROR("rowEnum.hasMoreRows() reported more rows, but nextRow() returned null");
        break;
      }
      rowCount++;
      totalRows++;
      Element te = generateDataRow(row, rowCount);
      dbg.VERBOSE("Generating row # " + rowCount);
      te.output(out);
      LF.output(out);
    }
    System.gc(); // hope this helps
    out.flush();
  }

  /*package*/ void fillHeader(PrintWriter out) {
    if(headers == null) { // fabricate the header definitions
      headers = fabricateHeaders();
      if(headers == null) {
        headers = new HeaderDef[0];
      }
    }
    // make the header
    Element e = (new THead().addElement(generateHeaderRow())).addElement(LF);
    e.output(out);
    out.flush();
  }

  /*package*/ void fillFooter(PrintWriter out) {
    page++;
    try {
      for(int feet = 0; feet < _footerRows(); feet++) {
        generateFooterRow(feet).output(out);
        LF.output(out);
      }
    } catch (Exception t4) {
      dbg.Caught("Exception generating table footer.",t4);
    } finally {
      out.flush();
    }
  }

  private Element generateDataRow(TableGenRow row, int count) {
    int numCols = (headers != null) ? headers.length : 0;
    boolean light = (count % 2 == 0);
    ColorSet color =  light ? colors.LIGHT : colors.MEDIUM;
    TR trBody = new TR().setBgColor(color.BG);
    for(int col = 0; col < numCols; col++) {
      try {
        Element cell = row.column(col);
        Font f = new Font();
        if(!light) {
          f.setColor(color.FG);
        }
        f.addElement(new StringElement(Entities.NBSP)).addElement(cell);
        trBody.addElement(new TD(f).setAlign(headers[col].colAlign).addElement(LF));
      } catch (Exception t) {
        dbg.Caught("generateDataRow: Exception generating column " + col,t);
      }
    }
    return trBody;
  }

  // shared stuff
  private Element generateHeaderRow() {
    // make the header
    TR trHead = new TR().setBgColor(colors.DARK.BG);
    int col = -1;
    try {
      for(col = 0; col< headers.length;col++) {
/*
        trHead.addElement(new TH(new Font().setColor(colors.DARK.FG)
                                           .addElement(Entities.NBSP)
                                           .addElement(headers[col].title)
                                           .addElement(Entities.NBSP))
                              .setAlign(headers[col].colAlign).addElement(LF));
*/
        Font f = new Font().setColor(colors.DARK.FG);
        if(!headers[col].colAlign.equalsIgnoreCase(AlignType.LEFT)) {
          f.addElement(Entities.NBSP);
        }
        f.addElement(headers[col].title);
        if(!headers[col].colAlign.equalsIgnoreCase(AlignType.RIGHT)) {
          f.addElement(Entities.NBSP);
        }
        trHead.addElement(new TH(f).setAlign(headers[col].colAlign).addElement(LF));
      }
    } catch (Exception t) {
      dbg.Caught("Exception generating header row at column " + col, t);
    }
    return trHead;
  }

  private Element generateFooterRow(int row) {
    // make the footer
    TR trFoot = new TR().setBgColor(colors.DARK.BG);
    int col = -1;
    try {
      for(col = 0; col< headers.length;col++) {
        Font f = new Font().setColor(colors.DARK.FG);
        if(!headers[col].colAlign.equalsIgnoreCase(AlignType.LEFT)) {
          f.addElement(Entities.NBSP);
        }
        f.addElement(_footer(row, col));
        if(!headers[col].colAlign.equalsIgnoreCase(AlignType.RIGHT)) {
          f.addElement(Entities.NBSP);
        }
        trFoot.addElement(new TH(f).setAlign(headers[col].colAlign).addElement(LF));
      }
    } catch (Exception t) {
      dbg.Caught("Exception generating footer row=" + row + ", col=" + col, t);
    }
    return trFoot;
  }

  // !!! overload when extending
  protected abstract RowEnumeration rows();
  protected abstract HeaderDef[] fabricateHeaders();
  protected int footerRows() {
    return FOOTERSNOTINITED; // means not inited; use defaults
  }
  protected Element footer(int row, int col) {
    return emptyFooterCell;
  }

  private int FOOTERSNOTINITED = -1;
  private int _footerRows() {
    return (footersNotInited() ? 1 : footerRows()) + (hasMore() ? 1 : 0) ;
  }
  private boolean footersNotInited() {
    return (footerRows() == FOOTERSNOTINITED);
  }

  protected static final StringElement emptyElement = new StringElement(" ");
  private static final Element emptyFooterCell = emptyElement;
  private static final Element errFooterCell = new StringElement(" !ERROR! ");
  private static final String moreStr = "next ->";

  private Element _footer(int row, int col) {
    Element footerCell = errFooterCell;
    if((row < 0) || (row >= _footerRows())) {
      dbg.ERROR("_footer(): Requested invalid row [" + row + "].");
    } else {
      if((col < 0) || (col >= headers.length)) {
        dbg.ERROR("_footer(): Requested invalid col [" + col + "].");
      } else {
        footerCell = emptyFooterCell;
        // first the count; don't show it if footers are defined
        int theCount = footersNotInited() ? 0 : FOOTERSNOTINITED;
        // then the next/previous/pick
        int theNext = footersNotInited() ? theCount + 1 : footerRows();
        if(row == theCount) {
          switch(col) {
            case 0: {
              footerCell = new StringElement((((maxRows < 0) || !hasMore())? "Count:" : "SubCount:") + " " + rowsYet());
            } break;
          }
        } else if(row == theNext) {
          if((col == (headers.length-1)) || col == 0) {
            // +++ this is where we put the "next->" stuff (and eventually the "back" stuff and the "select which one" stuff)
            footerCell = new A(absoluteURL + "&k="+key()+"&pg="+page()).addElement((new Font()).setColor(colors.DARK.FG).addElement(moreStr));
          }
        } else {
          footerCell = footer(row, col);
        }
      }
    }
    return footerCell;
  }

}

class DBTableBodyElement extends GenericDBTableElement {
  public DBTableBodyElement(TableGen tbg) {
    super(tbg);
  }

  public void output(PrintWriter out) {
    tbg.fillAllFlesh(out);
  }
}

class DBTableHeaderElement extends GenericDBTableElement {
  public DBTableHeaderElement(TableGen tbg) {
    super(tbg);
  }

  public void output(PrintWriter out) {
    tbg.fillHeader(out);
  }
}

class DBTableFooterElement extends GenericDBTableElement {
  public DBTableFooterElement(TableGen tbg) {
    super(tbg);
  }

  public void output(PrintWriter out) {
    tbg.fillFooter(out);
  }
}

class GenericDBTableElement extends GenericElement {
  TableGen tbg = null;

  public GenericDBTableElement(TableGen tbg) {
    this.tbg = tbg;
  }

  public void output(OutputStream out) {
    PrintWriter pw = new PrintWriter(out);
    output(pw);
  }

  // crap
  public Element addElementToRegistry(Element element) {
    return this;
  };
  public Element addElementToRegistry(String element){
    return this;
  };
  public Element removeElementFromRegistry(Element element){
    return this;
  };
  public Element removeElementFromRegistry(String element){
    return this;
  };
  public boolean registryHasElement(Element element){
    return false;
  };
  public boolean registryHasElement(String element){
    return false;
  };
}

