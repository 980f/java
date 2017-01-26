package net.paymate.web.table;
import  net.paymate.database.*;
import  net.paymate.web.color.*;
import  net.paymate.web.UserSession;
import  java.sql.*;
import  org.apache.ecs.*;
import  org.apache.ecs.html.*;
import  net.paymate.util.*;
import  net.paymate.web.table.query.RecordEditFormat;
import net.paymate.web.page.*;
import net.paymate.web.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: ProfileDBTableGen.java,v 1.15 2003/10/31 05:52:20 mattm Exp $
 */

public class ProfileDBTableGen extends TableGen implements RowEnumeration, TableGenRow {
  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(ProfileDBTableGen.class, ErrorLogStream.WARNING);

  private TableInfoList list = null;
  private int currentTable = -1;
  private String baseURL = "";

  /**
   * @param tableName - if null means to profile ALL tables
   */
  public ProfileDBTableGen(TableInfoList list, ColorScheme colors, String baseURL) {
    super("", colors, null, null);
    this.title = "Tables in database:";
    this.list = list;
    this.baseURL = baseURL;
    headers = COUNTHEADERS;
  }

  public static final Element output(TableInfoList list, ColorScheme colors, String baseURL) {
    return new ProfileDBTableGen(list, colors, baseURL);
  }

  private static final HeaderDef NAMEHEADER   = new HeaderDef(AlignType.LEFT, "Table Name / Contents");
  private static final HeaderDef EDITHEADER   = new HeaderDef(AlignType.LEFT, "Edit Record #");

  private HeaderDef COUNTHEADERS[] = {
    NAMEHEADER,
    EDITHEADER,
  };

  protected HeaderDef[] fabricateHeaders() {
    return headers;
  }

  public RowEnumeration rows() {
    return this;
  }

  public boolean hasMoreRows() {
    // doesn't matter; prolly won't ever get called
    return (currentTable < (list.size()-1));
  }



  private Element nameField    = emptyElement;
  private Element editField    = emptyElement;
  public TableGenRow nextRow() {
    TableGenRow tgr = null;
    try {
      dbg.Enter("nextRow");
      nameField = emptyElement;
      editField = emptyElement;
      TableInfo ti = list.itemAt(++currentTable);
      if(ti != null) {
        String table = ti.name();
        nameField   = new A(baseURL + "&op=profile" + "&table=" + table,  table);
        Form f = PayMatePage.NewPostForm("./" + Acct.key() + "?" + AdminOp.editRecordAdminOp.url());
        Table tedit = new Table();
        TD td1 = new TD().addElement(new Input(Input.TEXT, RecordEditFormat.ID, ""));
        TD td2 = new TD().addElement(new Input().setType(Input.SUBMIT).setValue("Edit"));
        TR tr1 = new TR().addElement(td1).addElement(td2);
        tedit.addElement(tr1);
        f.addElement(tedit).
          addElement(new Input(Input.HIDDEN, RecordEditFormat.TABLENAME, table));
        editField   = f;
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
        el = editField;
      } break;
    }
    return el;
  }

}

