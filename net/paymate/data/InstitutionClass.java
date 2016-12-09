// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/InstitutionClass.Enum]
package net.paymate.data;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class InstitutionClass extends TrueEnum {
  public final static int CardIssuer          =0;
  public final static int DriversLicenseBureau=1;
  public final static int Paymate             =2;
  public final static int Other               =3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(InstitutionClass.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final InstitutionClass Prop=new InstitutionClass();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
