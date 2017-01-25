// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/util/LogLevelEnum.Enum]
package net.paymate.util;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class LogLevelEnum extends TrueEnum {
  public final static int VERBOSE=0;
  public final static int WARNING=1;
  public final static int ERROR  =2;
  public final static int OFF    =3;

  public int numValues(){ return 4; }
  static final TextList myText = TrueEnum.nameVector(LogLevelEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final LogLevelEnum Prop=new LogLevelEnum();
  public LogLevelEnum(){
    super();
  }
  public LogLevelEnum(int rawValue){
    super(rawValue);
  }
  public LogLevelEnum(String textValue){
    super(textValue);
  }
  public LogLevelEnum(LogLevelEnum rhs){
    this(rhs.Value());
  }

}
