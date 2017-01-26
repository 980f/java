// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/LogFileFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class LogFileFormatEnum extends TrueEnum {
  public final static int nameCol        =0;
  public final static int pendingLinesCol=1;
  public final static int statusCol      =2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(LogFileFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final LogFileFormatEnum Prop=new LogFileFormatEnum();//for accessing class info
  public LogFileFormatEnum(){
    super();
  }
  public LogFileFormatEnum(int rawValue){
    super(rawValue);
  }
  public LogFileFormatEnum(String textValue){
    super(textValue);
  }
  public LogFileFormatEnum(LogFileFormatEnum rhs){
    this(rhs.Value());
  }
  public LogFileFormatEnum setto(LogFileFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static LogFileFormatEnum CopyOf(LogFileFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new LogFileFormatEnum(rhs) : new LogFileFormatEnum();
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

