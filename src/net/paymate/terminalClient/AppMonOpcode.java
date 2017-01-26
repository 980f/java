// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/AppMonOpcode.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class AppMonOpcode extends TrueEnum {
  public final static int terminalOp  =0;
  public final static int standin     =1;
  public final static int statusclient=2;
  public final static int logging     =3;
  public final static int alarms      =4;
  public final static int shell       =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(AppMonOpcode.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AppMonOpcode Prop=new AppMonOpcode();//for accessing class info
  public AppMonOpcode(){
    super();
  }
  public AppMonOpcode(int rawValue){
    super(rawValue);
  }
  public AppMonOpcode(String textValue){
    super(textValue);
  }
  public AppMonOpcode(AppMonOpcode rhs){
    this(rhs.Value());
  }
  public AppMonOpcode setto(AppMonOpcode rhs){
    setto(rhs.Value());
    return this;
  }
  public static AppMonOpcode CopyOf(AppMonOpcode rhs){//null-safe cloner
    return (rhs!=null)? new AppMonOpcode(rhs) : new AppMonOpcode();
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

