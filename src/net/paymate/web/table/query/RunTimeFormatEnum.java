// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/RunTimeFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class RunTimeFormatEnum extends TrueEnum {
  public final static int nameCol  =0;
  public final static int statusCol=1;

  public int numValues(){ return 2; }
  private static final TextList myText = TrueEnum.nameVector(RunTimeFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final RunTimeFormatEnum Prop=new RunTimeFormatEnum();
  public RunTimeFormatEnum(){
    super();
  }
  public RunTimeFormatEnum(int rawValue){
    super(rawValue);
  }
  public RunTimeFormatEnum(String textValue){
    super(textValue);
  }
  public RunTimeFormatEnum(RunTimeFormatEnum rhs){
    this(rhs.Value());
  }
  public RunTimeFormatEnum setto(RunTimeFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
