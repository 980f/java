// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/AuthStat.Enum]
package net.paymate.database;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class AuthStat extends TrueEnum {
  public final static int N=0;
  public final static int S=1;
  public final static int R=2;
  public final static int D=3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(AuthStat.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final AuthStat Prop=new AuthStat();
  public AuthStat(){
    super();
  }
  public AuthStat(int rawValue){
    super(rawValue);
  }
  public AuthStat(String textValue){
    super(textValue);
  }
  public AuthStat(AuthStat rhs){
    this(rhs.Value());
  }
  public AuthStat setto(AuthStat rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
