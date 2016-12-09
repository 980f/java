// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/color/ColorSchema.Enum]
package net.paymate.web.color;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

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
  private static final TextList myText = TrueEnum.nameVector(ColorSchema.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ColorSchema Prop=new ColorSchema();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
