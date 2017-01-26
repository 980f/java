// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/SystemFunction.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class SystemFunction extends TrueEnum {
  public final static int TerminalInfo =0;
  public final static int ContactInfo  =1;
  public final static int MajicNumber  =2;
  public final static int ToggleShowSig=3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(SystemFunction.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SystemFunction Prop=new SystemFunction();//for accessing class info
  public SystemFunction(){
    super();
  }
  public SystemFunction(int rawValue){
    super(rawValue);
  }
  public SystemFunction(String textValue){
    super(textValue);
  }
  public SystemFunction(SystemFunction rhs){
    this(rhs.Value());
  }
  public SystemFunction setto(SystemFunction rhs){
    setto(rhs.Value());
    return this;
  }
  public static SystemFunction CopyOf(SystemFunction rhs){//null-safe cloner
    return (rhs!=null)? new SystemFunction(rhs) : new SystemFunction();
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

