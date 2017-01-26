// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/servlet/ServletType.Enum]
package net.paymate.servlet;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class ServletType extends TrueEnum {
  public final static int admin=0;
  public final static int txn  =1;

  public int numValues(){ return 2; }
  private static final TextList myText = TrueEnum.nameVector(ServletType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ServletType Prop=new ServletType();
  public ServletType(){
    super();
  }
  public ServletType(int rawValue){
    super(rawValue);
  }
  public ServletType(String textValue){
    super(textValue);
  }
  public ServletType(ServletType rhs){
    this(rhs.Value());
  }
  public ServletType setto(ServletType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
