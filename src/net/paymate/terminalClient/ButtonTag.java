// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/ButtonTag.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

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
  public final static int DriversLicense  =10;
  public final static int OtherCard       =11;
  public final static int CouponDesired   =12;

  public int numValues(){ return 13; }
  private static final TextList myText = TrueEnum.nameVector(ButtonTag.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ButtonTag Prop=new ButtonTag();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
