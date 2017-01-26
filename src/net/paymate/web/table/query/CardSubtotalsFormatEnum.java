// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/CardSubtotalsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class CardSubtotalsFormatEnum extends TrueEnum {
  public final static int PayTypeCol     =0;
  public final static int InstitutionCol =1;
  public final static int InstSubCountCol=2;
  public final static int InstSubTotalCol=3;
  public final static int CountCol       =4;
  public final static int SumCol         =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(CardSubtotalsFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final CardSubtotalsFormatEnum Prop=new CardSubtotalsFormatEnum();//for accessing class info
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
  public static CardSubtotalsFormatEnum CopyOf(CardSubtotalsFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new CardSubtotalsFormatEnum(rhs) : new CardSubtotalsFormatEnum();
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

