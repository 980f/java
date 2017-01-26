// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ISO8583/data/TransferType.Enum]
package net.paymate.ISO8583.data;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class TransferType extends TrueEnum {
  public final static int Unknown =0;
  public final static int Sale    =1;
  public final static int Return  =2;
  public final static int Reversal=3;
  public final static int ReEntry =4;

  public int numValues(){ return 5; }
  private static final TextList myText = TrueEnum.nameVector(TransferType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TransferType Prop=new TransferType();
  public TransferType(){
    super();
  }
  public TransferType(int rawValue){
    super(rawValue);
  }
  public TransferType(String textValue){
    super(textValue);
  }
  public TransferType(TransferType rhs){
    this(rhs.Value());
  }
  public TransferType setto(TransferType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
