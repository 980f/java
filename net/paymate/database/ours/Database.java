package net.paymate.database.ours;

import net.paymate.database.ours.table.*;
import net.paymate.database.*; // TableProfile

/**
 * Title:        Database
 * Description:  The paymate database (actually more than that now) -- static representation
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Database.java,v 1.15 2001/11/16 13:17:27 mattm Exp $
 */

public interface Database {

  public static final EnterpriseTable enterprise           = new EnterpriseTable();
  public static final StoreTable store                     = new StoreTable();
  public static final TerminalTable terminal               = new TerminalTable();
  public static final TranjourTable tranjour               = new TranjourTable();
  public static final DrawerTable drawer                   = new DrawerTable();
  public static final AssociateTable associate             = new AssociateTable();
  public static final BinTable bin                         = new BinTable();
  public static final PaytypeTable paytype                 = new PaytypeTable();
  public static final StoreaccessTable storeaccess         = new StoreaccessTable();

  public TableProfile [] tables = {
    associate,
    bin,
    enterprise,
    paytype,
    store,
    storeaccess,
    terminal,
    tranjour,
    drawer,
  };

}
