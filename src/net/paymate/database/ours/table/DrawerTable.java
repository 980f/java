package net.paymate.database.ours.table;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: DrawerTable.java,v 1.20 2003/10/01 04:33:07 mattm Exp $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class DrawerTable extends GenericTableProfile {
  public final ColumnProfile associateid  =createColumn("associateid"  ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile auto         =createColumn("auto"         ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,null);
  public final ColumnProfile drawerid     =createColumn("drawerid"     ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile terminalid   =createColumn("terminalid"   ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  // convert to a no-millis int4 field ...
  public final ColumnProfile transtarttime=createColumn("transtarttime",DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile txncount     =createColumn("txncount"     ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile txntotal     =createColumn("txntotal"     ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);

  IndexProfile drassocidx  = new IndexProfile("drassocidx" , this, associateid);
  IndexProfile drtermidx   = new IndexProfile("drtermidx"  , this, terminalid);
  IndexProfile drcntidx    = new IndexProfile("drcntidx"   , this, txncount);
  IndexProfile drttlidx    = new IndexProfile("drttlidx"   , this, txntotal);
  IndexProfile bmstarttime = new IndexProfile("bmstarttime", this, transtarttime);
  // partial index ...
  IndexProfile drauto      = new IndexProfile("drauto"     , this, auto, PayMateDBQueryString.whereIsTrue(auto));

  public DrawerTable() {
    super(DRAWERTABLE, logType);
    setContents("pkdr_drawerid", drawerid);
   }
}

