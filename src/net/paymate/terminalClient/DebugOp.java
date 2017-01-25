// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/DebugOp.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class DebugOp extends TrueEnum {
  public final static int Refresh       =0;
  public final static int ForceLoggedIn =1;
  public final static int ForceClerkIn  =2;
  public final static int ForceSignature=3;
  public final static int SimulateOn    =4;
  public final static int SimulateOff   =5;
  public final static int TranSpamOn    =6;
  public final static int TranSpamOff   =7;

  public int numValues(){ return 8; }
  private static final TextList myText = TrueEnum.nameVector(DebugOp.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final DebugOp Prop=new DebugOp();
  public DebugOp(){
    super();
  }
  public DebugOp(int rawValue){
    super(rawValue);
  }
  public DebugOp(String textValue){
    super(textValue);
  }
  public DebugOp(DebugOp rhs){
    this(rhs.Value());
  }
  public DebugOp setto(DebugOp rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
