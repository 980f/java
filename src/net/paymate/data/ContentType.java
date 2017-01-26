// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/ContentType.Enum]
package net.paymate.data;

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
  private static final TextList myText = TrueEnum.nameVector(ContentType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ContentType Prop=new ContentType();
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
  public ContentType setto(ContentType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
