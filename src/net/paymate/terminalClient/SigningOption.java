// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/SigningOption.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class SigningOption extends TrueEnum {
  public final static int DoneSigning=0;
  public final static int SignPaper  =1;
  public final static int StartOver  =2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(SigningOption.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SigningOption Prop=new SigningOption();//for accessing class info
  public SigningOption(){
    super();
  }
  public SigningOption(int rawValue){
    super(rawValue);
  }
  public SigningOption(String textValue){
    super(textValue);
  }
  public SigningOption(SigningOption rhs){
    this(rhs.Value());
  }
  public SigningOption setto(SigningOption rhs){
    setto(rhs.Value());
    return this;
  }
  public static SigningOption CopyOf(SigningOption rhs){//null-safe cloner
    return (rhs!=null)? new SigningOption(rhs) : new SigningOption();
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

