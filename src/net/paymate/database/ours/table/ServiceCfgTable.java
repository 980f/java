package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/ServiceCfgTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.16 $
 */

import net.paymate.database.*; // TableProfile
import net.paymate.database.ours.*; // DBConstants

public class ServiceCfgTable extends GenericTableProfile {
  public final ColumnProfile paramname   =createColumn("paramname"   ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile paramvalue  =createColumn("paramvalue"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile servicecfgid=createColumn("servicecfgid",DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile servicename =createColumn("servicename" ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);

  IndexProfile svcnameparamndx = new IndexProfile("isc_svcparam" , this, new ColumnProfile [] {servicename,paramname,}, /*uniue*/ true);
  IndexProfile isc_svcname     = new IndexProfile("isc_svcname"  , this, servicename);
  IndexProfile isc_paramname   = new IndexProfile("isc_paramname", this, paramname);

  public ServiceCfgTable() {
    super(SERVICECFGTABLE, cfgType);
    setContents("servicecfgpk", servicecfgid);
  }
}
