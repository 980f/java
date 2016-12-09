// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/PaySelect.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class PaySelect extends TrueEnum {
  public final static int Cancel     =0;
  public final static int ManualCard =1;
  public final static int ManualCheck=2;

  public int numValues(){ return 3; }
  private static final TextList myText = TrueEnum.nameVector(PaySelect.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final PaySelect Prop=new PaySelect();
  public PaySelect(){
    super();
  }
  public PaySelect(int rawValue){
    super(rawValue);
  }
  public PaySelect(String textValue){
    super(textValue);
  }
  public PaySelect(PaySelect rhs){
    this(rhs.Value());
  }
  public PaySelect setto(PaySelect rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
