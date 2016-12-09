// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/ActionType.Enum]
package net.paymate.connection;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class ActionType extends TrueEnum {
  public final static int unknown     =0;
  public final static int admin       =1;
  public final static int stoodin     =2;
  public final static int connection  =3;
  public final static int update      =4;
  public final static int clerkLogin  =5;
  public final static int message     =6;
  public final static int tolog       =7;
  public final static int toclerk     =8;
  public final static int toprinter   =9;
  public final static int financial   =10;
  public final static int check       =11;
  public final static int card        =12;
  public final static int credit      =13;
  public final static int debit       =14;
  public final static int adminWeb    =15;
  public final static int batch       =16;
  public final static int reversal    =17;
  public final static int receiptStore=18;
  public final static int receiptGet  =19;

  public int numValues(){ return 20; }
  private static final TextList myText = TrueEnum.nameVector(ActionType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final ActionType Prop=new ActionType();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
