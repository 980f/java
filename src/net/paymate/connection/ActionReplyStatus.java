// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/ActionReplyStatus.Enum]
package net.paymate.connection;

import net.paymate.lang.TrueEnum;

public class ActionReplyStatus extends TrueEnum {
  public final static int Success                             =0;
  public final static int SuccessfullyFaked                   =1;
  public final static int OriginalNotFound                    =2;
  public final static int CertifyFailed                       =3;
  public final static int ConnectFailed                       =4;
  public final static int NotInitiated                        =5;
  public final static int SocketTimedOut                      =6;
  public final static int SocketCantInit                      =7;
  public final static int UnknownException                    =8;
  public final static int ObjectStreamingException            =9;
  public final static int Unimplemented                       =10;
  public final static int OutdatedProgram                     =11;
  public final static int InvalidAppliance                    =12;
  public final static int InvalidTerminal                     =13;
  public final static int InvalidLogin                        =14;
  public final static int InsufficientDetail                  =15;
  public final static int InsufficientPriveleges              =16;
  public final static int GarbledRequest                      =17;
  public final static int UndefinedISOResponse                =18;
  public final static int ErrorFaking                         =19;
  public final static int IllegibleIsoMessageFormat           =20;
  public final static int GarbledReply                        =21;
  public final static int ServerError                         =22;
  public final static int DatabaseQueryError                  =23;
  public final static int FailureSeeErrors                    =24;
  public final static int UnableToCreateTransaction           =25;
  public final static int TxnNotFound                         =26;
  public final static int HostTimedOut                        =27;
  public final static int UnavailableDueToTxnSystemMaintenance=28;
  public final static int ReplyTimeout                        =29;
  public final static int TryAgainLater                       =30;

  public int numValues(){ return 31; }
  private static final String[ ] myText = TrueEnum.nameVector(ActionReplyStatus.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ActionReplyStatus Prop=new ActionReplyStatus();//for accessing class info
  public ActionReplyStatus(){
    super();
  }
  public ActionReplyStatus(int rawValue){
    super(rawValue);
  }
  public ActionReplyStatus(String textValue){
    super(textValue);
  }
  public ActionReplyStatus(ActionReplyStatus rhs){
    this(rhs.Value());
  }
  public ActionReplyStatus setto(ActionReplyStatus rhs){
    setto(rhs.Value());
    return this;
  }
  public static ActionReplyStatus CopyOf(ActionReplyStatus rhs){//null-safe cloner
    return (rhs!=null)? new ActionReplyStatus(rhs) : new ActionReplyStatus();
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

