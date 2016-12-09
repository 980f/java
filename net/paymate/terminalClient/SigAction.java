// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/SigAction.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class SigAction extends TrueEnum {
  public final static int Electronic=0;
  public final static int Manual    =1;
  public final static int OnFile    =2;

  public int numValues(){ return 3; }
  static final TextList myText = TrueEnum.nameVector(SigAction.class);
  protected final TextList getMyText() {
    return myText;
  }
  static SigAction Prop=new SigAction();
  public SigAction(){
    super();
  }
  public SigAction(int rawValue){
    super(rawValue);
  }
  public SigAction(String textValue){
    super(textValue);
  }
  public SigAction(SigAction rhs){
    this(rhs.Value());
  }

}
