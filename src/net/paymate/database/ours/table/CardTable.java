package net.paymate.database.ours.table;
/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/CardTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.23 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

import net.paymate.data.BinEntry;//for properties <-> columns

public class CardTable extends GenericTableProfile {
  public final ColumnProfile cardid     =createColumn("cardid"            ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile enMod10ck  =createColumn(BinEntry.enMod10ckKey,DBTypesFiltered.BOOL,NOTNULL,NOAUTO,null);
  public final ColumnProfile exp        =createColumn(BinEntry.expiresKey  ,DBTypesFiltered.BOOL,NOTNULL,NOAUTO,null);
  public final ColumnProfile highbin    =createColumn(BinEntry.highKey     ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  // convert to an ID link to the issuers table
  public final ColumnProfile institution=createColumn(BinEntry.issuerKey   ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile lowbin     =createColumn(BinEntry.lowKey      ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  // convert to a 1-char field
  public final ColumnProfile paytype    =createColumn(BinEntry.actKey      ,DBTypesFiltered.CHAR,NOTNULL,NOAUTO,null);

  IndexProfile cardpt = new IndexProfile("cardpt", this, paytype);
  IndexProfile cardin = new IndexProfile("cardin", this, institution);
  IndexProfile cardbinrange = new IndexProfile("cardbinrange", this, new ColumnProfile[] {lowbin, highbin, });

  public CardTable() {
    super(CARDTABLE, cfgType);
    setContents("cardpk", cardid);
  }
}
//$Id: CardTable.java,v 1.23 2003/09/08 18:22:29 mattm Exp $