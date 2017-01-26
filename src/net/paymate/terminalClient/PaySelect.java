// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/PaySelect.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class PaySelect extends TrueEnum {
  public final static int Cancel     =0;
  public final static int ManualCard =1;
  public final static int ManualCheck=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(PaySelect.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final PaySelect Prop=new PaySelect();//for accessing class info
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
  public static PaySelect CopyOf(PaySelect rhs){//null-safe cloner
    return (rhs!=null)? new PaySelect(rhs) : new PaySelect();
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

