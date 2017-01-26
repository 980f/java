package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/ApplPgmStatusTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

import net.paymate.database.*;
import net.paymate.data.sinet.hardware.ApplPgmStatus;
import net.paymate.lang.Bool;

public class ApplPgmStatusTable extends GenericTableProfile {
  public final ColumnProfile applpgmstatusid=createColumn(ApplPgmStatus.APPLPGMSTATUSID,DBTypesFiltered.INT4,NOTNULL,AUTO,null);
  public final ColumnProfile applianceid    =createColumn(ApplPgmStatus.APPLIANCEID    ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile revision       =createColumn(ApplPgmStatus.REVISION       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile appltime       =createColumn(ApplPgmStatus.APPLTIME       ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile srvrtime       =createColumn(ApplPgmStatus.SRVRTIME       ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile freemem        =createColumn(ApplPgmStatus.FREEMEM        ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile ttlmem         =createColumn(ApplPgmStatus.TTLMEM         ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile threadcount    =createColumn(ApplPgmStatus.THREADCOUNT    ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile alarmcount     =createColumn(ApplPgmStatus.ALARMCOUNT     ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile stoodrcpt      =createColumn(ApplPgmStatus.STOODRCPT      ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile stoodtxn       =createColumn(ApplPgmStatus.STOODTXN       ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile wanip          =createColumn(ApplPgmStatus.WANIP          ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile wasconnection  =createColumn(ApplPgmStatus.WASCONNECTION  ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());

  IndexProfile apstime    = new IndexProfile("apstime"    , this, srvrtime);
  IndexProfile aps_applidx= new IndexProfile("aps_applidx", this, applianceid);

  public ApplPgmStatusTable() {
    super(APPLPGMSTATUSTABLE, logType);
    setContents("apspgmpk", applpgmstatusid);
  }
}
