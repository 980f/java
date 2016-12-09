package net.paymate.database.ours.table;

/**
 * Title:        $Source $
 * Description:  Store table definition
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: StoreTable.java,v 1.2 2001/11/17 06:16:59 mattm Exp $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;

public class StoreTable extends TableProfile implements DBConstants {

  public static final String STORE             = "STORE";
  // fieldname constants
  public static final String ADDRESS1          = "address1";
  public static final String ADDRESS2          = "address2";
  public static final String AUTOAPPROVE       = "autoApprove"; // LEAVE CASE LIKE IT IS! (used by another class) default false
  public static final String CITY              = "city";
  public static final String CHECKSALLOWED     = "checksAllowed"; // LEAVE CASE LIKE IT IS! (used by another class) default false
  public static final String COUNTRY           = "country";
  public static final String CREDITALLOWED     = "creditAllowed"; // LEAVE CASE LIKE IT IS! (used by another class) default false
  public static final String DEBITALLOWED      = "debitAllowed"; // LEAVE CASE LIKE IT IS! (used by another class) default false
  public static final String FREEPASS          = "freePass";    // LEAVE CASE LIKE IT IS! (used by another class) default true
  public static final String JAVATZ            = "javatz";
  public static final String RECEIPTABIDE      = "receiptabide";
  public static final String RECEIPTHEADER     = "receiptheader";
  public static final String RECEIPTSHOWSIG    = "receiptshowsig";
  public static final String RECEIPTTAGLINE    = "receipttagline";
  public static final String RECEIPTTIMEFORMAT = "receipttimeformat";
  public static final String STANDINLIMIT      = "standinlimit";
  public static final String STANOMETER        = "stanometer";
  public static final String STATE             = "state";
  public static final String STOREHOMEPAGE     = "storehomepage";
  public static final String STORENAME         = "storename";
  public static final String STORESTANDINTOTAL = "storestandintotal";
  public static final String ZIPCODE           = "zipcode";

//  public static final ColumnProfile storeid           = ColumnProfile.create(STORE, STOREID          , DBTypesFiltered.INTEGER,   4, ColumnProfile.NOTNULL  , "StoreId"   , ColumnProfile.NOAUTO);
  public static final ColumnProfile address1          = ColumnProfile.create(STORE, ADDRESS1         , DBTypesFiltered.CHAR   ,  20, ColumnProfile.ALLOWNULL, "Address1"         , ColumnProfile.NOAUTO);
  public static final ColumnProfile address2          = ColumnProfile.create(STORE, ADDRESS2         , DBTypesFiltered.CHAR   ,  20, ColumnProfile.ALLOWNULL, "Address2"         , ColumnProfile.NOAUTO);
  public static final ColumnProfile authtermid        = ColumnProfile.create(STORE, AUTHTERMID       , DBTypesFiltered.CHAR   ,  10, ColumnProfile.ALLOWNULL, "AuthTermId"       , ColumnProfile.NOAUTO);
  public static final ColumnProfile autoapprove       = ColumnProfile.create(STORE, AUTOAPPROVE      , DBTypesFiltered.CHAR   ,   1, ColumnProfile.ALLOWNULL, "AutoApprove"      , ColumnProfile.NOAUTO);
  public static final ColumnProfile checksallowed     = ColumnProfile.create(STORE, CHECKSALLOWED    , DBTypesFiltered.CHAR   ,   1, ColumnProfile.ALLOWNULL, "ChecksAllowed"    , ColumnProfile.NOAUTO); // +++ default false
  public static final ColumnProfile city              = ColumnProfile.create(STORE, CITY             , DBTypesFiltered.CHAR   ,  20, ColumnProfile.ALLOWNULL, "City"             , ColumnProfile.NOAUTO);
  public static final ColumnProfile country           = ColumnProfile.create(STORE, COUNTRY          , DBTypesFiltered.CHAR   ,   2, ColumnProfile.ALLOWNULL, "Country"          , ColumnProfile.NOAUTO);
  public static final ColumnProfile creditallowed     = ColumnProfile.create(STORE, CREDITALLOWED    , DBTypesFiltered.CHAR   ,   1, ColumnProfile.ALLOWNULL, "DebitAllowed"     , ColumnProfile.NOAUTO); // +++ default false
  public static final ColumnProfile debitallowed      = ColumnProfile.create(STORE, DEBITALLOWED     , DBTypesFiltered.CHAR   ,   1, ColumnProfile.ALLOWNULL, "DebitAllowed"     , ColumnProfile.NOAUTO); // +++ default false
  public static final ColumnProfile enterpriseid      = ColumnProfile.create(STORE, ENTERPRISEID     , DBTypesFiltered.CHAR   ,   6, ColumnProfile.ALLOWNULL, "EnterpriseId"     , ColumnProfile.NOAUTO); // after conversion, change to notnull +++
  public static final ColumnProfile freepass          = ColumnProfile.create(STORE, FREEPASS         , DBTypesFiltered.CHAR   ,   1, ColumnProfile.ALLOWNULL, "FreePass"         , ColumnProfile.NOAUTO);
  public static final ColumnProfile javatz            = ColumnProfile.create(STORE, JAVATZ           , DBTypesFiltered.CHAR   ,  30, ColumnProfile.ALLOWNULL, "JavaTZ"           , ColumnProfile.NOAUTO); // +++ after conversion, make notnull
  public static final ColumnProfile receiptabide      = ColumnProfile.create(STORE, RECEIPTABIDE     , DBTypesFiltered.CHAR   ,  60, ColumnProfile.ALLOWNULL, "ReceiptAbide"     , ColumnProfile.NOAUTO); // +++ after conversion, make notnull
  public static final ColumnProfile receiptheader     = ColumnProfile.create(STORE, RECEIPTHEADER    , DBTypesFiltered.CHAR   , 120, ColumnProfile.ALLOWNULL, "ReceiptHeader"    , ColumnProfile.NOAUTO);
  public static final ColumnProfile receiptshowsig    = ColumnProfile.create(STORE, RECEIPTSHOWSIG   , DBTypesFiltered.CHAR   ,   1, ColumnProfile.ALLOWNULL, "RcptShowSig"      , ColumnProfile.NOAUTO); // +++ after conversion, make notnull
  public static final ColumnProfile receipttagline    = ColumnProfile.create(STORE, RECEIPTTAGLINE   , DBTypesFiltered.CHAR   , 120, ColumnProfile.ALLOWNULL, "ReceiptTagline"   , ColumnProfile.NOAUTO);
  public static final ColumnProfile receipttimeformat = ColumnProfile.create(STORE, RECEIPTTIMEFORMAT, DBTypesFiltered.CHAR   ,  42, ColumnProfile.ALLOWNULL, "RcptTimeFormat"   , ColumnProfile.NOAUTO); // +++ after conversion, make notnull
  public static final ColumnProfile standinlimit      = ColumnProfile.create(STORE, STANDINLIMIT     , DBTypesFiltered.DECIMAL,  10, ColumnProfile.ALLOWNULL, "Standinlimit"     , ColumnProfile.NOAUTO); // 10.2
  public static final ColumnProfile stanometer        = ColumnProfile.create(STORE, STANOMETER       , DBTypesFiltered.INTEGER,   4, ColumnProfile.ALLOWNULL, "Stanometer"       , ColumnProfile.NOAUTO); // +++ after conversion, make notnull
  public static final ColumnProfile state             = ColumnProfile.create(STORE, STATE            , DBTypesFiltered.CHAR   ,   2, ColumnProfile.ALLOWNULL, "State"            , ColumnProfile.NOAUTO);
  public static final ColumnProfile storehomepage     = ColumnProfile.create(STORE, STOREHOMEPAGE    , DBTypesFiltered.CHAR   ,  80, ColumnProfile.ALLOWNULL, "StoreHomepage"    , ColumnProfile.NOAUTO);
  public static final ColumnProfile storename         = ColumnProfile.create(STORE, STORENAME        , DBTypesFiltered.CHAR   ,  40, ColumnProfile.ALLOWNULL, "StoreName"        , ColumnProfile.NOAUTO);
  public static final ColumnProfile storestandintotal = ColumnProfile.create(STORE, STORESTANDINTOTAL, DBTypesFiltered.DECIMAL,  10, ColumnProfile.ALLOWNULL, "StoreStandinTotal", ColumnProfile.NOAUTO); // 10.2
  public static final ColumnProfile zipcode           = ColumnProfile.create(STORE, ZIPCODE          , DBTypesFiltered.CHAR   ,   9, ColumnProfile.ALLOWNULL, "Zipcode"          , ColumnProfile.NOAUTO);
//                                                                                                                              629
  private static final ColumnProfile [] staticColumns = {
//    storeid,
    address1,
    address2,
    authtermid,
    autoapprove,
    checksallowed,
    city,
    country,
    creditallowed,
    debitallowed,
    enterpriseid,
    freepass,
    javatz,
    receiptabide,
    receiptheader,
    receiptshowsig,
    receipttagline,
    receipttimeformat,
    standinlimit,
    stanometer,
    state,
    storehomepage,
    storename,
    storestandintotal,
    zipcode,
  };

  public StoreTable() {
    super(new TableInfo(STORE), staticColumns);
  }
}

// $Id: StoreTable.java,v 1.2 2001/11/17 06:16:59 mattm Exp $
