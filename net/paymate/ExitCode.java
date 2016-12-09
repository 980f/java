// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ExitCode.Enum]
package net.paymate;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ExitCode extends TrueEnum {
  public final static int Ok        =0;
  public final static int Halt      =1;
  public final static int WarmBoot  =2;
  public final static int UpgradeApp=3;
  public final static int UpgradeAll=4;
  public final static int StatusOn  =5;
  public final static int StatusOff =6;
  public final static int seven     =7;
  public final static int eight     =8;
  public final static int MainDied  =9;
  public final static int MainCaught=10;

  public int numValues(){ return 11; }
  private static final TextList myText = TrueEnum.nameVector(ExitCode.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ExitCode Prop=new ExitCode();
  public ExitCode(){
    super();
  }
  public ExitCode(int rawValue){
    super(rawValue);
  }
  public ExitCode(String textValue){
    super(textValue);
  }
  public ExitCode(ExitCode rhs){
    this(rhs.Value());
  }
  public ExitCode setto(ExitCode rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
