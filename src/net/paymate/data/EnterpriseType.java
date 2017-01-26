// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/EnterpriseType.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class EnterpriseType extends TrueEnum {
  public final static int S=0;
  public final static int P=1;
  public final static int A=2;
  public final static int M=3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(EnterpriseType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final EnterpriseType Prop=new EnterpriseType();//for accessing class info
  public EnterpriseType(){
    super();
  }
  public EnterpriseType(int rawValue){
    super(rawValue);
  }
  public EnterpriseType(String textValue){
    super(textValue);
  }
  public EnterpriseType(EnterpriseType rhs){
    this(rhs.Value());
  }
  public EnterpriseType setto(EnterpriseType rhs){
    setto(rhs.Value());
    return this;
  }
  public static EnterpriseType CopyOf(EnterpriseType rhs){//null-safe cloner
    return (rhs!=null)? new EnterpriseType(rhs) : new EnterpriseType();
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

