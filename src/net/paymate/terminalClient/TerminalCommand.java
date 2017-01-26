// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/TerminalCommand.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class TerminalCommand extends TrueEnum {
  public final static int Identify     =0;
  public final static int GoOnline     =1;
  public final static int GetConfig    =2;
  public final static int GetProgram   =3;
  public final static int StatusUp     =4;
  public final static int Restart      =5;
  public final static int Shutdown     =6;
  public final static int Clear        =7;
  public final static int SERVICEMODE  =8;
  public final static int Normal       =9;
  public final static int StatusDown   =10;
  public final static int GoOffline    =11;
  public final static int sendSignature=12;

  public int numValues(){ return 13; }
  private static final String[ ] myText = TrueEnum.nameVector(TerminalCommand.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TerminalCommand Prop=new TerminalCommand();//for accessing class info
  public TerminalCommand(){
    super();
  }
  public TerminalCommand(int rawValue){
    super(rawValue);
  }
  public TerminalCommand(String textValue){
    super(textValue);
  }
  public TerminalCommand(TerminalCommand rhs){
    this(rhs.Value());
  }
  public TerminalCommand setto(TerminalCommand rhs){
    setto(rhs.Value());
    return this;
  }
  public static TerminalCommand CopyOf(TerminalCommand rhs){//null-safe cloner
    return (rhs!=null)? new TerminalCommand(rhs) : new TerminalCommand();
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

