package net.paymate.ISO8583.factory;

/**
 * Title:        $Source: /cvs/src/net/paymate/ISO8583/factory/LegacyProtocol.java,v $
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      PayMate.net
 * @author PayMate.net
 * @version $Revision: 1.1 $
 */

import net.paymate.data.*;
import net.paymate.util.*;

public class LegacyProtocol extends Protocol {
  /* the bitmapped fields */
  private static FieldDef lookup[]={
    new FieldDef(32-1, new ContentType(ContentType.arbitrary),2, 99),
    new FieldDef(46-1, new ContentType(ContentType.arbitrary),3,999),
    new FieldDef(Field.PrimaryAccountNumber                ,  new ContentType(ContentType.decimal), 2, 19),
    new FieldDef(Field.ProcessingCode                      ,  new ContentType(ContentType.decimal), 0,6),
    new FieldDef(Field.TransactionAmount                   ,  new ContentType(ContentType.decimal), 0,12),//cents
    new FieldDef(Field.TransmissionDateTime                ,  new ContentType(ContentType.decimal), 0,10),//time10
    new FieldDef(Field.SystemTraceAuditNumber              ,  new ContentType(ContentType.decimal), 0,6),
    new FieldDef(Field.LocalTransactionTime                ,  new ContentType(ContentType.decimal), 0,6), //time6
    new FieldDef(Field.LocalTransactionDate                ,  new ContentType(ContentType.decimal), 0,4), //date
    new FieldDef(Field.ExpirationDate                      ,  new ContentType(ContentType.decimal), 0,4), //yearmonth
    new FieldDef(Field.SettlementDate                      ,  new ContentType(ContentType.decimal), 0,4), //date
    new FieldDef(Field.MerchantType                        ,  new ContentType(ContentType.arbitrary), 0,4), //SIC code...appenidx C someplace
    new FieldDef(Field.PointOfServiceEntryMode             ,  new ContentType(ContentType.decimal), 0,3),
    new FieldDef(Field.PointOfServiceConditionCode         ,  new ContentType(ContentType.decimal), 0,2),
    new FieldDef(Field.Track2Data                          ,  new ContentType(ContentType.arbitrary), 2,37),
    new FieldDef(Field.RetrievalReferenceNumber            ,  new ContentType(ContentType.alphanum), 0,12),
    new FieldDef(Field.AuthorizationIdentificationResponse ,  new ContentType(ContentType.alphanum), 0,6),
    new FieldDef(Field.ResponseCode                        ,  new ContentType(ContentType.decimal),0,2),
    new FieldDef(Field.CardAcceptorTerminalIdentification  ,  new ContentType(ContentType.arbitrary), 0,8),
    new FieldDef(Field.CardAcceptorIdentificationCode      ,  new ContentType(ContentType.arbitrary), 0,15),//store # cat "001"
    new FieldDef(Field.CardAcceptorNameLocation            ,  new ContentType(ContentType.arbitrary), 0,40),
    new FieldDef(Field.Track1Data                          ,  new ContentType(ContentType.arbitrary), 2,76),
    new FieldDef(Field.AdditionalDataPrivate               ,  new ContentType(ContentType.arbitrary),3,999),
    new FieldDef(Field.PersonalIdentificationNumberData    ,  new ContentType(ContentType.hex), 0,16),
    new FieldDef(Field.SecurityRelatedControlInformation   ,  new ContentType(ContentType.decimal), 0,16),
    new FieldDef(Field.AdditionalAmount                    ,  new ContentType(ContentType.alphanum), 3,120),
    new FieldDef(Field.AuthorizationLifeCycle              ,  new ContentType(ContentType.decimal), 0,3),
    new FieldDef(Field.PrivateDataForISP                   ,  new ContentType(ContentType.arbitrary), 3,99), //ms limit, iso says 999
    new FieldDef(Field.SettlementCode                      ,  new ContentType(ContentType.decimal), 0,1),
    new FieldDef(Field.NetworkManagementInformationCode    ,  new ContentType(ContentType.decimal), 0,3),
    new FieldDef(Field.TotalNumberCredits                  ,  new ContentType(ContentType.decimal), 0,10),
    new FieldDef(Field.TotalNumberCreditRevesals           ,  new ContentType(ContentType.decimal), 0,10),
    new FieldDef(Field.TotalNumberDebits                   ,  new ContentType(ContentType.decimal), 0,10),
    new FieldDef(Field.TotalNumberDebitReversals           ,  new ContentType(ContentType.decimal), 0,10),
    new FieldDef(Field.TotalNumberAuthorizations           ,  new ContentType(ContentType.decimal),0,10),
    new FieldDef(Field.TotalAmountCredits                  ,  new ContentType(ContentType.decimal),0,16), //big money
    new FieldDef(Field.TotalAmountCreditReversals          ,  new ContentType(ContentType.decimal),0,16),
    new FieldDef(Field.TotalAmountDebits                   ,  new ContentType(ContentType.decimal),0,16),
    new FieldDef(Field.TotalAmountDebitReversals           ,  new ContentType(ContentType.decimal),0,16),
    new FieldDef(Field.OriginalDataElements                ,  new ContentType(ContentType.decimal),0,42),
    new FieldDef(Field.MessageSecurityCode                 ,  new ContentType(ContentType.hex),0,16),
    new FieldDef(Field.NetSettlementAmount                 ,  new ContentType(ContentType.alphanum),0,17), //signed money
    new FieldDef(Field.ReceivingInstitutionIDCode          ,  new ContentType(ContentType.decimal),2,11),
    new FieldDef(Field.ANSIAdditionalTraceData             ,  new ContentType(ContentType.arbitrary),3,999),
    new FieldDef(Field.TransStartTime                      ,  new ContentType(ContentType.decimal),0,14), //longtime
    new FieldDef(Field.TransEndTime                        ,  new ContentType(ContentType.decimal),0,14),
    new FieldDef(Field.AuthorizationStartTime              ,  new ContentType(ContentType.decimal),0,14),
    new FieldDef(Field.AuthorizationEndTime                ,  new ContentType(ContentType.decimal),0,14),
    new FieldDef(Field.PaymentTypeCode                     ,  new ContentType(ContentType.alphanum),0,2),
    new FieldDef(Field.TransactionType                     ,  new ContentType(ContentType.alphanum),0,2),
    new FieldDef(Field.EventNumber                         ,  new ContentType(ContentType.alphanum),0,8),
    new FieldDef(Field.ServerName                          ,  new ContentType(ContentType.arbitrary),0,8),
    new FieldDef(Field.OriginalSTAN                        ,  new ContentType(ContentType.decimal),0,6),
    new FieldDef(Field.StandInIndicator                    ,  new ContentType(ContentType.alphanum),0,1),
    new FieldDef(Field.LogToDiskTimer                      ,  new ContentType(ContentType.decimal),0,14),//longtime
    new FieldDef(Field.BatchNumber                         ,  new ContentType(ContentType.decimal),0,6),
    new FieldDef(Field.ActionCode                          ,  new ContentType(ContentType.alphanum),0,1),
    new FieldDef(Field.DemoModeIndicator                   ,  new ContentType(ContentType.alphanum),0,1),
    new FieldDef(Field.TransGTRID                          ,  new ContentType(ContentType.decimal),0,12),
    new FieldDef(Field.OriginatingPID                      ,  new ContentType(ContentType.decimal),0,12),
    new FieldDef(Field.OriginateNode                       ,  new ContentType(ContentType.alphanum),0,8),
    new FieldDef(Field.CashbackAmount                      ,  new ContentType(ContentType.decimal),0,12),//cents
    new FieldDef(Field.EmployeeNumber                      ,  new ContentType(ContentType.alphanum),2,16),
    new FieldDef(Field.ControllerType                      ,  new ContentType(ContentType.decimal), 0,3),
    new FieldDef(Field.CheckType                           ,  new ContentType(ContentType.decimal), 0,1),
    new FieldDef(Field.MessageMap                          ,  new ContentType(ContentType.alphanum),2,99),
    new FieldDef(Field.ReconciliationType                  ,  new ContentType(ContentType.unknown),0,-1),
    new FieldDef(Field.IndividualTotals                    ,  new ContentType(ContentType.unknown),0,-1),
    new FieldDef(Field.KeyManagementInformation            ,  new ContentType(ContentType.alphanum),2,27), //packed field
    new FieldDef(Field.DriversLicense                      ,  new ContentType(ContentType.alphanum),2,99), //state code then free form
    new FieldDef(Field.TimeOutIndicator                    ,  new ContentType(ContentType.alphanum),0,1),
    new FieldDef(Field.LateResponseIndicator               ,  new ContentType(ContentType.alphanum),0,1),
    new FieldDef(Field.AuthorizerServer                    ,  new ContentType(ContentType.arbitrary),0,8),
    new FieldDef(Field.AuthorizerName                      ,  new ContentType(ContentType.arbitrary),0,12),
    new FieldDef(Field.EmployeePIN                         ,  new ContentType(ContentType.arbitrary),2,4),
    new FieldDef(Field.LogonTime                           ,  new ContentType(ContentType.unknown),0,-1),
    new FieldDef(Field.LogonDate                           ,  new ContentType(ContentType.unknown),0,-1),
    //the following should be decimal,0,9! logically, I only changed to decimal from arbutrary
    //the only legal variants are 4digits-4digits and 9 digits. all modern checks are the 9 digit format.
    new FieldDef(Field.CheckRouting                        ,  new ContentType(ContentType.decimal),2,9),
    new FieldDef(Field.CheckingAccountNumber               ,  new ContentType(ContentType.alphanum),2,19),
    new FieldDef(Field.CheckNumber                         ,  new ContentType(ContentType.decimal),2,5),
    new FieldDef(Field.SocialSecurityNumber                ,  new ContentType(ContentType.decimal),0,9),
    new FieldDef(Field.OtherCardNumber                     ,  new ContentType(ContentType.alphanum),2,28),
    new FieldDef(Field.IdentificationType                  ,  new ContentType(ContentType.purealpha),0,2),
    new FieldDef(Field.ResubmittalCount                    ,  new ContentType(ContentType.decimal),0,10),
    new FieldDef(Field.HostSettlementDate                  ,  new ContentType(ContentType.unknown),0,-1),
    new FieldDef(Field.HostProcessingCode                  ,  new ContentType(ContentType.unknown),0,-1),
    new FieldDef(Field.SettleInstID                        ,  new ContentType(ContentType.alphanum),0,4),
    new FieldDef(Field.StateCode                           ,  new ContentType(ContentType.purealpha),0,2),
    new FieldDef(Field.ZipCode                             ,  new ContentType(ContentType.decimal),2,10),
    new FieldDef(Field.HostResponseCode                    ,  new ContentType(ContentType.alphanum),0,2),
    new FieldDef(Field.MerchantID                          ,  new ContentType(ContentType.alphanum),0,15),
    new FieldDef(Field.StoreAuthData                       ,  new ContentType(ContentType.arbitrary),2,25),
    new FieldDef(Field.SwitchDateTime                      ,  new ContentType(ContentType.decimal),0,14),
    new FieldDef(Field.CourtesyCardNumber                  ,  new ContentType(ContentType.unknown),0,-1),
    new FieldDef(Field.StoreID                             ,  new ContentType(ContentType.decimal),0,4),
    new FieldDef(Field.ManualVoucherNumber                 ,  new ContentType(ContentType.arbitrary),2,28)
  };

  public LegacyProtocol() {
    super(lookup);
  }

  public int maxMaps(){
    return 3;
  }


}
//$Id: LegacyProtocol.java,v 1.1 2001/11/14 13:53:45 andyh Exp $