package net.paymate.database;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ForeignKeyProfile.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

public class ForeignKeyProfile extends KeyProfile {
  public boolean isPrimary() {
    return false; // if it isn't primary, it is foreign
  }

  public TableProfile referenceTable = null;

  public ForeignKeyProfile(String name, TableProfile table, ColumnProfile field, TableProfile referenceTable) {
    super(name, table, field);
    this.referenceTable = referenceTable;
  }
}