// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/AdminOpCode.Enum]
package net.paymate.web;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class AdminOpCode extends TrueEnum {
  public final static int t          =0;
  public final static int h          =1;
  public final static int s          =2;
  public final static int e          =3;
  public final static int m          =4;
  public final static int a          =5;
  public final static int o          =6;
  public final static int p          =7;
  public final static int d          =8;
  public final static int g          =9;
  public final static int b          =10;
  public final static int f          =11;
  public final static int bl         =12;
  public final static int bp         =13;
  public final static int shUtdOwn   =14;
  public final static int c          =15;
  public final static int c1         =16;
  public final static int ss         =17;
  public final static int ap         =18;
  public final static int bu         =19;
  public final static int cArdsYstEms=20;
  public final static int n          =21;
  public final static int bt         =22;
  public final static int duptemp    =23;
  public final static int gc         =24;

  public int numValues(){ return 25; }
  private static final TextList myText = TrueEnum.nameVector(AdminOpCode.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final AdminOpCode Prop=new AdminOpCode();
  public AdminOpCode(){
    super();
  }
  public AdminOpCode(int rawValue){
    super(rawValue);
  }
  public AdminOpCode(String textValue){
    super(textValue);
  }
  public AdminOpCode(AdminOpCode rhs){
    this(rhs.Value());
  }
  public AdminOpCode setto(AdminOpCode rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
