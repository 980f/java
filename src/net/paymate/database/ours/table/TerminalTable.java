package net.paymate.database.ours.table;

import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.util.*;
import net.paymate.lang.Bool;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TerminalTable.java,v 1.41 2004/03/03 23:11:25 mattm Exp $
 */

public class TerminalTable extends GenericTableProfile {
  // +++ deprecate modelcode when equipment tables are in place
  public final ColumnProfile applianceid =createColumn("applianceid" ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile dosigcap    =createColumn("dosigcap"    ,DBTypesFiltered.BOOL,NOTNULL,NOAUTO,Bool.TRUE());
  public final ColumnProfile enabled     =createColumn("enabled"     ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.TRUE());
  public final ColumnProfile enavs       =createColumn("enavs"       ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile ensimodify  =createColumn("ensimodify"  ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile eqhack      =createColumn("eqhack"      ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile modelcode   =createColumn("modelcode"   ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile storeid     =createColumn("storeid"     ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile terminalid  =createColumn("terminalid"  ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile terminalname=createColumn("terminalname",DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile twocopies   =createColumn("twocopies"   ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());

  IndexProfile teapplidx = new IndexProfile("teapplidx", this, applianceid);
  IndexProfile ipk_terminalname = new IndexProfile("ipk_terminalname", this, terminalname);
  IndexProfile testoridx = new IndexProfile("testoridx", this, storeid);

  public TerminalTable() {
    super(TERMINALTABLE, cfgType);
    setContents("pka_terminalid", terminalid);
  }
}

