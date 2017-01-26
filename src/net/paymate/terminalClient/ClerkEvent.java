// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/ClerkEvent.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class ClerkEvent extends TrueEnum {
  public final static int Clear        =0;
  public final static int Cancel       =1;
  public final static int Enter        =2;
  public final static int Send         =3;
  public final static int Reprint      =4;
  public final static int Login        =5;
  public final static int SendSignature=6;
  public final static int Reconnect    =7;
  public final static int Functions    =8;
  public final static int Debug        =9;

  public int numValues(){ return 10; }
  private static final String[ ] myText = TrueEnum.nameVector(ClerkEvent.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ClerkEvent Prop=new ClerkEvent();//for accessing class info
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
  public static ClerkEvent CopyOf(ClerkEvent rhs){//null-safe cloner
    return (rhs!=null)? new ClerkEvent(rhs) : new ClerkEvent();
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

