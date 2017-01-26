// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/SinetTimeout.Enum]
package net.paymate.connection;

import net.paymate.lang.TrueEnum;

public class SinetTimeout extends TrueEnum {
  public final static int connection   =0;
  public final static int configuration=1;
  public final static int single       =2;
  public final static int multiple     =3;
  public final static int holdoff      =4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(SinetTimeout.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SinetTimeout Prop=new SinetTimeout();//for accessing class info
  public SinetTimeout(){
    super();
  }
  public SinetTimeout(int rawValue){
    super(rawValue);
  }
  public SinetTimeout(String textValue){
    super(textValue);
  }
  public SinetTimeout(SinetTimeout rhs){
    this(rhs.Value());
  }
  public SinetTimeout setto(SinetTimeout rhs){
    setto(rhs.Value());
    return this;
  }
  public static SinetTimeout CopyOf(SinetTimeout rhs){//null-safe cloner
    return (rhs!=null)? new SinetTimeout(rhs) : new SinetTimeout();
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

