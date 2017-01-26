// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/CheckType.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class CheckType extends TrueEnum {
  public final static int Personal  =0;
  public final static int Payroll   =1;
  public final static int Government=2;
  public final static int ThirdParty=3;
  public final static int Business  =4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(CheckType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final CheckType Prop=new CheckType();//for accessing class info
  public CheckType(){
    super();
  }
  public CheckType(int rawValue){
    super(rawValue);
  }
  public CheckType(String textValue){
    super(textValue);
  }
  public CheckType(CheckType rhs){
    this(rhs.Value());
  }
  public CheckType setto(CheckType rhs){
    setto(rhs.Value());
    return this;
  }
  public static CheckType CopyOf(CheckType rhs){//null-safe cloner
    return (rhs!=null)? new CheckType(rhs) : new CheckType();
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

