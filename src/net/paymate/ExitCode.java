// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ExitCode.Enum]
package net.paymate;

import net.paymate.lang.TrueEnum;

public class ExitCode extends TrueEnum {
  public final static int Ok         =0;
  public final static int LaunchError=1;
  public final static int WarmBoot   =2;
  public final static int UpgradeApp =3;
  public final static int UpgradeAll =4;
  public final static int StatusOn   =5;
  public final static int StatusOff  =6;
  public final static int seven      =7;
  public final static int eight      =8;
  public final static int Halt       =9;
  public final static int MainDied   =10;
  public final static int MainCaught =11;

  public int numValues(){ return 12; }
  private static final String[ ] myText = TrueEnum.nameVector(ExitCode.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ExitCode Prop=new ExitCode();//for accessing class info
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
  public static ExitCode CopyOf(ExitCode rhs){//null-safe cloner
    return (rhs!=null)? new ExitCode(rhs) : new ExitCode();
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

