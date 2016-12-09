package net.paymate.database.ours.table;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DrawerTable.java,v 1.2 2001/11/17 06:16:59 mattm Exp $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class DrawerTable extends TableProfile implements DBConstants {

  private static final String DRAWER = "DRAWER";

  public static final ColumnProfile drawerid       =ColumnProfile.create(DRAWER, DRAWERID      ,DBTypesFiltered.INTEGER, 8,ColumnProfile.NOTNULL  ,"DrawerID"     ,ColumnProfile.AUTO);
  public static final ColumnProfile terminalid     =ColumnProfile.create(DRAWER, TERMINALID    ,DBTypesFiltered.CHAR   ,32,ColumnProfile.NOTNULL  ,"Terminal ID"  ,ColumnProfile.NOAUTO); // +++ needs unique index in terminal table // replace with TERMID that is integer ???
  public static final ColumnProfile associateid    =ColumnProfile.create(DRAWER, ASSOCIATEID   ,DBTypesFiltered.INTEGER, 4,ColumnProfile.ALLOWNULL,"Associate ID" ,ColumnProfile.NOAUTO); // +++ needs unique index in associate table // make not null later ++
  public static final ColumnProfile enterpriseid   =ColumnProfile.create(DRAWER, ENTERPRISEID  ,DBTypesFiltered.INTEGER, 4,ColumnProfile.ALLOWNULL,"Enterprise ID",ColumnProfile.NOAUTO); // +++ needs unique index enterprise table // make it NOTNULL later
  public static final ColumnProfile storeid        =ColumnProfile.create(DRAWER, STOREID       ,DBTypesFiltered.INTEGER, 4,ColumnProfile.NOTNULL  ,"Store ID#"    ,ColumnProfile.NOAUTO); // +++ needs unique index in the store table
  public static final ColumnProfile transtarttime  =ColumnProfile.create(DRAWER, TRANSTARTTIME ,DBTypesFiltered.CHAR   ,14,ColumnProfile.NOTNULL  ,"Txn Time"     ,ColumnProfile.NOAUTO);


  private static final ColumnProfile [] staticColumns = {
    drawerid,
    transtarttime,
    terminalid,
    associateid,
    enterpriseid,
    storeid,
  };

  public DrawerTable() {
    super(new TableInfo(DRAWER), staticColumns);
  }

}

