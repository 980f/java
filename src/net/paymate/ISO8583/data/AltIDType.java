// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ISO8583/data/AltIDType.Enum]
package net.paymate.ISO8583.data;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class AltIDType extends TrueEnum {
  public final static int AL=0;
  public final static int OT=1;
  public final static int PN=2;
  public final static int MR=3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(AltIDType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final AltIDType Prop=new AltIDType();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
