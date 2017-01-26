// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/StatementsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class StatementsFormatEnum extends TrueEnum {
  public final static int numCol =0;
  public final static int nameCol=1;

  public int numValues(){ return 2; }
  private static final TextList myText = TrueEnum.nameVector(StatementsFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final StatementsFormatEnum Prop=new StatementsFormatEnum();
  public StatementsFormatEnum(){
    super();
  }
  public StatementsFormatEnum(int rawValue){
    super(rawValue);
  }
  public StatementsFormatEnum(String textValue){
    super(textValue);
  }
  public StatementsFormatEnum(StatementsFormatEnum rhs){
    this(rhs.Value());
  }
  public StatementsFormatEnum setto(StatementsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
