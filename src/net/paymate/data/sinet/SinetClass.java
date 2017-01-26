// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/sinet/SinetClass.Enum]
package net.paymate.data.sinet;

import net.paymate.lang.TrueEnum;

public class SinetClass extends TrueEnum {
  public final static int Enterprise   =0;
  public final static int Store        =1;
  public final static int Associate    =2;
  public final static int Appliance    =3;
  public final static int Terminal     =4;
  public final static int ApplPgmStatus=5;
  public final static int ApplNetStatus=6;

  public int numValues(){ return 7; }
  private static final String[ ] myText = TrueEnum.nameVector(SinetClass.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SinetClass Prop=new SinetClass();//for accessing class info
  public SinetClass(){
    super();
  }
  public SinetClass(int rawValue){
    super(rawValue);
  }
  public SinetClass(String textValue){
    super(textValue);
  }
  public SinetClass(SinetClass rhs){
    this(rhs.Value());
  }
  public SinetClass setto(SinetClass rhs){
    setto(rhs.Value());
    return this;
  }
  public static SinetClass CopyOf(SinetClass rhs){//null-safe cloner
    return (rhs!=null)? new SinetClass(rhs) : new SinetClass();
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

