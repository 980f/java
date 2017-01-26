package net.paymate.authorizer.npc;

/**
 * Title:        $Source: /cvs/src/net/paymate/authorizer/npc/NPCConstants.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.5 $
 */

public interface NPCConstants {

//  /* package */
  static final int MAXREQUESTSIZE = 240;//247;//250; // +++ Need to implement: MAXSETTLESIZE, MAXPACKETSIZE, MAXRECORDSIZE, etc.
  static final String MONEYIMAGE07 = "######0"; // +++ use fstring
  static final String MONEYIMAGE08 = "#######0";
  static final String MONEYIMAGE09 = "########0";
  static final String MONEYIMAGE10 = "#########0";
  static final String TERMTYPE = "F.";
  // Upload Type. Valid codes are  [only 1 & 2 used as of 20020619]:
  static final String UploadTypeInquiryWithoutBatchClear = "1";
  static final String UploadTypeImmediateWithBatchClear  = "2";
  static final String UploadTypeTimedWithBatchClear      = "3"; // not really used right now
  // Template type [only retail used as of 20020619]
  static final String TemplateTypeRestaurant="203";
  static final String TemplateTypeRetail = "205";
  static final String TemplateTypeFinancial_PrivateLabel = "209";
  static final String TemplateTypeCrossReference = "211";
  static final String TemplateTypeTandE_AutoRental_Hotel = "212";
  static final String TemplateTypeDirectMarketing = "216";
  // TransactionType (++ finish this list):
  static final String TransactionTypeSale   = "1";
  static final String TransactionTypeCredit = "5";

  // Gateway ID
  static final String GatewayID = "1"; // always 1 for VISA
}
