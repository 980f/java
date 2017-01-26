// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/FTstate.Enum]
package net.paymate.terminalClient;

import net.paymate.lang.TrueEnum;

public class FTstate extends TrueEnum {
  public final static int NoInfo    =0;
  public final static int WaitAdmin =1;
  public final static int Incomplete=2;
  public final static int WaitAuth  =3;
  public final static int WaitSig   =4;
  public final static int DoneGood  =5;
  public final static int DoneBad   =6;

  public int numValues(){ return 7; }
  private static final String[ ] myText = TrueEnum.nameVector(FTstate.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final FTstate Prop=new FTstate();//for accessing class info
  public FTstate(){
    super();
  }
  public FTstate(int rawValue){
    super(rawValue);
  }
  public FTstate(String textValue){
    super(textValue);
  }
  public FTstate(FTstate rhs){
    this(rhs.Value());
  }
  public FTstate setto(FTstate rhs){
    setto(rhs.Value());
    return this;
  }
  public static FTstate CopyOf(FTstate rhs){//null-safe cloner
    return (rhs!=null)? new FTstate(rhs) : new FTstate();
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

