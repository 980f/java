// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/Functions.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class Functions extends TrueEnum {
  public final static int Sale          =0;
  public final static int Return        =1;
  public final static int Void          =2;
  public final static int OtherTransactn=3;
  public final static int LastReceipt   =4;
  public final static int Drawer        =5;
  public final static int StoreAdmin    =6;
  public final static int ChangeUser    =7;
  public final static int GiftCard      =8;
  public final static int Maintenance   =9;

  public int numValues(){ return 10; }
  private static final String[ ] myText = TrueEnum.nameVector(Functions.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final Functions Prop=new Functions();//for accessing class info
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
  public static Functions CopyOf(Functions rhs){//null-safe cloner
    return (rhs!=null)? new Functions(rhs) : new Functions();
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

