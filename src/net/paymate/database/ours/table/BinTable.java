package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/BinTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class BinTable extends TableProfile implements DBConstants {

  private static final String BIN = "BIN";

  private static final ColumnProfile [] staticColumns = {
  };

  public BinTable() {
    super(new TableInfo(BIN), staticColumns);
  }
}

