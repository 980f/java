package net.paymate.database.ours.table;

/**
 * Title:        $Source $
 * Description:  Store table definition
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: StoreTable.java,v 1.50 2004/03/26 19:58:45 mattm Exp $
 */

import net.paymate.database.*;
import net.paymate.database.ours.*;
import net.paymate.util.*;
import net.paymate.lang.Bool;
import net.paymate.terminalClient.TerminalCapabilities;//we do columns<->properties<->TermCap
import net.paymate.data.MerchantType;
import net.paymate.data.sinet.business.Store;

public class StoreTable extends GenericTableProfile {
// +++ put the rest of these defaults into the Store class!
  public final ColumnProfile address1          =createColumn(Store.ADDRESS1    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"Address 1");
  public final ColumnProfile address2          =createColumn(Store.ADDRESS2    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"Address 2");
  public final ColumnProfile alwaysid          =createColumn(Store.ALWAYSID    ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.TRUE()); // default true
  public final ColumnProfile autoapprove       =createColumn(Store.AUTOAPPROVE ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  public final ColumnProfile autocomplete      =createColumn(Store.AUTOCOMPLETE,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.TRUE()); // default true
  public final ColumnProfile autodepositmins   =createColumn(Store.AUTODEPOSITMINS,DBTypesFiltered.INT4,CANNULL,NOAUTO,"0");
  public final ColumnProfile autodrawermins    =createColumn(Store.AUTODRAWERMINS,DBTypesFiltered.INT4,CANNULL,NOAUTO,"0");
  public final ColumnProfile autoquery         =createColumn(Store.AUTOQUERY   ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default FALSE
  public final ColumnProfile checksallowed     =createColumn(Store.CHECKSALLOWED,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  public final ColumnProfile city              =createColumn(Store.CITY        ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"Austin");
  public final ColumnProfile country           =createColumn(Store.COUNTRY     ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"US");
  public final ColumnProfile creditallowed     =createColumn(Store.CREDITALLOWED,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  public final ColumnProfile debitallowed      =createColumn(Store.DEBITALLOWED,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  public final ColumnProfile debitpushthreshold=createColumn(Store.DEBITPUSHTHRESHOLD,DBTypesFiltered.INT4,CANNULL,NOAUTO,"0"); // default 0
  public final ColumnProfile enabled           =createColumn(Store.ENABLED     ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.TRUE());
  public final ColumnProfile enauthonly        =createColumn(Store.ENAUTHONLY  ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enautodeposit     =createColumn(Store.ENAUTODEPOSIT,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enautodrawer      =createColumn(Store.ENAUTODRAWER,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enautologout      =createColumn(Store.ENAUTOLOGOUT,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enlistsummary     =createColumn(Store.ENLISTSUMMARY,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enmerchref        =createColumn(Store.ENMERCHREF  ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enmodify          =createColumn(Store.ENMODIFY    ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE());
  public final ColumnProfile enterpriseid      =createColumn(Store.ENTERPRISEID,DBTypesFiltered.INT4,NOTNULL,NOAUTO,null);
  public final ColumnProfile freepass          =createColumn(Store.FREEPASS    ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  // +++ convert this next one to a char-1 enumeration?  number them for int4?  what?
  public final ColumnProfile javatz            =createColumn(Store.JAVATZ      ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"America/Chicago");
  public final ColumnProfile merchanttype      =createColumn(Store.MERCHANTTYPE,DBTypesFiltered.CHAR,CANNULL,NOAUTO,Store.merchantTypeDefault);
  public final ColumnProfile merchreflabel     =createColumn(Store.MERCHREFLABEL,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile phone             =createColumn(Store.PHONE       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,null);
  public final ColumnProfile pushdebit         =createColumn(Store.PUSHDEBIT   ,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  public final ColumnProfile receiptabide      =createColumn(Store.RECEIPTABIDE,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"I agree to pay per my cardholder agreement");
  public final ColumnProfile receiptheader     =createColumn(Store.RECEIPTHEADER,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"Receipt header.");
  public final ColumnProfile receiptshowsig    =createColumn(Store.RECEIPTSHOWSIG,DBTypesFiltered.BOOL,CANNULL,NOAUTO,Bool.FALSE()); // default false
  public final ColumnProfile receipttagline    =createColumn(Store.RECEIPTTAGLINE,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"Receipt footer.");
  public final ColumnProfile receipttimeformat =createColumn(Store.RECEIPTTIMEFORMAT,DBTypesFiltered.TEXT,CANNULL,NOAUTO,Store.DEFAULTRCPTTIMEFORMAT);
  public final ColumnProfile sigcapthresh      =createColumn(Store.SIGCAPTHRESH,DBTypesFiltered.INT4,CANNULL,NOAUTO,"0");
  public final ColumnProfile silimit           =createColumn(Store.SILIMIT     ,DBTypesFiltered.INT4,CANNULL,NOAUTO,"3500");
  public final ColumnProfile sitotal           =createColumn(Store.SITOTAL     ,DBTypesFiltered.INT4,CANNULL,NOAUTO,"50000");
  public final ColumnProfile state             =createColumn(Store.STATE       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"TX");
  public final ColumnProfile storeid           =createColumn(Store.STOREID     ,DBTypesFiltered.INT4,NOTNULL,AUTO  ,null);
  public final ColumnProfile storename         =createColumn(Store.STORENAME   ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"Storename here");
  public final ColumnProfile storenumber       =createColumn(Store.STORENUMBER ,DBTypesFiltered.INT4,CANNULL,NOAUTO,null);
  public final ColumnProfile zipcode           =createColumn(Store.ZIPCODE     ,DBTypesFiltered.TEXT,CANNULL,NOAUTO,"787");

  IndexProfile stenteridx = new IndexProfile("stenteridx", this, enterpriseid);
  IndexProfile si_name = new IndexProfile("si_name", this, storename);
  IndexProfile si_number = new IndexProfile("si_number", this, storenumber);

  public StoreTable() {
    super(STORETABLE, cfgType);
    setContents("pka_storeid", storeid);
  }
}

// $Id: StoreTable.java,v 1.50 2004/03/26 19:58:45 mattm Exp $
