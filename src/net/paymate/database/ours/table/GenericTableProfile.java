package net.paymate.database.ours.table;

import net.paymate.database.TableProfile;
import java.lang.reflect.Field;
import java.util.Vector;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/GenericTableProfile.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.15 $
 */

import net.paymate.database.*;

public class GenericTableProfile extends TableProfile {

  protected static final PayMateTableEnum APPLIANCETABLE = new PayMateTableEnum(PayMateTableEnum.appliance);
  protected static final PayMateTableEnum APPLNETSTATUSTABLE = new PayMateTableEnum(PayMateTableEnum.applnetstatus);
  protected static final PayMateTableEnum APPLPGMSTATUSTABLE = new PayMateTableEnum(PayMateTableEnum.applpgmstatus);
  protected static final PayMateTableEnum ASSOCIATETABLE = new PayMateTableEnum(PayMateTableEnum.associate);
  protected static final PayMateTableEnum AUTHATTEMPTTABLE = new PayMateTableEnum(PayMateTableEnum.authattempt);
  protected static final PayMateTableEnum AUTHORIZERTABLE = new PayMateTableEnum(PayMateTableEnum.authorizer);
  protected static final PayMateTableEnum BATCHTABLE = new PayMateTableEnum(PayMateTableEnum.batch);
  protected static final PayMateTableEnum CARDTABLE = new PayMateTableEnum(PayMateTableEnum.card);
  protected static final PayMateTableEnum DRAWERTABLE = new PayMateTableEnum(PayMateTableEnum.drawer);
  protected static final PayMateTableEnum ENTERPRISETABLE = new PayMateTableEnum(PayMateTableEnum.enterprise);
  protected static final PayMateTableEnum SERVICECFGTABLE = new PayMateTableEnum(PayMateTableEnum.servicecfg);
  protected static final PayMateTableEnum STORETABLE = new PayMateTableEnum(PayMateTableEnum.store);
  protected static final PayMateTableEnum STOREACCESSTABLE = new PayMateTableEnum(PayMateTableEnum.storeaccess);
  protected static final PayMateTableEnum STOREAUTHTABLE = new PayMateTableEnum(PayMateTableEnum.storeauth);
  protected static final PayMateTableEnum TERMAUTHTABLE = new PayMateTableEnum(PayMateTableEnum.termauth);
  protected static final PayMateTableEnum TERMINALTABLE = new PayMateTableEnum(PayMateTableEnum.terminal);
  protected static final PayMateTableEnum TXNTABLE = new PayMateTableEnum(PayMateTableEnum.txn);

  // constraints
  public static final boolean NOTNULL = false;
  public static final boolean CANNULL = true;
  public static final boolean NOAUTO = false;
  public static final boolean AUTO = true;

  // types
  public static final TableType logType = new TableType(TableType.log);
  public static final TableType cfgType = new TableType(TableType.cfg);

  protected GenericTableProfile(TableInfo ti, TableType type, ColumnProfile [] columns) {
    super(ti, type, columns);
  }

  protected GenericTableProfile(PayMateTableEnum tableenum, TableType type) {
    this(new TableInfo(tableenum.Image()), type, null);
  }

  protected final void setContents(String primaryKeyName, ColumnProfile primaryColumn) {
    // columns
    reflectColumns();
    // primary key
    primaryKey = new PrimaryKeyProfile(primaryKeyName, this, primaryColumn);
    // indexes
    reflectIndexes();
  }

  // try to make the list of columns if there isn't one
  protected void reflectColumns() {
    try {
      if((columns == null) || (columns.length < 1)) {
        Vector v = new Vector();
        Object object = this;
        Class tableClass = object.getClass();
        dbg.VERBOSE("This class is a " + tableClass.getName());
        Field[] field = tableClass.getDeclaredFields(); // gets all, not just public
        Class theColumnClass = ColumnProfile.class;
        for (int i = field.length; i-- > 0; ) {
          Field f = field[i];
          Class thisFieldsClass = f.getType();
          boolean isAssignable = thisFieldsClass.isAssignableFrom(
              theColumnClass);
          dbg.VERBOSE("Member class \"" + thisFieldsClass.getName() +
                      "\" for field \"" + f.getName() +
                      "\" is " + (isAssignable ? "" : "NOT ") +
                      " assignable from the " +
                      theColumnClass.getName() + " class.");
          if (isAssignable) {
            Object o = f.get(object);
            if(o == null) {
              dbg.ERROR("o is null!");
            }
            v.add(o);
          }
        }
        columns = new ColumnProfile[v.size()];
        for (int i = columns.length; i-- > 0; ) {
          columns[i] = (ColumnProfile) v.elementAt(i);
        }
      }
    } catch (Exception ex) {
      System.out.println("Exception in GenericTableProfile.setColumns():"+ex);
      ex.printStackTrace();
    }
  }

  // try to make a list of columns if there isn't one
  protected void reflectIndexes() {
    try {
      if((indexes == null) || (indexes.length < 1)) {
        Vector v = new Vector();
        Object object = this;
        Class c = object.getClass();
        Field[ ] field = c.getDeclaredFields(); // gets all, not just public
        Class theIndexClass = IndexProfile.class;
        for(int i = field.length; i-- > 0; ) {
          Field f = field[i];
          Class thisFieldsClass = f.getType();
          boolean isAssignable = theIndexClass.isAssignableFrom(thisFieldsClass);
          dbg.VERBOSE("Member class \"" + thisFieldsClass.getName() +
                      "\" for field \"" + f.getName() +
                      "\" is " + (isAssignable ? "" : "NOT ") +
                      " assignable from the " +
                      theIndexClass.getName() + " class.");
          if(isAssignable) {
            Object o = f.get(object);
            if(o == null) {
              dbg.ERROR("o is null!");
            }
            v.add(o);
          }
        }
        indexes = new IndexProfile [ v.size() ];
        for(int i = indexes.length; i-->0;) {
          indexes[i] = (IndexProfile)v.elementAt(i);
        }
      }
    } catch (Exception ex) {
      System.out.println("Exception in GenericTableProfile.setIndexes():"+ex);
      ex.printStackTrace();
    }
  }

  protected final ColumnProfile createColumn(String name,
                                           int dbtype, boolean nullable,
                                           boolean autoIncrement,
                                           String columnDef) {
    return ColumnProfile.create(this, name, dbtype,
                  /*size*/0, nullable, /*displayName*/null,
                  autoIncrement, columnDef);
  }

}