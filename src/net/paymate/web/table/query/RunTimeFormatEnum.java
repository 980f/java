// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/RunTimeFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class RunTimeFormatEnum extends TrueEnum {
  public final static int nameCol  =0;
  public final static int statusCol=1;

  public int numValues(){ return 2; }
  private static final String[ ] myText = TrueEnum.nameVector(RunTimeFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final RunTimeFormatEnum Prop=new RunTimeFormatEnum();//for accessing class info
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
  public static RunTimeFormatEnum CopyOf(RunTimeFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new RunTimeFormatEnum(rhs) : new RunTimeFormatEnum();
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

