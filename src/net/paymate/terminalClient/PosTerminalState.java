// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/PosTerminalState.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class PosTerminalState extends TrueEnum {
  public final static int Ready      =0;
  public final static int Cancellable=1;
  public final static int Occupied   =2;
  public final static int Loafing    =3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(PosTerminalState.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final PosTerminalState Prop=new PosTerminalState();//for accessing class info
  public PosTerminalState(){
    super();
  }
  public PosTerminalState(int rawValue){
    super(rawValue);
  }
  public PosTerminalState(String textValue){
    super(textValue);
  }
  public PosTerminalState(PosTerminalState rhs){
    this(rhs.Value());
  }
  public PosTerminalState setto(PosTerminalState rhs){
    setto(rhs.Value());
    return this;
  }
  public static PosTerminalState CopyOf(PosTerminalState rhs){//null-safe cloner
    return (rhs!=null)? new PosTerminalState(rhs) : new PosTerminalState();
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

