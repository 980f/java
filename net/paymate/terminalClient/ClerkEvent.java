// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/ClerkEvent.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ClerkEvent extends TrueEnum {
  public final static int Clear        =0;
  public final static int Cancel       =1;
  public final static int Enter        =2;
  public final static int Send         =3;
  public final static int Reprint      =4;
  public final static int Login        =5;
  public final static int PrintCoupon  =6;
  public final static int SendSignature=7;
  public final static int Reconnect    =8;
  public final static int Functions    =9;
  public final static int Debug        =10;

  public int numValues(){ return 11; }
  private static final TextList myText = TrueEnum.nameVector(ClerkEvent.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ClerkEvent Prop=new ClerkEvent();
  public ClerkEvent(){
    super();
  }
  public ClerkEvent(int rawValue){
    super(rawValue);
  }
  public ClerkEvent(String textValue){
    super(textValue);
  }
  public ClerkEvent(ClerkEvent rhs){
    this(rhs.Value());
  }
  public ClerkEvent setto(ClerkEvent rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
