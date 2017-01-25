// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/net/HTTPMethod.Enum]
package net.paymate.net;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

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
  private static final TextList myText = TrueEnum.nameVector(HTTPMethod.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final HTTPMethod Prop=new HTTPMethod();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
