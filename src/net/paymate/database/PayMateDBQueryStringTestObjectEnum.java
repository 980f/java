// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/PayMateDBQueryStringTestObjectEnum.Enum]
package net.paymate.database;

import net.paymate.lang.TrueEnum;

public class PayMateDBQueryStringTestObjectEnum extends TrueEnum {
  public final static int BOOLEAN              =0;
  public final static int INT                  =1;
  public final static int STRING               =2;
  public final static int TIMEZONE             =3;
  public final static int LONG                 =4;
  public final static int FINANCIALREQUEST     =5;
  public final static int APPLIANCEID          =6;
  public final static int ASSOCIATEID          =7;
  public final static int AUTHATTEMPTID        =8;
  public final static int AUTHID               =9;
  public final static int BATCHID              =10;
  public final static int COLUMNPROFILE        =11;
  public final static int CONSTRAINT           =12;
  public final static int DRAWERID             =13;
  public final static int ENTERPRISEPERMISSIONS=14;
  public final static int ENTERPRISEID         =15;
  public final static int FOREIGNKEYPROFILE    =16;
  public final static int INDEXPROFILE         =17;
  public final static int PRIMARYKEYPROFILE    =18;
  public final static int STAN                 =19;
  public final static int STOREPERMISSIONS     =20;
  public final static int STOREID              =21;
  public final static int TERMAUTHID           =22;
  public final static int TERMINALID           =23;
  public final static int TIMERANGE            =24;
  public final static int TXNID                =25;
  public final static int UNIQUEID             =26;
  public final static int QUERYSTRING          =27;
  public final static int TABLEPROFILE         =28;
  public final static int TXNFILTER            =29;
  public final static int TXNROW               =30;
  public final static int EASYPROPERTIES       =31;
  public final static int EASYURLSTRING        =32;
  public final static int TEXTLIST             =33;
  public final static int UTC                  =34;
  public final static int PAYMENTREQUEST       =35;
  public final static int SERVICECFGID         =36;
  public final static int STOREAUTHID          =37;
  public final static int STOREACCESSID        =38;
  public final static int CLERKPRIVILEGES      =39;
  public final static int CARDID               =40;
  public final static int UNIQUEIDARRAY        =41;
  public final static int TERMINALIDARRAY      =42;

  public int numValues(){ return 43; }
  private static final String[ ] myText = TrueEnum.nameVector(PayMateDBQueryStringTestObjectEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final PayMateDBQueryStringTestObjectEnum Prop=new PayMateDBQueryStringTestObjectEnum();//for accessing class info
  public PayMateDBQueryStringTestObjectEnum(){
    super();
  }
  public PayMateDBQueryStringTestObjectEnum(int rawValue){
    super(rawValue);
  }
  public PayMateDBQueryStringTestObjectEnum(String textValue){
    super(textValue);
  }
  public PayMateDBQueryStringTestObjectEnum(PayMateDBQueryStringTestObjectEnum rhs){
    this(rhs.Value());
  }
  public PayMateDBQueryStringTestObjectEnum setto(PayMateDBQueryStringTestObjectEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static PayMateDBQueryStringTestObjectEnum CopyOf(PayMateDBQueryStringTestObjectEnum rhs){//null-safe cloner
    return (rhs!=null)? new PayMateDBQueryStringTestObjectEnum(rhs) : new PayMateDBQueryStringTestObjectEnum();
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

