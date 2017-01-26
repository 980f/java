// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/SignatureState.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class SignatureState extends TrueEnum {
  public final static int dontcare =0;
  public final static int desired  =1;
  public final static int onwire   =2;
  public final static int acquired =3;
  public final static int receipted=4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(SignatureState.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SignatureState Prop=new SignatureState();//for accessing class info
  public SignatureState(){
    super();
  }
  public SignatureState(int rawValue){
    super(rawValue);
  }
  public SignatureState(String textValue){
    super(textValue);
  }
  public SignatureState(SignatureState rhs){
    this(rhs.Value());
  }
  public SignatureState setto(SignatureState rhs){
    setto(rhs.Value());
    return this;
  }
  public static SignatureState CopyOf(SignatureState rhs){//null-safe cloner
    return (rhs!=null)? new SignatureState(rhs) : new SignatureState();
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

