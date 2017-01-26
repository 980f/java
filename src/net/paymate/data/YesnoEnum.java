// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/YesnoEnum.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class YesnoEnum extends TrueEnum {
  public final static int Yes          =0;
  public final static int No           =1;
  public final static int NotApplicable=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(YesnoEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final YesnoEnum Prop=new YesnoEnum();//for accessing class info
  public YesnoEnum(){
    super();
  }
  public YesnoEnum(int rawValue){
    super(rawValue);
  }
  public YesnoEnum(String textValue){
    super(textValue);
  }
  public YesnoEnum(YesnoEnum rhs){
    this(rhs.Value());
  }
  public YesnoEnum setto(YesnoEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static YesnoEnum CopyOf(YesnoEnum rhs){//null-safe cloner
    return (rhs!=null)? new YesnoEnum(rhs) : new YesnoEnum();
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

