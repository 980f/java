// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/SpecialOp.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class SpecialOp extends TrueEnum {
  public final static int List   =0;
  public final static int PreAuth=1;
  public final static int Force  =2;
  public final static int Adjust =3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(SpecialOp.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SpecialOp Prop=new SpecialOp();//for accessing class info
  public SpecialOp(){
    super();
  }
  public SpecialOp(int rawValue){
    super(rawValue);
  }
  public SpecialOp(String textValue){
    super(textValue);
  }
  public SpecialOp(SpecialOp rhs){
    this(rhs.Value());
  }
  public SpecialOp setto(SpecialOp rhs){
    setto(rhs.Value());
    return this;
  }
  public static SpecialOp CopyOf(SpecialOp rhs){//null-safe cloner
    return (rhs!=null)? new SpecialOp(rhs) : new SpecialOp();
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

