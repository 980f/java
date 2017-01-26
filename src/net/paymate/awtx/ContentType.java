// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/awtx/ContentType.Enum]
package net.paymate.awtx;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class ContentType extends TrueEnum {
  public final static int arbitrary =0;
  public final static int purealpha =1;
  public final static int alphanum  =2;
  public final static int password  =3;
  public final static int decimal   =4;
  public final static int hex       =5;
  public final static int money     =6;
  public final static int ledger    =7;
  public final static int cardnumber=8;
  public final static int expirdate =9;
  public final static int micrdata  =10;
  public final static int date      =11;
  public final static int time      =12;
  public final static int zulutime  =13;
  public final static int select    =14;
  public final static int unknown   =15;

  public int numValues(){ return 16; }
  static final TextList myText = TrueEnum.nameVector(ContentType.class);
  protected final TextList getMyText() {
    return myText;
  }
  static ContentType Prop=new ContentType();
  public ContentType(){
    super();
  }
  public ContentType(int rawValue){
    super(rawValue);
  }
  public ContentType(String textValue){
    super(textValue);
  }
  public ContentType(ContentType rhs){
    this(rhs.Value());
  }

}
