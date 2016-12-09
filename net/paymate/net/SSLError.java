// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/net/SSLError.Enum]
package net.paymate.net;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class SSLError extends TrueEnum {
  public final static int UntrustedCertificateChain=0;

  public int numValues(){ return 1; }
  private static final TextList myText = TrueEnum.nameVector(SSLError.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final SSLError Prop=new SSLError();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
