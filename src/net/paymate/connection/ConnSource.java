// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/ConnSource.Enum]
package net.paymate.connection;

import net.paymate.lang.TrueEnum;

public class ConnSource extends TrueEnum {
  public final static int terminalObjects=0;
  public final static int browserHttp    =1;

  public int numValues(){ return 2; }
  private static final String[ ] myText = TrueEnum.nameVector(ConnSource.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ConnSource Prop=new ConnSource();//for accessing class info
  public ConnSource(){
    super();
  }
  public ConnSource(int rawValue){
    super(rawValue);
  }
  public ConnSource(String textValue){
    super(textValue);
  }
  public ConnSource(ConnSource rhs){
    this(rhs.Value());
  }
  public ConnSource setto(ConnSource rhs){
    setto(rhs.Value());
    return this;
  }
  public static ConnSource CopyOf(ConnSource rhs){//null-safe cloner
    return (rhs!=null)? new ConnSource(rhs) : new ConnSource();
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

