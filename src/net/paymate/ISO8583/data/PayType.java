// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ISO8583/data/PayType.Enum]
package net.paymate.ISO8583.data;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class PayType extends TrueEnum {
  public final static int Unknown=0;
  public final static int Credit =1;
  public final static int SVC    =2;
  public final static int Debit  =3;
  public final static int Check  =4;
  public final static int Cash   =5;

  public int numValues(){ return 6; }
  private static final TextList myText = TrueEnum.nameVector(PayType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final PayType Prop=new PayType();
  public PayType(){
    super();
  }
  public PayType(int rawValue){
    super(rawValue);
  }
  public PayType(String textValue){
    super(textValue);
  }
  public PayType(PayType rhs){
    this(rhs.Value());
  }
  public PayType setto(PayType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
