// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/GiftCard_Op.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class GiftCard_Op extends TrueEnum {
  public final static int GetBalance  =0;
  public final static int CashOut     =1;
  public final static int Instructions=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(GiftCard_Op.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final GiftCard_Op Prop=new GiftCard_Op();//for accessing class info
  public GiftCard_Op(){
    super();
  }
  public GiftCard_Op(int rawValue){
    super(rawValue);
  }
  public GiftCard_Op(String textValue){
    super(textValue);
  }
  public GiftCard_Op(GiftCard_Op rhs){
    this(rhs.Value());
  }
  public GiftCard_Op setto(GiftCard_Op rhs){
    setto(rhs.Value());
    return this;
  }
  public static GiftCard_Op CopyOf(GiftCard_Op rhs){//null-safe cloner
    return (rhs!=null)? new GiftCard_Op(rhs) : new GiftCard_Op();
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

