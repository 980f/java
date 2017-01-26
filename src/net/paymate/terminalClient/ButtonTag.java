// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/ButtonTag.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class ButtonTag extends TrueEnum {
  public final static int Reserved0       =0;
  public final static int NullButton      =1;
  public final static int ClearForm       =2;
  public final static int CustomerAmountOk=3;
  public final static int Signed          =4;
  public final static int CustomerCancels =5;
  public final static int DoCheck         =6;
  public final static int DoCredit        =7;
  public final static int DoDebit         =8;
  public final static int DoCash          =9;
  public final static int GetBalance      =10;
  public final static int DriversLicense  =11;
  public final static int OtherCard       =12;
  public final static int CouponDesired   =13;

  public int numValues(){ return 14; }
  private static final String[ ] myText = TrueEnum.nameVector(ButtonTag.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ButtonTag Prop=new ButtonTag();//for accessing class info
  public ButtonTag(){
    super();
  }
  public ButtonTag(int rawValue){
    super(rawValue);
  }
  public ButtonTag(String textValue){
    super(textValue);
  }
  public ButtonTag(ButtonTag rhs){
    this(rhs.Value());
  }
  public ButtonTag setto(ButtonTag rhs){
    setto(rhs.Value());
    return this;
  }
  public static ButtonTag CopyOf(ButtonTag rhs){//null-safe cloner
    return (rhs!=null)? new ButtonTag(rhs) : new ButtonTag();
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

