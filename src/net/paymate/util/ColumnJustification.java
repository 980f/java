// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/util/ColumnJustification.Enum]
package net.paymate.util;

import net.paymate.lang.TrueEnum;

public class ColumnJustification extends TrueEnum {
  public final static int CENTERED =0;
  public final static int JUSTIFIED=1;
  public final static int PLAIN    =2;
  public final static int WINGED   =3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(ColumnJustification.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ColumnJustification Prop=new ColumnJustification();//for accessing class info
  public ColumnJustification(){
    super();
  }
  public ColumnJustification(int rawValue){
    super(rawValue);
  }
  public ColumnJustification(String textValue){
    super(textValue);
  }
  public ColumnJustification(ColumnJustification rhs){
    this(rhs.Value());
  }
  public ColumnJustification setto(ColumnJustification rhs){
    setto(rhs.Value());
    return this;
  }
  public static ColumnJustification CopyOf(ColumnJustification rhs){//null-safe cloner
    return (rhs!=null)? new ColumnJustification(rhs) : new ColumnJustification();
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

