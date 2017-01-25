// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/UnsettledTransactionFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class UnsettledTransactionFormatEnum extends TrueEnum {
  public final static int TimeCol    =0;
  public final static int TermCol    =1;
  public final static int StanCol    =2;
  public final static int SiCol      =3;
  public final static int StatusCol  =4;
  public final static int ApprovalCol=5;
  public final static int AcctNumCol =6;
  public final static int ExpDateCol =7;
  public final static int SaleCol    =8;
  public final static int ReturnCol  =9;
  public final static int SumCol     =10;

  public int numValues(){ return 11; }
  private static final TextList myText = TrueEnum.nameVector(UnsettledTransactionFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final UnsettledTransactionFormatEnum Prop=new UnsettledTransactionFormatEnum();
  public UnsettledTransactionFormatEnum(){
    super();
  }
  public UnsettledTransactionFormatEnum(int rawValue){
    super(rawValue);
  }
  public UnsettledTransactionFormatEnum(String textValue){
    super(textValue);
  }
  public UnsettledTransactionFormatEnum(UnsettledTransactionFormatEnum rhs){
    this(rhs.Value());
  }
  public UnsettledTransactionFormatEnum setto(UnsettledTransactionFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
