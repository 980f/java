/**
 * Title:        DatabaseProfile
 * Description:  Contains information for table(s) used by database profiling, etc.
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Id: DatabaseProfile.java,v 1.4 2001/05/30 02:43:22 mattm Exp $
 */

package net.paymate.database;
import  java.util.Vector;

public class DatabaseProfile extends Vector {
  // +++ contain instead of extend the vector so that remove can be hidden ???
  // +++ add more info in here from the DatabaseMetaData ???
  private String name = null;
  public TableProfile itemAt(int index) {
    return (TableProfile)elementAt(index);
  }
  public DatabaseProfile(String name) {
    this.name = name;
  }
  public String name() {
    return name;
  }
}
