package net.paymate.database.ours.table;

import net.paymate.database.*;
import net.paymate.database.ours.*;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/AuthorizerTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

/*
  Authorizers table actually contains many agencies. Only entries who have an authclass entry will be instantiated as an authorizer in the system
*/
public class AuthorizerTable extends GenericTableProfile {
  public final ColumnProfile authid   =createColumn("authid"   ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile authname =createColumn("authname" ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile authclass=createColumn("authclass",DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);

  IndexProfile iuau_name = new IndexProfile("iuau_name", this, authname); // +++ make it unique @@@ %%% !!!
  IndexProfile iau_class = new IndexProfile("iau_class", this, authclass);

  public AuthorizerTable() {
    super(AUTHORIZERTABLE, cfgType);
    setContents("authidpk", authid);
  }
}

