// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/AltIDType.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class AltIDType extends TrueEnum {
  public final static int AL=0;
  public final static int OT=1;
  public final static int PN=2;
  public final static int MR=3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(AltIDType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AltIDType Prop=new AltIDType();//for accessing class info
  public AltIDType(){
    super();
  }
  public AltIDType(int rawValue){
    super(rawValue);
  }
  public AltIDType(String textValue){
    super(textValue);
  }
  public AltIDType(AltIDType rhs){
    this(rhs.Value());
  }
  public AltIDType setto(AltIDType rhs){
    setto(rhs.Value());
    return this;
  }
  public static AltIDType CopyOf(AltIDType rhs){//null-safe cloner
    return (rhs!=null)? new AltIDType(rhs) : new AltIDType();
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

