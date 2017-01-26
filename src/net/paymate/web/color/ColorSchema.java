// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/color/ColorSchema.Enum]
package net.paymate.web.color;

import net.paymate.lang.TrueEnum;

public class ColorSchema extends TrueEnum {
  public final static int PLAIN      =0;
  public final static int SIMPLE     =1;
  public final static int MONOCHROME =2;
  public final static int TEAL       =3;
  public final static int PLUM       =4;
  public final static int BRONZE     =5;
  public final static int MIDNIGHT   =6;
  public final static int TRANQUILITY=7;
  public final static int MONEY      =8;
  public final static int HEAT       =9;

  public int numValues(){ return 10; }
  private static final String[ ] myText = TrueEnum.nameVector(ColorSchema.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ColorSchema Prop=new ColorSchema();//for accessing class info
  public ColorSchema(){
    super();
  }
  public ColorSchema(int rawValue){
    super(rawValue);
  }
  public ColorSchema(String textValue){
    super(textValue);
  }
  public ColorSchema(ColorSchema rhs){
    this(rhs.Value());
  }
  public ColorSchema setto(ColorSchema rhs){
    setto(rhs.Value());
    return this;
  }
  public static ColorSchema CopyOf(ColorSchema rhs){//null-safe cloner
    return (rhs!=null)? new ColorSchema(rhs) : new ColorSchema();
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

