// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/UserPermissions.Enum]
package net.paymate.web;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class UserPermissions extends TrueEnum {
  public final static int V=0;
  public final static int R=1;
  public final static int E=2;
  public final static int S=3;
  public final static int D=4;

  public int numValues(){ return 5; }
  private static final TextList myText = TrueEnum.nameVector(UserPermissions.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final UserPermissions Prop=new UserPermissions();
  public UserPermissions(){
    super();
  }
  public UserPermissions(int rawValue){
    super(rawValue);
  }
  public UserPermissions(String textValue){
    super(textValue);
  }
  public UserPermissions(UserPermissions rhs){
    this(rhs.Value());
  }
  public UserPermissions setto(UserPermissions rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
