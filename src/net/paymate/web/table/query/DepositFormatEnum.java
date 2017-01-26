// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/DepositFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class DepositFormatEnum extends TrueEnum {
  public final static int terminalNameCol  =0;
  public final static int authorizerNameCol=1;
  public final static int tadCol           =2;
  public final static int authtermCol      =3;
  public final static int lastClosedTimeCol=4;
  public final static int txnCountCol      =5;
  public final static int txnTotalCol      =6;
  public final static int CSVCol           =7;

  public int numValues(){ return 8; }
  private static final String[ ] myText = TrueEnum.nameVector(DepositFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final DepositFormatEnum Prop=new DepositFormatEnum();//for accessing class info
  public DepositFormatEnum(){
    super();
  }
  public DepositFormatEnum(int rawValue){
    super(rawValue);
  }
  public DepositFormatEnum(String textValue){
    super(textValue);
  }
  public DepositFormatEnum(DepositFormatEnum rhs){
    this(rhs.Value());
  }
  public DepositFormatEnum setto(DepositFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static DepositFormatEnum CopyOf(DepositFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new DepositFormatEnum(rhs) : new DepositFormatEnum();
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

