// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/ours/table/PayMateTableEnum.Enum]
package net.paymate.database.ours.table;

import net.paymate.lang.TrueEnum;

public class PayMateTableEnum extends TrueEnum {
  public final static int appliance    =0;
  public final static int applnetstatus=1;
  public final static int applpgmstatus=2;
  public final static int associate    =3;
  public final static int authattempt  =4;
  public final static int authorizer   =5;
  public final static int batch        =6;
  public final static int card         =7;
  public final static int drawer       =8;
  public final static int enterprise   =9;
  public final static int servicecfg   =10;
  public final static int storeaccess  =11;
  public final static int storeauth    =12;
  public final static int store        =13;
  public final static int termauth     =14;
  public final static int terminal     =15;
  public final static int txn          =16;

  public int numValues(){ return 17; }
  private static final String[ ] myText = TrueEnum.nameVector(PayMateTableEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final PayMateTableEnum Prop=new PayMateTableEnum();//for accessing class info
  public PayMateTableEnum(){
    super();
  }
  public PayMateTableEnum(int rawValue){
    super(rawValue);
  }
  public PayMateTableEnum(String textValue){
    super(textValue);
  }
  public PayMateTableEnum(PayMateTableEnum rhs){
    this(rhs.Value());
  }
  public PayMateTableEnum setto(PayMateTableEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static PayMateTableEnum CopyOf(PayMateTableEnum rhs){//null-safe cloner
    return (rhs!=null)? new PayMateTableEnum(rhs) : new PayMateTableEnum();
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

