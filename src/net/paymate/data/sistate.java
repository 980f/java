// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/sistate.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class sistate extends TrueEnum {
  public final static int not     =0;
  public final static int atClient=1;
  public final static int atServer=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(sistate.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final sistate Prop=new sistate();//for accessing class info
  public sistate(){
    super();
  }
  public sistate(int rawValue){
    super(rawValue);
  }
  public sistate(String textValue){
    super(textValue);
  }
  public sistate(sistate rhs){
    this(rhs.Value());
  }
  public sistate setto(sistate rhs){
    setto(rhs.Value());
    return this;
  }
  public static sistate CopyOf(sistate rhs){//null-safe cloner
    return (rhs!=null)? new sistate(rhs) : new sistate();
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

