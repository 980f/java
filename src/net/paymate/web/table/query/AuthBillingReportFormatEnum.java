// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AuthBillingReportFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class AuthBillingReportFormatEnum extends TrueEnum {
  public final static int AuthTermidCol  =0;
  public final static int TerminalNameCol=1;
  public final static int BatchTimeCol   =2;
  public final static int BatchNumberCol =3;
  public final static int AuthrespmsgCol =4;
  public final static int TotalCol       =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(AuthBillingReportFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AuthBillingReportFormatEnum Prop=new AuthBillingReportFormatEnum();//for accessing class info
  public AuthBillingReportFormatEnum(){
    super();
  }
  public AuthBillingReportFormatEnum(int rawValue){
    super(rawValue);
  }
  public AuthBillingReportFormatEnum(String textValue){
    super(textValue);
  }
  public AuthBillingReportFormatEnum(AuthBillingReportFormatEnum rhs){
    this(rhs.Value());
  }
  public AuthBillingReportFormatEnum setto(AuthBillingReportFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AuthBillingReportFormatEnum CopyOf(AuthBillingReportFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new AuthBillingReportFormatEnum(rhs) : new AuthBillingReportFormatEnum();
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

