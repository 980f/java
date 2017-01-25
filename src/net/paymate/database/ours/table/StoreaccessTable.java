package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/StoreaccessTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class StoreaccessTable extends TableProfile implements DBConstants {

  private static final String STOREACCESS = "STOREACCESS";

  private static final ColumnProfile [] staticColumns = {
  };

  public StoreaccessTable() {
    super(new TableInfo(STOREACCESS), staticColumns);
  }
}

