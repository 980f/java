// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AuthAttemptFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class AuthAttemptFormatEnum extends TrueEnum {
  public final static int StartTimeCol=0;
  public final static int AuthCol     =1;
  public final static int RequestCol  =2;
  public final static int ResponseCol =3;
  public final static int TraceCol    =4;
  public final static int TxnCol      =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(AuthAttemptFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AuthAttemptFormatEnum Prop=new AuthAttemptFormatEnum();//for accessing class info
  public AuthAttemptFormatEnum(){
    super();
  }
  public AuthAttemptFormatEnum(int rawValue){
    super(rawValue);
  }
  public AuthAttemptFormatEnum(String textValue){
    super(textValue);
  }
  public AuthAttemptFormatEnum(AuthAttemptFormatEnum rhs){
    this(rhs.Value());
  }
  public AuthAttemptFormatEnum setto(AuthAttemptFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AuthAttemptFormatEnum CopyOf(AuthAttemptFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new AuthAttemptFormatEnum(rhs) : new AuthAttemptFormatEnum();
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

