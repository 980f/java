// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/LogFileFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class LogFileFormatEnum extends TrueEnum {
  public final static int nameCol        =0;
  public final static int pendingLinesCol=1;
  public final static int statusCol      =2;

  public int numValues(){ return 3; }
  private static final TextList myText = TrueEnum.nameVector(LogFileFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final LogFileFormatEnum Prop=new LogFileFormatEnum();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
