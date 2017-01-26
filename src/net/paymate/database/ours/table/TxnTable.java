package net.paymate.database.ours.table;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: TxnTable.java,v 1.48 2004/03/02 00:15:38 mattm Exp $
 */

import net.paymate.data.ActionCode;
import net.paymate.data.SettleOp;
import net.paymate.database.*;
import net.paymate.database.ours.*;

public class TxnTable extends GenericTableProfile {

  private static final SettleOp settleoptemp = new SettleOp();
  private static final String settleOpDefault = String.valueOf(settleoptemp.CharFor(SettleOp.Unknown));

 // the following can be reduced by 84 bytes per record, or 42MB at our current size, or 84MB for a million records (a day at 3000 terminals):
  public final ColumnProfile acctbalance      =createColumn("acctbalance"    ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile actioncode       =createColumn("actioncode"     ,DBTypesFiltered.CHAR,CANNULL,NOAUTO, ActionCode.Unknown);
  public final ColumnProfile amount           =createColumn("amount"         ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile approvalcode     =createColumn("approvalcode"   ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile associateid      =createColumn("associateid"    ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  // convert to an int4 millis duration field ...
  public final ColumnProfile authendtime      =createColumn("authendtime"    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile authid           =createColumn("authid"         ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile authresponsemsg  =createColumn("authresponsemsg",DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile authrrn          =createColumn("authrrn"        ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile authseq          =createColumn("authseq"        ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  // convert to a no-millis int4 field ...
  public final ColumnProfile authstarttime    =createColumn("authstarttime"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile authtracedata    =createColumn("authtracedata"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile authz            =createColumn("authz"          ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, null); // basically defaults to false
  public final ColumnProfile avsrespcode      =createColumn("avsrespcode"    ,DBTypesFiltered.CHAR,CANNULL,NOAUTO, " "); // eventually get this from somewhere +++
  public final ColumnProfile batchid          =createColumn("batchid"        ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile cardhash      =createColumn("cardhash"      ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile cardlast4     =createColumn("cardlast4"     ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  // convert to a no-millis int4 field ...
  public final ColumnProfile clientreftime    =createColumn("clientreftime"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile cpsaci           =createColumn("cpsaci"         ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile cpsrespcode      =createColumn("cpsrespcode"    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile cpstxnid         =createColumn("cpstxnid"       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile cpsvalcode       =createColumn("cpsvalcode"     ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile drawerid         =createColumn("drawerid"       ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile echa          =createColumn("echa"          ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile echn          =createColumn("echn"          ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  // convert to an int4 ID link to the issuers table ...
  public final ColumnProfile institution      =createColumn("institution"    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile manual           =createColumn("manual"         ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, null);
  public final ColumnProfile merchref         =createColumn("merchref"       ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile origtxnid        =createColumn("origtxnid"      ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile paytype          =createColumn("paytype"        ,DBTypesFiltered.CHAR,CANNULL,NOAUTO, null);
  public final ColumnProfile servicecode      =createColumn("servicecode"    ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile settle           =createColumn("settle"         ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, null); // basically defaults to false
  public final ColumnProfile settleamount     =createColumn("settleamount"   ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile settleid         =createColumn("settleid"       ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile settleop         =createColumn("settleop"       ,DBTypesFiltered.CHAR,CANNULL,NOAUTO, settleOpDefault);
  public final ColumnProfile signature        =createColumn("signature"      ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile stan             =createColumn("stan"           ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  public final ColumnProfile stoodin          =createColumn("stoodin"        ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, null);
  public final ColumnProfile terminalid       =createColumn("terminalid"     ,DBTypesFiltered.INT4,CANNULL,NOAUTO, null);
  // convert to an int4 millis duration field ...
  public final ColumnProfile tranendtime      =createColumn("tranendtime"    ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile transfertype     =createColumn("transfertype"   ,DBTypesFiltered.CHAR,CANNULL,NOAUTO, null);
  // convert to a no-millis int4 field ...
  public final ColumnProfile transtarttime    =createColumn("transtarttime"  ,DBTypesFiltered.TEXT,CANNULL,NOAUTO, null);
  public final ColumnProfile txnid            =createColumn("txnid"          ,DBTypesFiltered.INT4,NOTNULL,AUTO  , null);
  public final ColumnProfile voided           =createColumn("voided"         ,DBTypesFiltered.BOOL,CANNULL,NOAUTO, null);


  IndexProfile txcardlast4x  = new IndexProfile("txcardlast4x" , this, cardlast4);
  IndexProfile txcardhashx   = new IndexProfile("txcardhashx"  , this, cardhash);
  IndexProfile txassocidx    = new IndexProfile("txassocidx"   , this, associateid); // --- remove this index?
  IndexProfile txauthidx     = new IndexProfile("txauthidx"    , this, authid);
  IndexProfile txsettleidx   = new IndexProfile("txsettleidx"  , this, settleid);
//  IndexProfile txsetlopidx   = new IndexProfile("txsetlopidx"  , this, settleop);
  IndexProfile txtermidx     = new IndexProfile("txtermidx"    , this, terminalid);
  IndexProfile txdrwridx     = new IndexProfile("txdrwridx"    , this, drawerid);
  IndexProfile txbatchidx    = new IndexProfile("txbatchidx"   , this, batchid);
  IndexProfile txorigtxnidx  = new IndexProfile("txorigtxnidx" , this, origtxnid); // +++ can this use a foreign key?
  IndexProfile txi_action    = new IndexProfile("txi_action"   , this, actioncode);
  IndexProfile txi_amount    = new IndexProfile("txi_amount"   , this, amount);
  IndexProfile txi_approval  = new IndexProfile("txi_approval" , this, approvalcode);
  IndexProfile txi_authend   = new IndexProfile("txi_authend"  , this, authendtime);
  IndexProfile ti_clreftime  = new IndexProfile("ti_clreftime" , this, clientreftime);
  IndexProfile ti_inst       = new IndexProfile("ti_inst"      , this, institution);
  IndexProfile tx_paytype    = new IndexProfile("tx_paytype"  , this, paytype);
  IndexProfile txi_authrrn   = new IndexProfile("txi_authrrn"  , this, authrrn);
  IndexProfile ti_svccode    = new IndexProfile("ti_svccode"   , this, servicecode); // --- remove this index?
  IndexProfile txi_stan      = new IndexProfile("txi_stan"     , this, stan);
  // a partial index of the less frequently true "where stoodin ["where stoodin IS TRUE"] clause
  IndexProfile ti_stoodin    = new IndexProfile("ti_stoodin"   , this, stoodin, PayMateDBQueryString.whereIsTrue(stoodin));
  IndexProfile ti_tfertype   = new IndexProfile("ti_tfertype"  , this, transfertype);
  IndexProfile txi_transtart = new IndexProfile("txi_transtart", this, transtarttime);
  // a partial index of the less frequently true "where voided" ["where voided IS TRUE"] clause
  IndexProfile txi_voided    = new IndexProfile("txi_voided"   , this, voided, PayMateDBQueryString.whereIsTrue(voided));
  IndexProfile ti_merchref   = new IndexProfile("ti_merchref"  , this, merchref);
  IndexProfile ti_authz      = new IndexProfile("ti_authz"     , this, authz);
  // indexes for ISNULL fields:
  IsNullIndexProfile txdrwrnull = new IsNullIndexProfile("txdrwrnull", this, drawerid);
  IsNullIndexProfile txbtchnull = new IsNullIndexProfile("txbtchnull", this, batchid);
  IsNullIndexProfile txaendnull = new IsNullIndexProfile("txaendnull", this, authendtime);
  IsNullIndexProfile txsttlnull = new IsNullIndexProfile("txsttlnull", this, settleid);

  public TxnTable() {
    super(TXNTABLE, logType);
    setContents("txnpk", txnid);
  }
}

// $Id: TxnTable.java,v 1.48 2004/03/02 00:15:38 mattm Exp $
