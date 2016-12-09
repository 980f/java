package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/AssociateTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class AssociateTable extends TableProfile implements DBConstants {

  private static final String ASSOCIATE = "ASSOCIATE";

  public static final ColumnProfile enterpriseid =ColumnProfile.create(ASSOCIATE, ENTERPRISEID ,DBTypesFiltered.INTEGER, 4,ColumnProfile.ALLOWNULL,"Enterprise ID",ColumnProfile.NOAUTO);
  public static final ColumnProfile colorschemeid=ColumnProfile.create(ASSOCIATE, COLORSCHEMEID,DBTypesFiltered.CHAR   ,16,ColumnProfile.ALLOWNULL,"ColorSchemeID",ColumnProfile.NOAUTO);

  private static final ColumnProfile [] staticColumns = {
    enterpriseid,
    colorschemeid,
  };

  public AssociateTable() {
    super(new TableInfo(ASSOCIATE), staticColumns);
  }
}

