package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/BatchTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.23 $
 */

import net.paymate.data.*;
import net.paymate.database.*; // TableProfile
import net.paymate.database.ours.*; // DBConstants

public class BatchTable extends GenericTableProfile {
  public final ColumnProfile actioncode  =createColumn("actioncode"  ,DBTypesFiltered.CHAR,CANNULL,NOAUTO,ActionCode.Unknown);
  public final ColumnProfile authrespmsg =createColumn("authrespmsg" ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"in progress");
  public final ColumnProfile auto        =createColumn("auto"        ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,null);
  public final ColumnProfile batchid     =createColumn("batchid"     ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile batchseq    =createColumn("batchseq"    ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  // convert to a no-millis int4 field ...
  public final ColumnProfile batchtime   =createColumn("batchtime"   ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  // +++ add a duration field that tells how long it took in millis (int4)
  public final ColumnProfile termauthid  =createColumn("termauthid"  ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile termbatchnum=createColumn("termbatchnum",DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile txncount    =createColumn("txncount"    ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile txntotal    =createColumn("txntotal"    ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);

  IndexProfile batchtimeidx = new IndexProfile("batchtimeidx", this, batchtime);
  IndexProfile batchcntidx  = new IndexProfile("batchcntidx" , this, txncount);
  IndexProfile batchttlidx  = new IndexProfile("batchttlidx" , this, txntotal);
  IndexProfile batchacnidx  = new IndexProfile("batchacnidx" , this, actioncode);
  IndexProfile batchresidx  = new IndexProfile("batchresidx" , this, authrespmsg);
  IndexProfile bataidx      = new IndexProfile("bataidx"     , this, termauthid);
  IndexProfile batchseqidx  = new IndexProfile("batchseqidx" , this, batchseq);
  IndexProfile batchtnoidx  = new IndexProfile("batchtnoidx" , this, termbatchnum);
  // partial index ...
  IndexProfile batchauto    = new IndexProfile("batchauto"   , this, auto, PayMateDBQueryString.whereIsTrue(auto));

  public BatchTable() {
    super(BATCHTABLE, logType);
    setContents("batchpk", batchid);
  }
}
