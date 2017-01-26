package net.paymate.database.ours.table;

import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.util.*;
import net.paymate.lang.Bool;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/ApplianceTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.35 $
 */

import net.paymate.data.sinet.hardware.Appliance;

public class ApplianceTable extends GenericTableProfile {
  public final ColumnProfile applianceid   =createColumn(Appliance.APPLIANCEID,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile applname      =createColumn(Appliance.APPLNAME   ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile enabled       =createColumn(Appliance.ENABLED    ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.TRUE());
  public final ColumnProfile statusival    =createColumn(Appliance.STATUSIVAL ,DBTypesFiltered.INT4,CANNULL,NOAUTO,"129");
  public final ColumnProfile storeid       =createColumn(Appliance.STOREID    ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile track         =createColumn(Appliance.TRACK      ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile txnholdoff    =createColumn(Appliance.TXNHOLDOFF ,DBTypesFiltered.INT4,CANNULL,NOAUTO,"37"); // get the default from a server boot setting ??? +++

  IndexProfile applnameindex = new IndexProfile("ap_name", this, applname, true); // unique
  // next one is a partial index: "where not track" [although you might have to use "where track is false"
  // [always use the lower frequency of true or false to make the field smaller]
  IndexProfile ap_track = new IndexProfile("ap_track", this, track, PayMateDBQueryString.whereNot(track));
  IndexProfile ap_storeidx = new IndexProfile("ap_storeidx", this, storeid);

  public ApplianceTable() {
    super(APPLIANCETABLE, cfgType);
    setContents("appliancepk", applianceid);
  }
}
