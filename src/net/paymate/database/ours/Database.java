package net.paymate.database.ours;

import net.paymate.database.ours.table.*;
import net.paymate.database.*; // TableProfile

/**
 * Title:        Database
 * Description:  The paymate database (actually more than that now) -- static representation
 * Copyright:    Copyright (c) 2000
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Id: Database.java,v 1.34 2004/05/23 19:02:48 mattm Exp $
 */

public interface Database {

  public static final ApplianceTable     appliance    = new ApplianceTable();
// no persistence of appliance log data 20040523 !
//  public static final ApplNetStatusTable applnetstatus= new ApplNetStatusTable();
//  public static final ApplPgmStatusTable applpgmstatus= new ApplPgmStatusTable();
  public static final AssociateTable     associate    = new AssociateTable();
  public static final AuthAttemptTable   authattempt  = new AuthAttemptTable();
  public static final AuthorizerTable    authorizer   = new AuthorizerTable();
  public static final BatchTable         batch        = new BatchTable();
  public static final CardTable          card         = new CardTable();
  public static final DrawerTable        drawer       = new DrawerTable();
  public static final EnterpriseTable    enterprise   = new EnterpriseTable();
  public static final ServiceCfgTable    servicecfg   = new ServiceCfgTable();
  public static final StoreTable         store        = new StoreTable();
  public static final StoreaccessTable   storeaccess  = new StoreaccessTable();
  public static final StoreAuthTable     storeauth    = new StoreAuthTable();
  public static final TermAuthTable      termauth     = new TermAuthTable();
  public static final TerminalTable      terminal     = new TerminalTable();
  public static final TxnTable           txn          = new TxnTable();

  public static final ForeignKeyProfile[] applianceForeignKeys = {
    new ForeignKeyProfile("apfk_storeid", appliance, appliance.storeid, store),
  };
// no persistence of appliance log data 20040523 !
//  public static final ForeignKeyProfile[] applnetstatusForeignKeys = {
//    new ForeignKeyProfile("ansfk_applid", applnetstatus, applnetstatus.applianceid, appliance),
//  };
//  public static final ForeignKeyProfile[] applpgmstatusForeignKeys = {
//    new ForeignKeyProfile("apsfk_applid", applpgmstatus, applpgmstatus.applianceid, appliance),
//  };
  public static final ForeignKeyProfile[] associateForeignKeys = {
    new ForeignKeyProfile("asfk_entid", associate, associate.enterpriseid, enterprise),
  };
  public static final ForeignKeyProfile[] authattemptForeignKeys = {
    new ForeignKeyProfile("aafk_txnid", authattempt, authattempt.txnid, txn),
    new ForeignKeyProfile("aafk_authid", authattempt, authattempt.authid, authorizer),
  };
  public static final ForeignKeyProfile[] batchForeignKeys = {
    new ForeignKeyProfile("bataidx", batch, batch.termauthid, termauth),
  };
  public static final ForeignKeyProfile[] drawerForeignKeys = {
    new ForeignKeyProfile("drfk_assid", drawer, drawer.associateid, associate),
    new ForeignKeyProfile("drfk_termid", drawer, drawer.terminalid, terminal),
  };
// +++ Eventually ...
//  public static final ForeignKeyProfile[] enterpriseForeignKeys = {
//    new ForeignKeyProfile("enfk_entid", enterprise, enterprise.spenterpriseid, enterprise),
//  };
  public static final ForeignKeyProfile[] storeAccessForeignKeys = {
    new ForeignKeyProfile("sxfk_storeid", storeaccess, storeaccess.storeid    , store),
    new ForeignKeyProfile("sxfk_associd"  , storeaccess, storeaccess.associateid, associate),
  };
  public static final ForeignKeyProfile[] storeauthForeignKeys = {
    new ForeignKeyProfile("safk_authid"  , storeauth, storeauth.authid  , authorizer),
    new ForeignKeyProfile("safk_storid"  , storeauth, storeauth.storeid , store),
    new ForeignKeyProfile("safk_settleid", storeauth, storeauth.settleid, authorizer),
  };
  public static final ForeignKeyProfile[] storeForeignKeys = {
    new ForeignKeyProfile("stfk_entid", store, store.enterpriseid, enterprise),
  };
  public static final ForeignKeyProfile[] termauthForeignKeys = {
    new ForeignKeyProfile("tafk_authid", termauth, termauth.authid, authorizer),
    new ForeignKeyProfile("tafk_termid", termauth, termauth.terminalid, terminal),
  };
  public static final ForeignKeyProfile[] terminalForeignKeys = {
    new ForeignKeyProfile("tefk_applid", terminal, terminal.applianceid, appliance),
  };
  public static final ForeignKeyProfile[] txnForeignKeys = {
    new ForeignKeyProfile("txfk_associd" , txn, txn.associateid, associate),
    new ForeignKeyProfile("txfk_authid"  , txn, txn.authid     , authorizer),
    new ForeignKeyProfile("txfk_settleid", txn, txn.settleid   , authorizer),
    new ForeignKeyProfile("txfk_termid"  , txn, txn.terminalid , terminal),
    new ForeignKeyProfile("txfk_drwrid"  , txn, txn.drawerid   , drawer),
    new ForeignKeyProfile("txfk_batid"   , txn, txn.batchid    , batch),
    new ForeignKeyProfile("txfk_origtxn" , txn, txn.origtxnid  , txn),
  };

}
