// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ivicm/et1K/SignatureType.Enum]
package net.paymate.ivicm.et1K;

import net.paymate.lang.TrueEnum;

public class SignatureType extends TrueEnum {
  public final static int Hancock =0;
  public final static int NCRA    =1;
  public final static int Hypercom=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(SignatureType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SignatureType Prop=new SignatureType();//for accessing class info
  public SignatureType(){
    super();
  }
  public SignatureType(int rawValue){
    super(rawValue);
  }
  public SignatureType(String textValue){
    super(textValue);
  }
  public SignatureType(SignatureType rhs){
    this(rhs.Value());
  }
  public SignatureType setto(SignatureType rhs){
    setto(rhs.Value());
    return this;
  }
  public static SignatureType CopyOf(SignatureType rhs){//null-safe cloner
    return (rhs!=null)? new SignatureType(rhs) : new SignatureType();
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

