package net.paymate.web.table;
import  net.paymate.database.*;
import  net.paymate.web.color.*;
import  java.sql.*;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.util.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ProfileDBTableGen.java,v 1.4 2001/10/11 04:34:03 mattm Exp $
 */

public class ProfileDBTableGen extends TableGen implements RowEnumeration, TableGenRow {
  private static final ErrorLogStream dbg = new ErrorLogStream(ProfileDBTableGen.class.getName(), ErrorLogStream.WARNING);

  private TableInfoList list = null;
  private int currentTable = -1;
  private String baseURL = "";

  /**
   * @param tableName - if null means to profile ALL tables
   */
  public ProfileDBTableGen(TableInfoList list, ColorScheme colors, String baseURL, String sessionid) {
    super("", colors, myheaders, null, -1, sessionid);
    this.title = "Tables in database:";
    this.list = list;
    this.baseURL = baseURL;
  }

  public static final Element output(TableInfoList list, ColorScheme colors, String baseURL, String sessionid) {
    return new ProfileDBTableGen(list, colors, baseURL, sessionid);
  }

  private static final HeaderDef myheaders[] = {
    new HeaderDef(AlignType.LEFT, "Table Name / Contents"),
    new HeaderDef(AlignType.LEFT, "Type [Catalog.Schema] / Profile"),
    new HeaderDef(AlignType.LEFT, "Remarks"),
  };

  protected HeaderDef[] fabricateHeaders() {
    return myheaders;
  }

  public RowEnumeration rows() {
    return this;
  }

  public boolean hasMoreRows() {
    // doesn't matter; prolly won't ever get called
    return (currentTable < (list.size()-1));
  }



  private Element nameField = emptyElement;
  private Element typeField = emptyElement;
  private Element remarksField = emptyElement;
  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      nameField = emptyElement;
      typeField = emptyElement;
      remarksField = emptyElement;
      TableInfo ti = list.itemAt(++currentTable);
      if(ti != null) {
        String table = ti.name();
        nameField = new A(baseURL + "&op=disptable" + "&table=" + table,  table);
        typeField = new A(baseURL + "&op=profile" + "&table=" + table,  ti.type() + " [" + ti.catalog() + "." + ti.schema() + "]");
        remarksField = new StringElement(ti.remark());
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
        el = typeField;
      } break;
      case 2: {
        el = remarksField;
      } break;
    }
    return el;
  }

  public void close() {
    super.close();
  }
}

