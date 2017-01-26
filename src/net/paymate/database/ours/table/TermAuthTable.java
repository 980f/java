package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/TermAuthTable.java,v $
 * Description:  Auth * Terminal table (for merchants who use that auth)
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author       PayMate.net
 * @version      $Revision: 1.20 $
 */

import net.paymate.database.*; // TableProfile
import net.paymate.database.ours.*; // DBConstants

public class TermAuthTable extends GenericTableProfile {
  public final ColumnProfile authid      =createColumn("authid"    ,DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);
  // The sequence number to send for the next transaction!
  public final ColumnProfile authseq     =createColumn("authseq"   ,DBTypesFiltered.INT4,CANNULL,NOAUTO, "0");
  // CHAR(10) The terminal id sent to the authorizer for this auth + terminal
  public final ColumnProfile authtermid  =createColumn("authtermid",DBTypesFiltered.TEXT,CANNULL,NOAUTO, "");
  public final ColumnProfile termauthid  =createColumn("termauthid",DBTypesFiltered.INT4,NOTNULL,AUTO  , null);
  // INTEGER; increments with a *successful* batch.
  // This is an odometer, rolling over and being stamped into the batch
  public final ColumnProfile termbatchnum=createColumn("termbatchnum",DBTypesFiltered.INT4,CANNULL,NOAUTO, "0");
  public final ColumnProfile terminalid  =createColumn("terminalid",DBTypesFiltered.INT4,NOTNULL,NOAUTO, null);

  IndexProfile taauthidx = new IndexProfile("taauthidx", this, authid);
  IndexProfile tatermidx = new IndexProfile("tatermidx", this, terminalid);
  IndexProfile taauthtermidx = new IndexProfile("taauthtermidx", this, authtermid);
  IndexProfile tabatchnumidx = new IndexProfile("tabatchnumidx", this, termbatchnum);

  public TermAuthTable() {
    super(TERMAUTHTABLE, cfgType);
    setContents("termauthpk", termauthid);
  }
}

