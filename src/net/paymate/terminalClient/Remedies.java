// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/Remedies.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class Remedies extends TrueEnum {
  public final static int Void   =0;
  public final static int Done   =1;
  public final static int Retry  =2;
  public final static int Reprint=3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(Remedies.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final Remedies Prop=new Remedies();//for accessing class info
  public Remedies(){
    super();
  }
  public Remedies(int rawValue){
    super(rawValue);
  }
  public Remedies(String textValue){
    super(textValue);
  }
  public Remedies(Remedies rhs){
    this(rhs.Value());
  }
  public Remedies setto(Remedies rhs){
    setto(rhs.Value());
    return this;
  }
  public static Remedies CopyOf(Remedies rhs){//null-safe cloner
    return (rhs!=null)? new Remedies(rhs) : new Remedies();
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

