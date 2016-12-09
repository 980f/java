package net.paymate.database.ours.table;

import net.paymate.database.*;
import net.paymate.database.ours.*;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TerminalTable.java,v 1.8 2001/11/17 06:16:59 mattm Exp $
 */

public class TerminalTable extends TableProfile implements DBConstants {

  private static final String TERMINAL = "TERMINAL";
  // +++ needs unique index in appliance table (BUT NOT HERE!)
  // can be null in terminal table, but not in appliance table
  public static final ColumnProfile applianceid =ColumnProfile.create(TERMINAL, APPLIANCEID ,DBTypesFiltered.CHAR   ,32,ColumnProfile.ALLOWNULL,"ApplianceID" ,ColumnProfile.NOAUTO);
  public static final ColumnProfile terminalid  =ColumnProfile.create(TERMINAL, TERMINALID  ,DBTypesFiltered.CHAR   ,32,ColumnProfile.NOTNULL  ,"Terminal ID" ,ColumnProfile.NOAUTO); // +++ needs unique index in terminal table // replace with TERMID that is integer ???
  public static final ColumnProfile storeid     =ColumnProfile.create(TERMINAL, STOREID     ,DBTypesFiltered.INTEGER, 4,ColumnProfile.NOTNULL  ,"Store ID#"   ,ColumnProfile.NOAUTO); // +++ needs unique index in the store table
  public static final ColumnProfile terminalname=ColumnProfile.create(TERMINAL, TERMINALNAME,DBTypesFiltered.CHAR   , 8,ColumnProfile.NOTNULL  ,"TerminalName",ColumnProfile.NOAUTO); // Used in the tranjour table.

  private static final ColumnProfile [] staticColumns = {
    terminalid,
    terminalname,
    applianceid,
    storeid,
    // modelcode, deprecated
  };

  public TerminalTable() {
    super(new TableInfo(TERMINAL), staticColumns);
  }
}

