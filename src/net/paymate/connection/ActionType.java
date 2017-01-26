// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/ActionType.Enum]
package net.paymate.connection;

import net.paymate.lang.TrueEnum;

public class ActionType extends TrueEnum {
  public final static int admin       =0;
  public final static int adminWeb    =1;
  public final static int batch       =2;
  public final static int clerkLogin  =3;
  public final static int connection  =4;
  public final static int gateway     =5;
  public final static int ipstatupdate=6;
  public final static int multi       =7;
  public final static int payment     =8;
  public final static int receiptGet  =9;
  public final static int receiptStore=10;
  public final static int stoodin     =11;
  public final static int store       =12;
  public final static int unknown     =13;
  public final static int update      =14;

  public int numValues(){ return 15; }
  private static final String[ ] myText = TrueEnum.nameVector(ActionType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final ActionType Prop=new ActionType();//for accessing class info
  public ActionType(){
    super();
  }
  public ActionType(int rawValue){
    super(rawValue);
  }
  public ActionType(String textValue){
    super(textValue);
  }
  public ActionType(ActionType rhs){
    this(rhs.Value());
  }
  public ActionType setto(ActionType rhs){
    setto(rhs.Value());
    return this;
  }
  public static ActionType CopyOf(ActionType rhs){//null-safe cloner
    return (rhs!=null)? new ActionType(rhs) : new ActionType();
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

