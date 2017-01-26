// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ISO8583/data/TransactionType.Enum]
package net.paymate.ISO8583.data;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class TransactionType extends TrueEnum {
  public final static int CK=0;
  public final static int CC=1;
  public final static int CR=2;
  public final static int DB=3;
  public final static int EB=4;
  public final static int EC=5;
  public final static int GC=6;
  public final static int FA=7;
  public final static int FU=8;

  public int numValues(){ return 9; }
  private static final TextList myText = TrueEnum.nameVector(TransactionType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TransactionType Prop=new TransactionType();
  public TransactionType(){
    super();
  }
  public TransactionType(int rawValue){
    super(rawValue);
  }
  public TransactionType(String textValue){
    super(textValue);
  }
  public TransactionType(TransactionType rhs){
    this(rhs.Value());
  }
  public TransactionType setto(TransactionType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
