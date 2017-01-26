// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/RunTimeFormatRowEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class RunTimeFormatRowEnum extends TrueEnum {
  public final static int DFDataRow=0;

  public int numValues(){ return 1; }
  private static final String[ ] myText = TrueEnum.nameVector(RunTimeFormatRowEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final RunTimeFormatRowEnum Prop=new RunTimeFormatRowEnum();//for accessing class info
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
  public static RunTimeFormatRowEnum CopyOf(RunTimeFormatRowEnum rhs){//null-safe cloner
    return (rhs!=null)? new RunTimeFormatRowEnum(rhs) : new RunTimeFormatRowEnum();
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

