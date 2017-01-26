package net.paymate.web.table.query;

/**
 * <p>Title: $Source: /cvs/src/net/paymate/web/table/query/RecordEditFormat.java,v $</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: PayMate.net</p>
 * @author PayMate.net
 * @version $Revision: 1.9 $
 */

import  net.paymate.web.table.*;
import  net.paymate.*;
import  net.paymate.data.UniqueId;
import  net.paymate.database.TableProfile;
import  net.paymate.database.ColumnProfile;
import  net.paymate.database.DBTypesFiltered;
import  net.paymate.util.*; // ErrorlogStream
import  org.apache.ecs.*; // element
import  org.apache.ecs.html.*; // html elements
import  net.paymate.web.color.*;
import  java.util.*;
import net.paymate.lang.StringX;
import net.paymate.lang.ObjectX;

public class RecordEditFormat extends TableGen implements TableGenRow, RowEnumeration {

  private static final ErrorLogStream dbg = ErrorLogStream.getForClass(RecordEditFormat.class, ErrorLogStream.WARNING);

  protected static final HeaderDef[] theHeaders = new HeaderDef [(new RecordEditFormatEnum()).numValues()];
  static {
    theHeaders[RecordEditFormatEnum.nameCol]    = new HeaderDef(AlignType.LEFT, "Name");
    theHeaders[RecordEditFormatEnum.valueCol]   = new HeaderDef(AlignType.LEFT, "Value");
    theHeaders[RecordEditFormatEnum.nullableCol]= new HeaderDef(AlignType.LEFT, "Nullable");
    theHeaders[RecordEditFormatEnum.defaultCol] = new HeaderDef(AlignType.LEFT, "Default");
    theHeaders[RecordEditFormatEnum.typeCol]    = new HeaderDef(AlignType.LEFT, "Type");
    theHeaders[RecordEditFormatEnum.oldValueCol]= new HeaderDef(AlignType.LEFT, "Original Value");
  }

  public static final String TABLENAME = "tp";
  public static final String ID = "id";

  private UniqueId id;
  private TableProfile table;
  private EasyProperties values;
  private TextList fieldNames;
  // NOTE!  Do not feed this thing a UniqueId [primary key] field!
  // Else, it will fail on the update!
  public RecordEditFormat(ColorScheme colors, String title, EasyProperties values,
                          UniqueId id, TableProfile table) {
    super(title, colors, theHeaders, "");
    this.id = id;
    this.table = table;
    if(values == null) {
      values = new EasyProperties();
    }
    this.values = values;
    // the field names should come from the table (all columns except the primary key):
    TextList fNames = table.fieldNames();
    fNames.remove(fNames.indexOf(table.primaryKey.field.name()));
    fieldNames = fNames.sort();
    rowCount = fieldNames.size();
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

  private String name = "";
  private String value = "";
  private String defawlt = "";
  private String type = "";
  private boolean nullable = false;
  private ColumnProfile cp = null;

  private static final String [ ] trues = {
      "t",
      "true",
      "yes",
      "y",
  };

  private static final int ORIGINALWRAPWIDTH = 50; // +++ get from configs

  public Element column(int col) {
    String str = "";
    switch(col) {
      case RecordEditFormatEnum.nameCol: {
        str = name;
      } break;
      case RecordEditFormatEnum.oldValueCol: {
        // lets wrap this up
        if(value.length() > ORIGINALWRAPWIDTH) {
          TextList tl = new TextList(value, ORIGINALWRAPWIDTH, true);
          str = tl.asParagraph("<BR>\n");
        } else {
          str = value;
        }
      } break;
      case RecordEditFormatEnum.defaultCol: {
        str = defawlt;
      } break;
      case RecordEditFormatEnum.nullableCol: {
        str = nullable ? "" : "NOT NULL!";
      } break;
      case RecordEditFormatEnum.valueCol: {
        // +++ possibly right-align for integer, checkbox for T/F, and left-align for text?
        int typei=-1 ;
        if(cp != null) {
          dbg.WARNING("Column type is " + cp.numericType().Image());
          typei = cp.numericType().Value();
        }
        switch (typei) {
          case DBTypesFiltered.TEXT: {
            return new TextArea(name, 3, 40).addElement(value);
          } // break;
          case DBTypesFiltered.BOOL: {
            boolean checked = StringX.equalAnyStrings(value, trues, true);
            dbg.WARNING("Setting checkbox to " + checked + " where value = " +
                        value + ".");
            return new Input(
                Input.CHECKBOX, name,
                "t"
                /*
                the above "t" means to return "t" when the checkbox is checked;
                 the parameter is completely omitted from the html return parameters
                 when it is unchecked!
                 Note that "the parameter being omitted when it is unchecked" == "false" issue
                 is handled in PayMateDB.setRecordProperties().
                 */).
                setChecked(checked);
          } // break;
          case DBTypesFiltered.CHAR /*1*/: {
            return new Input(Input.TEXT, name, value).setSize(1);
          } // break;
          default: {
            return new Input(Input.TEXT, name, value);
          } // break;
        }
      } // break;
      case RecordEditFormatEnum.typeCol: {
        str = type;
      } break;
    }
    return new StringElement(str);
  }
  private int rowCount = 0;
  public boolean hasMoreRows() {
    return currentRow < (rowCount - 1);
  }
  private int currentRow = -1;

  public TableGenRow nextRow() {
    currentRow++;
    name = fieldNames.itemAt(currentRow);
    value = values.getString(name);
    cp = table.column(name);
    if(cp != null) {
      defawlt = cp.columnDef;
      nullable = cp.nullable();
      type = cp.type();
    } else {
      defawlt = "COULDN'T RESOLVE FIELD!";
      nullable = false;
      type = "UNKNOWN";
    }
    return this;
  }

}
//$Id: RecordEditFormat.java,v 1.9 2004/02/07 22:35:47 mattm Exp $
