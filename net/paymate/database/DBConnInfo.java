package net.paymate.database;

/**
 * Title:        $Source $
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DBConnInfo.java,v 1.4 2001/11/16 01:34:29 mattm Exp $
 */

import net.paymate.util.*;

public class DBConnInfo {

  public String connDatasource = "";
  String connUser = "";
  String connPass = "";
  String drivername = "";

  public DBConnInfo() {
  }

  public DBConnInfo(String connDatasource, String connUser, String connPass, String drivername) {
    this.connDatasource = connDatasource;
    this.connUser = connUser;
    this.connPass = connPass;
    this.drivername = Safe.TrivialDefault(drivername, "com.informix.jdbc.IfxDriver");
  }

  public DBConnInfo(String connDatasource, String connUser, String connPass) {
    this(connDatasource, connUser, connPass, null);
  }

}

// $Id: DBConnInfo.java,v 1.4 2001/11/16 01:34:29 mattm Exp $
