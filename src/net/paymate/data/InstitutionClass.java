// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/InstitutionClass.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class InstitutionClass extends TrueEnum {
  public final static int CardIssuer          =0;
  public final static int DriversLicenseBureau=1;
  public final static int Paymate             =2;
  public final static int Other               =3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(InstitutionClass.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final InstitutionClass Prop=new InstitutionClass();//for accessing class info
  public InstitutionClass(){
    super();
  }
  public InstitutionClass(int rawValue){
    super(rawValue);
  }
  public InstitutionClass(String textValue){
    super(textValue);
  }
  public InstitutionClass(InstitutionClass rhs){
    this(rhs.Value());
  }
  public InstitutionClass setto(InstitutionClass rhs){
    setto(rhs.Value());
    return this;
  }
  public static InstitutionClass CopyOf(InstitutionClass rhs){//null-safe cloner
    return (rhs!=null)? new InstitutionClass(rhs) : new InstitutionClass();
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

