// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/util/ColumnJustification.Enum]
package net.paymate.util;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ColumnJustification extends TrueEnum {
  public final static int CENTERED =0;
  public final static int JUSTIFIED=1;
  public final static int PLAIN    =2;
  public final static int WINGED   =3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(ColumnJustification.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ColumnJustification Prop=new ColumnJustification();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
