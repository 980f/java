// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/net/SSLError.Enum]
package net.paymate.net;

import net.paymate.lang.TrueEnum;

public class SSLError extends TrueEnum {
  public final static int UntrustedCertificateChain=0;

  public int numValues(){ return 1; }
  private static final String[ ] myText = TrueEnum.nameVector(SSLError.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final SSLError Prop=new SSLError();//for accessing class info
  public SSLError(){
    super();
  }
  public SSLError(int rawValue){
    super(rawValue);
  }
  public SSLError(String textValue){
    super(textValue);
  }
  public SSLError(SSLError rhs){
    this(rhs.Value());
  }
  public SSLError setto(SSLError rhs){
    setto(rhs.Value());
    return this;
  }
  public static SSLError CopyOf(SSLError rhs){//null-safe cloner
    return (rhs!=null)? new SSLError(rhs) : new SSLError();
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

