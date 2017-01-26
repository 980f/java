package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/AssociateTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.30 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.data.sinet.business.Associate;

public class AssociateTable extends GenericTableProfile {
  public final ColumnProfile associateid  =createColumn(Associate.ASSOCIATEID  ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile browselines  =createColumn(Associate.BROWSELINES  ,DBTypesFiltered.INT4,CANNULL,NOAUTO,"15"); // default to 15
  // +++ convert the next one to a char(1) enumeration
  public final ColumnProfile colorschemeid=createColumn(Associate.COLORSCHEMEID,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile enabled      =createColumn(Associate.ENABLED      ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.TRUE());
  // +++ convert the next one to an int4 checksum instead?
  public final ColumnProfile encodedpw    =createColumn(Associate.ENCODEDPW    ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile endb         =createColumn(Associate.ENDB         ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enterpriseid =createColumn(Associate.ENTERPRISEID ,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile enweb        =createColumn(Associate.ENWEB        ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile firstname    =createColumn(Associate.FIRSTNAME    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"FirstName");
  public final ColumnProfile lastname     =createColumn(Associate.LASTNAME     ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"LastName");
  public final ColumnProfile loginname    =createColumn(Associate.LOGINNAME    ,DBTypesFiltered.TEXT,NOTNULL,NOAUTO,null);
  public final ColumnProfile middleinitial=createColumn(Associate.MIDDLEINITIAL,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);

  IndexProfile ai_encodedpw = new IndexProfile("ai_encodedpw", this, encodedpw);
  IndexProfile ai_aidlogin = new IndexProfile("ai_aidlogin", this, new ColumnProfile [ ] {associateid, loginname, });
  IndexProfile ai_loginname = new IndexProfile("ai_loginname", this, loginname);
  IndexProfile ai_enteridx = new IndexProfile("ai_enteridx", this, enterpriseid);

  public AssociateTable() {
    super(ASSOCIATETABLE, cfgType);
    setContents("pka_associateid", associateid);
  }
}

