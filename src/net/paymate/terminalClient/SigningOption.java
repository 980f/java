// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/SigningOption.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class SigningOption extends TrueEnum {
  public final static int DoneSigning    =0;
  public final static int SignPaper      =1;
  public final static int VoidTransaction=2;

  public int numValues(){ return 3; }
  private static final TextList myText = TrueEnum.nameVector(SigningOption.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final SigningOption Prop=new SigningOption();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
