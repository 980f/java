// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/TerminalOps.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class TerminalOps extends TrueEnum {
  public final static int Oops   =0;
  public final static int Login  =1;
  public final static int Logout =2;
  public final static int Restart=3;

  public int numValues(){ return 4; }
  static final TextList myText = TrueEnum.nameVector(TerminalOps.class);
  protected final TextList getMyText() {
    return myText;
  }
  static TerminalOps Prop=new TerminalOps();
  public TerminalOps(){
    super();
  }
  public TerminalOps(int rawValue){
    super(rawValue);
  }
  public TerminalOps(String textValue){
    super(textValue);
  }
  public TerminalOps(TerminalOps rhs){
    this(rhs.Value());
  }

}
