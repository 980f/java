// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/UserPermissions.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class UserPermissions extends TrueEnum {
  public final static int canvoid    =0;
  public final static int canreturn  =1;
  public final static int canweb     =2;
  public final static int cansale    =3;
  public final static int candatabase=4;
  public final static int canclose   =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(UserPermissions.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final UserPermissions Prop=new UserPermissions();//for accessing class info
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
  public static UserPermissions CopyOf(UserPermissions rhs){//null-safe cloner
    return (rhs!=null)? new UserPermissions(rhs) : new UserPermissions();
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

