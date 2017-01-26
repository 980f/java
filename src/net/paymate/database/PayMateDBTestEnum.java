// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/PayMateDBTestEnum.Enum]
package net.paymate.database;

import net.paymate.lang.TrueEnum;

public class PayMateDBTestEnum extends TrueEnum {
  public final static int genCreateStoreAccess_UniqueId_UniqueId_ClerkPrivileges_UniqueId                   =0;
  public final static int needsAuth                                                                         =1;
  public final static int openStandins                                                                      =2;
  public final static int whichTxns_boolean_boolean_boolean                                                 =3;
  public final static int genApplianceForTerminal_Terminalid                                                =4;
  public final static int genAuthAttempt_AuthattemptId                                                      =5;
  public final static int genAuthAttempt_Txnid                                                              =6;
  public final static int genSelectTxnSubquery                                                              =7;
  public final static int genWhichTxnSubqueryInner_Terminalid_boolean_QueryString                           =8;
  public final static int genWhichTxnsSubquery_Batchid_TermAuthid_boolean                                   =9;
  public final static int genWhichTxnsSubquery_Drawerid_Terminalid_boolean                                  =10;
  public final static int batchBaseQuery                                                                    =11;
  public final static int drawerBaseQuery                                                                   =12;
  public final static int genPaymentTypeFromCardNo_String                                                   =13;
  public final static int whereServiceParam_String_String                                                   =14;
  public final static int genAuthIdFromName_String                                                          =15;
  public final static int idDistinct_TableProfile                                                           =16;
  public final static int genVacuumDatabase_boolean_boolean_boolean                                         =17;
  public final static int whereIsTrue_ColumnProfile                                                         =18;
  public final static int whereNot_ColumnProfile                                                            =19;
  public final static int getTableArray                                                                     =20;
  public final static int genApplianceId_String                                                             =21;
  public final static int genid_TableProfile                                                                =22;
  public final static int genCreateStore_UniqueId_String_UniqueId                                           =23;
  public final static int genCreateAssociate_UniqueId_String_String_UniqueId                                =24;
  public final static int genCreateAppliance_String_UniqueId_UniqueId                                       =25;
  public final static int genDatabaseAge_String                                                             =26;
  public final static int genStatsRowLevel                                                                  =27;
  public final static int genTablePages_TableProfile                                                        =28;
  public final static int genTableStats_TableProfile                                                        =29;
  public final static int genUpdateRecord_TableProfile_UniqueId_EasyProperties                              =30;
  public final static int genSelectRecord_TableProfile_UniqueId                                             =31;
  public final static int genSelectNextVal_ColumnProfile                                                    =32;
  public final static int genCreateStoreAuth_Authid_String_String_int_String_UniqueId_Authid_String_UniqueId=33;
  public final static int genAllStoreAuths_UniqueId_                                                        =34;
  public final static int genVacuumAnalyzeDatabase_boolean_boolean_boolean_boolean                          =35;
  public final static int genVacuum_TableProfile_boolean_boolean_boolean                                    =36;
  public final static int genBinEntry_int                                                                   =37;
  public final static int genCreateCard_EasyProperties_UniqueId                                             =38;
  public final static int genMaxId_ColumnProfile                                                            =39;
  public final static int genDupCheck_String                                                                =40;
  public final static int genBatchTxnCounts_Batchid                                                         =41;
  public final static int genTermsInfoForStores_UniqueId_Authid                                             =42;
  public final static int genTermBatchReport_TextList_TimeRange                                             =43;
  public final static int genFullAuthStore_Authid_UniqueId                                                  =44;
  public final static int genTermAuthPendingTotal_TermAuthid                                                =45;
  public final static int genUsedTtPtIn_Authid_UniqueId_TimeRange                                           =46;
  public final static int genTermAuthLastSubmit_TermAuthid                                                  =47;
  public final static int genLastAutoBatchQuery_TextList                                                    =48;
  public final static int genLastAutoDrawerQuery_Terminalid_                                                =49;
  public final static int genTerminalPendingTotal_Terminalid                                                =50;
  public final static int genTerminalidsForStore_UniqueId                                                   =51;
  public final static int genTerminalsForStore_UniqueId                                                     =52;
  public final static int genTerminalPendingRow_Terminalid                                                  =53;
  public final static int genApplianceTerminalsQuery_UniqueId                                               =54;
  public final static int genApplianceRowQuery_UniqueId                                                     =55;
  public final static int genTerminalsforAppliance_UniqueId                                                 =56;
  public final static int genFindTransactionsBy_TxnFilter_UniqueId                                          =57;
  public final static int genFindExactly_Terminalid_PaymentRequest                                          =58;
  public final static int genFindVoidForOriginal_Txnid                                                      =59;
  public final static int AuthAttempt_UniqueId                                                              =60;
  public final static int genTxnid_Terminalid_String                                                        =61;
  public final static int genTxnid_Terminalid_STAN                                                          =62;
  public final static int genTransactionFromQuery_Terminalid_String                                         =63;
  public final static int genTxnsForTime_String                                                             =64;
  public final static int genPendingStartTimeQuery_Terminalid                                               =65;
  public final static int genSetDrawerTotals_Drawerid_int_int                                               =66;
  public final static int genGetDrawerTotals_Drawerid                                                       =67;
  public final static int genGetDrawerableTxns_Terminalid                                                   =68;
  public final static int genStorePayInst_UniqueId                                                          =69;
  public final static int genTerminalForTxnid_Txnid                                                         =70;
  public final static int genTerminalAndAuthForTermauth_TermAuthid                                          =71;
  public final static int genTerminalAndAuthForBatch_Batchid                                                =72;
  public final static int genGetBatchNumberValue_Authid_Terminalid                                          =73;
  public final static int genUpdateTermauthInfo_TermAuthid_int_int                                          =74;
  public final static int genUpdateBatchStatus_Batchid_String_String                                        =75;
  public final static int genGetBatch_Batchid                                                               =76;
  public final static int genGetBatchableTxns_Terminalid_Authid                                             =77;
  public final static int genUpdateBatchseq_Batchid_int                                                     =78;
  public final static int genDeleteBatch_Batchid                                                            =79;
  public final static int forTrantime_UTC                                                                   =80;
  public final static int forTrantime_long                                                                  =81;
  public final static int genTermAuth_Terminalid_Authid                                                     =82;
  public final static int genFullBatchQuery_Batchid                                                         =83;
  public final static int genStoreBatchesQuery_UniqueId_boolean_TimeRange                                   =84;
  public final static int genSetBatchTotals_Batchid_int_int                                                 =85;
  public final static int genSetTxnBatchid_Txnid_Batchid                                                    =86;
  public final static int genTxnidsForBatchid_Batchid                                                       =87;
  public final static int genSetDrawerid_Txnid_Drawerid                                                     =88;
  public final static int genTerminalidFromDrawerid_Drawerid                                                =89;
  public final static int genBookmarkQuery_Drawerid                                                         =90;
  public final static int genMostRecentStoreBatch_UniqueId                                                  =91;
  public final static int tranUTC_String                                                                    =92;
  public final static int genMostRecentStoreDrawer_UniqueId                                                 =93;
  public final static int genStoreDrawersQuery_UniqueId_TimeRange                                           =94;
  public final static int genBatchQuery_TermAuthid                                                          =95;
  public final static int genBatchQuery_Batchid                                                             =96;
  public final static int genDrawerQuery_Drawerid_Terminalid_boolean                                        =97;
  public final static int genPendingTermAuths_UniqueId                                                      =98;
  public final static int genAssociateIdByLogin_String_UniqueId                                             =99;
  public final static int genStoreAuths_UniqueId                                                            =100;
  public final static int genStoresQuery_UniqueId                                                           =101;
  public final static int genSetTxnSequence_Txnid_int                                                       =102;
  public final static int genSetSequence_Authid_Terminalid_int                                              =103;
  public final static int genGetSequence_Authid_Terminalid                                                  =104;
  public final static int genToAuth_Authid                                                                  =105;
  public final static int genStampVoidTxn_Txnid_String                                                      =106;
  public final static int genStampAuthAttemptTxnidOnly_AuthattemptId_Txnid                                  =107;
  public final static int genStampAuthAttemptDone_AuthattemptId_Txnid_EasyUrlString                         =108;
  public final static int genDefaultAuthidForTerminal_Terminalid                                            =109;
  public final static int genAuthIdsForStore_UniqueId                                                       =110;
  public final static int genAuthIdsForTerminal_Terminalid                                                  =111;
  public final static int genUpdateServiceParam_String_String_String                                        =112;
  public final static int genServiceParam_String_String                                                     =113;
  public final static int genServiceParamNames_String                                                       =114;
  public final static int genAuths                                                                          =115;
  public final static int genAuthAttempts_Terminalid_TimeRange                                              =116;
  public final static int genFindTransactionBy_Terminalid_UTC                                               =117;
  public final static int genAssociateidForAssociateLoginname_String                                        =118;
  public final static int genStoreForTerminal_Terminalid                                                    =119;
  public final static int genAssociateId_String_String_UniqueId                                             =120;
  public final static int genTtlStandinsForTerminal_Terminalid                                              =121;
  public final static int genMerchantIds_Authid_UniqueId                                                    =122;
  public final static int genProcInfoFor_Terminalid_Authid_UniqueId_boolean                                 =123;
  public final static int genTermAuthIds_Terminalid                                                         =124;
  public final static int genStoreAuthInfo_UniqueId_String_String                                           =125;
  public final static int genStoreAccessesByStore_UniqueId                                                  =126;
  public final static int genStoreAccessesByAssoc_UniqueId                                                  =127;
  public final static int genStoreAccessid_UniqueId_UniqueId                                                =128;
  public final static int genTermAuths_Terminalid                                                           =129;
  public final static int genEnterpriseIdsbyEnabledName                                                     =130;
  public final static int Now                                                                               =131;
  public final static int genCreateTable_TableProfile                                                       =132;
  public final static int genRenameColumn_String_String_String                                              =133;
  public final static int genDropConstraint_TableProfile_Constraint                                         =134;
  public final static int genAddForeignKeyConstraint_ForeignKeyProfile                                      =135;
  public final static int genAddPrimaryKeyConstraint_PrimaryKeyProfile                                      =136;
  public final static int genCreateIndex_IndexProfile                                                       =137;
  public final static int genChangeFieldDefault_ColumnProfile                                               =138;
  public final static int genChangeFieldNullable_ColumnProfile                                              =139;
  public final static int genAddField_ColumnProfile                                                         =140;
  public final static int genDropTable_String                                                               =141;
  public final static int genDropIndex_String                                                               =142;
  public final static int genDropField_ColumnProfile                                                        =143;
  public final static int filter_TxnFilter                                                                  =144;
  public final static int isReadOnly_String                                                                 =145;
  public final static int class__String                                                                     =146;
  public final static int setProperty_EasyProperties_ColumnProfile_long                                     =147;
  public final static int setProperty_EasyProperties_ColumnProfile_int                                      =148;
  public final static int setProperty_EasyProperties_ColumnProfile_UniqueId                                 =149;
  public final static int setProperty_EasyProperties_ColumnProfile_boolean                                  =150;
  public final static int setProperty_EasyProperties_ColumnProfile_String                                   =151;
  public final static int genCreateTermauth_Authid_Terminalid_int_String_int_UniqueId                       =152;
  public final static int genCreateTerminal_String_String_UniqueId_boolean_UniqueId_UniqueId                =153;
  public final static int genCreateCloseDrawer_Terminalid_String_UniqueId_UniqueId_boolean                  =154;
  public final static int genCreateBatch_TermAuthid_String_int_UniqueId_boolean                             =155;
  public final static int genCreateServiceParam_String_String_String_UniqueId                               =156;
  public final static int genCreateAuthAttempt_Txnid_Authid_String_Terminalid_UniqueId                      =157;
  public final static int genCreateTxn_EasyProperties_UniqueId                                              =158;
  public final static int genLastStoreTxn_UniqueId                                                          =159;
  public final static int genStoreTxnCounts                                                                 =160;
  public final static int genStoreidsByAscTxncount                                                          =161;

  public int numValues(){ return 162; }
  private static final String[ ] myText = TrueEnum.nameVector(PayMateDBTestEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final PayMateDBTestEnum Prop=new PayMateDBTestEnum();//for accessing class info
  public PayMateDBTestEnum(){
    super();
  }
  public PayMateDBTestEnum(int rawValue){
    super(rawValue);
  }
  public PayMateDBTestEnum(String textValue){
    super(textValue);
  }
  public PayMateDBTestEnum(PayMateDBTestEnum rhs){
    this(rhs.Value());
  }
  public PayMateDBTestEnum setto(PayMateDBTestEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static PayMateDBTestEnum CopyOf(PayMateDBTestEnum rhs){//null-safe cloner
    return (rhs!=null)? new PayMateDBTestEnum(rhs) : new PayMateDBTestEnum();
  }
/** @return whether it was invalid */
  public boolean AssureValid(int defaultValue){//setto only if invalid
    if( ! isLegal() ){
       setto(defaultValue);
       return true;
    } else {
       return false;
    }
  }

}

