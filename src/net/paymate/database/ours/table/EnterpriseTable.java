package net.paymate.database.ours.table;

/**
 * Title:        $Source: /cvs/src/net/paymate/database/ours/table/EnterpriseTable.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class EnterpriseTable extends TableProfile implements DBConstants {

  public static final String ENTERPRISE = "ENTERPRISE";

  // fieldname constants
  public static final String ADDRESS1       = "ADDRESS1";
  public static final String ADDRESS2       = "ADDRESS2";
  public static final String CITY           = "CITY";
  public static final String COUNTRY        = "COUNTRY";
  public static final String ENTERPRISENAME = "ENTERPRISENAME";
  public static final String HOMEPAGE       = "HOMEPAGE";
  public static final String PHONE          = "PHONE";
  public static final String STATE          = "STATE";
  public static final String ZIPCODE        = "ZIPCODE";

  public static final ColumnProfile enterpriseid  = ColumnProfile.create(ENTERPRISE, ENTERPRISEID  , DBTypesFiltered.INTEGER,  4, ColumnProfile.NOTNULL  , "EnterpriseId"  , ColumnProfile.AUTO);

  public static final ColumnProfile address1      = ColumnProfile.create(ENTERPRISE, ADDRESS1      , DBTypesFiltered.CHAR   , 20, ColumnProfile.ALLOWNULL, "Address1"      , ColumnProfile.NOAUTO);
  public static final ColumnProfile address2      = ColumnProfile.create(ENTERPRISE, ADDRESS2      , DBTypesFiltered.CHAR   , 20, ColumnProfile.ALLOWNULL, "Address2"      , ColumnProfile.NOAUTO);
  public static final ColumnProfile city          = ColumnProfile.create(ENTERPRISE, CITY          , DBTypesFiltered.CHAR   , 20, ColumnProfile.ALLOWNULL, "City"          , ColumnProfile.NOAUTO);
  public static final ColumnProfile country       = ColumnProfile.create(ENTERPRISE, COUNTRY       , DBTypesFiltered.CHAR   ,  2, ColumnProfile.ALLOWNULL, "Country"       , ColumnProfile.NOAUTO);
  public static final ColumnProfile enterprisename= ColumnProfile.create(ENTERPRISE, ENTERPRISENAME, DBTypesFiltered.CHAR   , 40, ColumnProfile.NOTNULL  , "EnterpriseName", ColumnProfile.NOAUTO);
  public static final ColumnProfile homepage      = ColumnProfile.create(ENTERPRISE, HOMEPAGE      , DBTypesFiltered.CHAR   , 80, ColumnProfile.ALLOWNULL, "Homepage"      , ColumnProfile.NOAUTO);
  public static final ColumnProfile phone         = ColumnProfile.create(ENTERPRISE, PHONE         , DBTypesFiltered.CHAR   , 18, ColumnProfile.ALLOWNULL, "Phone"         , ColumnProfile.NOAUTO);
  public static final ColumnProfile state         = ColumnProfile.create(ENTERPRISE, STATE         , DBTypesFiltered.CHAR   ,  2, ColumnProfile.ALLOWNULL, "TX"            , ColumnProfile.NOAUTO);
  public static final ColumnProfile zipcode       = ColumnProfile.create(ENTERPRISE, ZIPCODE       , DBTypesFiltered.CHAR   , 10, ColumnProfile.ALLOWNULL, "Zipcode"       , ColumnProfile.NOAUTO);


  private static final ColumnProfile [] staticColumns = {
    address1,
    address2,
    city,
    country,
    enterpriseid,
    enterprisename,
    homepage,
    phone,
    state,
    zipcode,
  };

  public EnterpriseTable() {
    super(new TableInfo(ENTERPRISE), staticColumns);
  }
}

// $Id: EnterpriseTable.java,v 1.1 2001/11/16 01:34:31 mattm Exp $
