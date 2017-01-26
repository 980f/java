package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/StoreAuthTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.19 $
 */

import net.paymate.database.*; // TableProfile
import net.paymate.database.ours.*; // DBConstants
import net.paymate.data.PayType;

public class StoreAuthTable extends GenericTableProfile {
  private static final String ptdefault = String.valueOf(new PayType().CharFor(PayType.Unknown));

  public final ColumnProfile authid       =createColumn("authid"     ,DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);
  public final ColumnProfile authmerchid  =createColumn("authmerchid",DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  // convert to an ID link to the issuers table ...
  public final ColumnProfile institution  =createColumn("institution",DBTypesFiltered.TEXT,NOTNULL,NOAUTO, null);
  public final ColumnProfile maxtxnlimit  =createColumn("maxtxnlimit",DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);
  // convert to a 1-char field ...
  public final ColumnProfile paytype      =createColumn("paytype"    ,DBTypesFiltered.CHAR,NOTNULL,NOAUTO, ptdefault);
  public final ColumnProfile settleid     =createColumn("settleid"   ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile settlemerchid=createColumn("settlemerchid",DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile storeauthid  =createColumn("storeauthid",DBTypesFiltered.INT4,NOTNULL,AUTO  , null);
  public final ColumnProfile storeid      =createColumn("storeid"    ,DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);

  IndexProfile saauthidx = new IndexProfile("saauthidx", this, authid);
  IndexProfile sastoreidx = new IndexProfile("sastoreidx", this, storeid);
  IndexProfile sasettleidx = new IndexProfile("sasettleidx", this, settleid);
  IndexProfile sa_paytype = new IndexProfile("sa_paytype", this, paytype);
  IndexProfile sa_inst = new IndexProfile("sa_inst", this, institution);

  public StoreAuthTable() {
    super(STOREAUTHTABLE, cfgType);
    setContents("storeauthpk", storeauthid);
  }
}
