// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/net/HTTPMethod.Enum]
package net.paymate.net;

import net.paymate.lang.TrueEnum;

public class HTTPMethod extends TrueEnum {
  public final static int OPTIONS=0;
  public final static int GET    =1;
  public final static int HEAD   =2;
  public final static int POST   =3;
  public final static int PUT    =4;
  public final static int DELETE =5;
  public final static int TRACE  =6;
  public final static int CONNECT=7;

  public int numValues(){ return 8; }
  private static final String[ ] myText = TrueEnum.nameVector(HTTPMethod.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final HTTPMethod Prop=new HTTPMethod();//for accessing class info
  public HTTPMethod(){
    super();
  }
  public HTTPMethod(int rawValue){
    super(rawValue);
  }
  public HTTPMethod(String textValue){
    super(textValue);
  }
  public HTTPMethod(HTTPMethod rhs){
    this(rhs.Value());
  }
  public HTTPMethod setto(HTTPMethod rhs){
    setto(rhs.Value());
    return this;
  }
  public static HTTPMethod CopyOf(HTTPMethod rhs){//null-safe cloner
    return (rhs!=null)? new HTTPMethod(rhs) : new HTTPMethod();
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

