// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/CardSubtotalsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class CardSubtotalsFormatEnum extends TrueEnum {
  public final static int CardTypeCol=0;
  public final static int CountCol   =1;
  public final static int SumCol     =2;

  public int numValues(){ return 3; }
  private static final TextList myText = TrueEnum.nameVector(CardSubtotalsFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final CardSubtotalsFormatEnum Prop=new CardSubtotalsFormatEnum();
  public CardSubtotalsFormatEnum(){
    super();
  }
  public CardSubtotalsFormatEnum(int rawValue){
    super(rawValue);
  }
  public CardSubtotalsFormatEnum(String textValue){
    super(textValue);
  }
  public CardSubtotalsFormatEnum(CardSubtotalsFormatEnum rhs){
    this(rhs.Value());
  }
  public CardSubtotalsFormatEnum setto(CardSubtotalsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
