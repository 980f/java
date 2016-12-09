// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/RunTimeFormatRowEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class RunTimeFormatRowEnum extends TrueEnum {
  public final static int DFDataRow=0;

  public int numValues(){ return 1; }
  private static final TextList myText = TrueEnum.nameVector(RunTimeFormatRowEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final RunTimeFormatRowEnum Prop=new RunTimeFormatRowEnum();
  public RunTimeFormatRowEnum(){
    super();
  }
  public RunTimeFormatRowEnum(int rawValue){
    super(rawValue);
  }
  public RunTimeFormatRowEnum(String textValue){
    super(textValue);
  }
  public RunTimeFormatRowEnum(RunTimeFormatRowEnum rhs){
    this(rhs.Value());
  }
  public RunTimeFormatRowEnum setto(RunTimeFormatRowEnum rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
