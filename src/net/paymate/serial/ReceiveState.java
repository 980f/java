// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/serial/ReceiveState.Enum]
package net.paymate.serial;

import net.paymate.lang.TrueEnum;

public class ReceiveState extends TrueEnum {
  public final static int Unknown=0;
  public final static int Empty  =1;
  public final static int Active =2;
  public final static int Control=3;
  public final static int Packet =4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(ReceiveState.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ReceiveState Prop=new ReceiveState();//for accessing class info
  public ReceiveState(){
    super();
  }
  public ReceiveState(int rawValue){
    super(rawValue);
  }
  public ReceiveState(String textValue){
    super(textValue);
  }
  public ReceiveState(ReceiveState rhs){
    this(rhs.Value());
  }
  public ReceiveState setto(ReceiveState rhs){
    setto(rhs.Value());
    return this;
  }
  public static ReceiveState CopyOf(ReceiveState rhs){//null-safe cloner
    return (rhs!=null)? new ReceiveState(rhs) : new ReceiveState();
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

