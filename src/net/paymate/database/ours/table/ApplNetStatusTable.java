package net.paymate.database.ours.table;

import net.paymate.database.*;
import net.paymate.data.sinet.hardware.ApplNetStatus;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/ApplNetStatusTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.3 $
 */

public class ApplNetStatusTable extends GenericTableProfile {

  public final ColumnProfile applnetstatusid=createColumn(ApplNetStatus.APPLNETSTATUSID,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile applianceid    =createColumn(ApplNetStatus.APPLIANCEID    ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile appltime       =createColumn(ApplNetStatus.APPLTIME       ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile srvrtime       =createColumn(ApplNetStatus.SRVRTIME       ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile lanip          =createColumn(ApplNetStatus.LANIP          ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile wanip          =createColumn(ApplNetStatus.WANIP          ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);

  IndexProfile anstime    = new IndexProfile("anstime"    , this, srvrtime);
  IndexProfile ans_applidx= new IndexProfile("ans_applidx", this, applianceid);

  public ApplNetStatusTable() {
    super(APPLNETSTATUSTABLE, logType);
    setContents("applnetpk", applnetstatusid);
  }
}
