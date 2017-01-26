/* $Id: Field.java,v 1.14 2001/10/02 17:06:34 mattm Exp $ */
/**
  manually generated enumeration of ISO fields

  old spex used 1 based addressing, we are using a real
  programming language so we convert to 0 based via the explict -1's,

  */
package net.paymate.ISO8583.factory;
import  net.paymate.lang.TrueEnum;
import  net.paymate.util.TextList;

public class Field extends TrueEnum {

  public static final int PrimaryAccountNumber                =  2-1;
  public static final int ProcessingCode                      =  3-1;
  public static final int TransactionAmount                   =  4-1;
  public static final int TransmissionDateTime                =  7-1;
  public static final int SystemTraceAuditNumber              = 11-1;
  public static final int LocalTransactionTime                = 12-1;
  public static final int LocalTransactionDate                = 13-1;
  public static final int ExpirationDate                      = 14-1;
  public static final int SettlementDate                      = 15-1;
  public static final int MerchantType                        = 18-1;
  public static final int PointOfServiceEntryMode             = 22-1;
  public static final int PointOfServiceConditionCode         = 25-1;
  public static final int Field32                             = 32-1;
  public static final int Track2Data                          = 35-1;
  public static final int RetrievalReferenceNumber            = 37-1;
  public static final int AuthorizationIdentificationResponse = 38-1;
  public static final int ResponseCode                        = 39-1;
  public static final int CardAcceptorTerminalIdentification  = 41-1;
  public static final int CardAcceptorIdentificationCode      = 42-1;
  public static final int CardAcceptorNameLocation            = 43-1;
  public static final int Track1Data                          = 45-1;
  public static final int Field46                             = 46-1;
  public static final int AdditionalDataPrivate               = 48-1;
  public static final int PersonalIdentificationNumberData    = 52-1;
  public static final int SecurityRelatedControlInformation   = 53-1;
  public static final int AdditionalAmount                    = 54-1;
  public static final int AuthorizationLifeCycle              = 57-1;
  public static final int PrivateDataForISP                   = 63-1;
  public static final int SettlementCode                      = 66-1;
  public static final int NetworkManagementInformationCode    = 70-1;
  public static final int TotalNumberCredits                  = 74-1;
  public static final int TotalNumberCreditRevesals           = 75-1;
  public static final int TotalNumberDebits                   = 76-1;
  public static final int TotalNumberDebitReversals           = 77-1;
  public static final int TotalNumberAuthorizations           = 81-1;
  public static final int TotalAmountCredits                  = 86-1;
  public static final int TotalAmountCreditReversals          = 87-1;
  public static final int TotalAmountDebits                   = 88-1;
  public static final int TotalAmountDebitReversals           = 89-1;
  public static final int OriginalDataElements                = 90-1;
  public static final int MessageSecurityCode                 = 96-1;
  public static final int NetSettlementAmount                 = 97-1;
  public static final int ReceivingInstitutionIDCode          =100-1;
  public static final int ANSIAdditionalTraceData             =116-1;
  public static final int TransStartTime                      =130-1;
  public static final int TransEndTime                        =131-1;
  public static final int AuthorizationStartTime              =132-1;
  public static final int AuthorizationEndTime                =133-1;
  public static final int PaymentTypeCode                     =134-1;
  public static final int TransactionType                     =135-1;
  public static final int EventNumber                         =136-1;
  public static final int ServerName                          =137-1;
  public static final int OriginalSTAN                        =138-1;
  public static final int StandInIndicator                    =139-1;//DONT USE!
  public static final int LogToDiskTimer                      =140-1;
  public static final int BatchNumber                         =141-1;
  public static final int ActionCode                          =142-1;
  public static final int DemoModeIndicator                   =143-1;
  public static final int TransGTRID                          =144-1;
  public static final int OriginatingPID                      =145-1;
  public static final int OriginateNode                       =146-1;
  public static final int CashbackAmount                      =147-1;
  public static final int EmployeeNumber                      =148-1;
  public static final int ControllerType                      =149-1;
  public static final int CheckType                           =150-1;
  public static final int MessageMap                          =153-1;
  public static final int ReconciliationType                  =154-1;
  public static final int IndividualTotals                    =155-1;
  public static final int KeyManagementInformation            =156-1;
  public static final int DriversLicense                      =157-1;
  public static final int TimeOutIndicator                    =158-1;
  public static final int LateResponseIndicator               =159-1;
  public static final int AuthorizerServer                    =160-1;
  public static final int AuthorizerName                      =161-1;
  public static final int EmployeePIN                         =162-1;
  public static final int LogonTime                           =163-1;
  public static final int LogonDate                           =164-1;
  public static final int CheckRouting                        =165-1;
  public static final int CheckingAccountNumber               =166-1;
  public static final int CheckNumber                         =167-1;
  public static final int SocialSecurityNumber                =168-1;
  public static final int OtherCardNumber                     =169-1;
  public static final int IdentificationType                  =170-1;
  public static final int ResubmittalCount                    =171-1;
  public static final int HostSettlementDate                  =172-1;
  public static final int HostProcessingCode                  =173-1;
  public static final int SettleInstID                        =174-1;
  public static final int StateCode                           =175-1;
  public static final int ZipCode                             =176-1;
  public static final int HostResponseCode                    =177-1;
  public static final int MerchantID                          =178-1;
  public static final int StoreAuthData                       =179-1;
  public static final int SwitchDateTime                      =180-1;
  public static final int CourtesyCardNumber                  =181-1;
  public static final int StoreID                             =182-1;
  public static final int ManualVoucherNumber                 =183-1;

  public int numValues(){ return 192; }//quantity defined by bitmasks
  static final TextList myText = TrueEnum.nameVector(Field.class);
  protected final TextList getMyText() {
    return myText;
  }

}
//$Id: Field.java,v 1.14 2001/10/02 17:06:34 mattm Exp $
