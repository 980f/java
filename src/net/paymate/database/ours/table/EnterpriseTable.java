package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/EnterpriseTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.27 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.data.EnterpriseType;
import net.paymate.data.sinet.business.Enterprise;
import net.paymate.util.*;
import net.paymate.lang.Bool;

public class EnterpriseTable extends GenericTableProfile {

  public final ColumnProfile address1      =createColumn(Enterprise.ADDRESS1      ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile address2      =createColumn(Enterprise.ADDRESS2      ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile city          =createColumn(Enterprise.CITY          ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,Enterprise.CITYDEFAULT);
  public final ColumnProfile country       =createColumn(Enterprise.COUNTRY       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,Enterprise.COUNTRYDEFAULT);
  public final ColumnProfile enabled       =createColumn(Enterprise.ENABLED       ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Enterprise.ENABLEDDEFAULT);
  public final ColumnProfile enterpriseid  =createColumn(Enterprise.ENTERPRISEID  ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile enterprisename=createColumn(Enterprise.ENTERPRISENAME,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null); // notnull enforced on this in code!
//  public final ColumnProfile enterprisetype=createColumn(Enterprise.ENTERPRISETYPE,DBTypesFiltered.CHAR,NOTNULL,NOAUTO,(new EnterpriseType()).TextFor(EnterpriseType.M)); // defaults to a merchant type, for when the system adds the field to the database
  public final ColumnProfile notes         =createColumn(Enterprise.NOTES         ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile phone         =createColumn(Enterprise.PHONE         ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
//  public final ColumnProfile spenterpriseid=createColumn(Enterprise.SPENTERPRISEID,DBTypesFiltered.INT4,CANNULL,NOAUTO,null); // +++ Change to NOTNULL once all of the code is in place
//  public final ColumnProfile spenabled     =createColumn(Enterprise.SPENABLED     ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile state         =createColumn(Enterprise.STATE         ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,Enterprise.STATEDEFAULT);
  public final ColumnProfile zipcode       =createColumn(Enterprise.ZIPCODE       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);

//  IndexProfile ei_type = new IndexProfile("ei_type"     , this, enterprisetype);

  public EnterpriseTable() {
    super(ENTERPRISETABLE, cfgType); // +++ should be set from the Enterprise object, not from here!
    setContents("enterprisepk", enterpriseid);
  }
}

// $Id: EnterpriseTable.java,v 1.27 2003/11/24 04:52:30 mattm Exp $
