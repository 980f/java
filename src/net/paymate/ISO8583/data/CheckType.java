// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ISO8583/data/CheckType.Enum]
package net.paymate.ISO8583.data;

import net.paymate.lang.TrueEnum;
//import net.paymate.util.TextList;

public class CheckType extends TrueEnum {
  public final static int Personal  =0;
  public final static int Payroll   =1;
  public final static int Government=2;
  public final static int ThirdParty=3;
  public final static int Business  =4;

  public int numValues(){ return 5; }
  private static final String[] myText = TrueEnum.nameVector(CheckType.class);
  protected final String[] getMyText() {
    return myText;
  }
  public static final CheckType Prop=new CheckType();
  public CheckType(){
    super();
  }
  public CheckType(int rawValue){
    super(rawValue);
  }
  public CheckType(String textValue){
    super(textValue);
  }
  public CheckType(CheckType rhs){
    this(rhs.Value());
  }
  public CheckType setto(CheckType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
