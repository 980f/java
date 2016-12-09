// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/ActionReplyStatus.Enum]
package net.paymate.connection;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ActionReplyStatus extends TrueEnum {
  public final static int Success                             =0;
  public final static int CertifyFailed                       =1;
  public final static int ConnectFailed                       =2;
  public final static int NotInitiated                        =3;
  public final static int SocketTimedOut                      =4;
  public final static int SocketCantInit                      =5;
  public final static int UnknownException                    =6;
  public final static int ObjectStreamingException            =7;
  public final static int Unimplemented                       =8;
  public final static int OutdatedProgram                     =9;
  public final static int InvalidAppliance                    =10;
  public final static int InvalidTerminal                     =11;
  public final static int InvalidLogin                        =12;
  public final static int InsufficientPriveleges              =13;
  public final static int GarbledRequest                      =14;
  public final static int UndefinedISOResponse                =15;
  public final static int SuccessfullyFaked                   =16;
  public final static int ErrorFaking                         =17;
  public final static int IllegibleIsoMessageFormat           =18;
  public final static int GarbledReply                        =19;
  public final static int ServerError                         =20;
  public final static int DatabaseQueryError                  =21;
  public final static int FailureSeeErrors                    =22;
  public final static int UnableToCreateTransaction           =23;
  public final static int TxnNotFound                         =24;
  public final static int HostTimedOut                        =25;
  public final static int UnavailableDueToTxnSystemMaintenance=26;
  public final static int ReplyTimeout                        =27;
  public final static int TryAgainLater                       =28;

  public int numValues(){ return 29; }
  private static final TextList myText = TrueEnum.nameVector(ActionReplyStatus.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ActionReplyStatus Prop=new ActionReplyStatus();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
