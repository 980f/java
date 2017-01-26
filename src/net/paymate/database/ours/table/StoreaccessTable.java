package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/StoreaccessTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.22 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.lang.Bool;

public class StoreaccessTable extends GenericTableProfile {
  public final ColumnProfile associateid  =createColumn("associateid"  ,DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);
  public final ColumnProfile enclosedrawer=createColumn("enclosedrawer",DBTypesFiltered.BOOL,CANNULL,NOAUTO, Bool.FALSE());
  public final ColumnProfile enreturn     =createColumn("enreturn"     ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, Bool.FALSE());
  public final ColumnProfile ensale       =createColumn("ensale"       ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, Bool.FALSE());
  public final ColumnProfile envoid       =createColumn("envoid"       ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, Bool.FALSE());
  public final ColumnProfile storeaccessid=createColumn("storeaccessid",DBTypesFiltered.INT4,NOTNULL,AUTO  , null);
  public final ColumnProfile storeid      =createColumn("storeid"      ,DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);

  IndexProfile sxstoreidx = new IndexProfile("sxstoreidx", this, storeid);
  IndexProfile sxassocidx = new IndexProfile("sxassocidx", this, associateid);

  public StoreaccessTable() {
    super(STOREACCESSTABLE, cfgType);
    setContents("storeaxespk", storeaccessid);
  }
}

