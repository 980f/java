// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/Functions.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class Functions extends TrueEnum {
  public final static int Sale       =0;
  public final static int Return     =1;
  public final static int Void       =2;
  public final static int ReEntry    =3;
  public final static int LastReceipt=4;
  public final static int PrintDrawer=5;
  public final static int CloseDrawer=6;
  public final static int ChangeUser =7;
  public final static int PrintCoupon=8;
  public final static int reserved   =9;

  public int numValues(){ return 10; }
  private static final TextList myText = TrueEnum.nameVector(Functions.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final Functions Prop=new Functions();
  public Functions(){
    super();
  }
  public Functions(int rawValue){
    super(rawValue);
  }
  public Functions(String textValue){
    super(textValue);
  }
  public Functions(Functions rhs){
    this(rhs.Value());
  }
  public Functions setto(Functions rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
