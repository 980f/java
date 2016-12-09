// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/FTstate.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class FTstate extends TrueEnum {
  public final static int NoInfo    =0;
  public final static int Incomplete=1;
  public final static int WaitAuth  =2;
  public final static int WaitSig   =3;
  public final static int SigOnWire =4;
  public final static int DoneGood  =5;
  public final static int DoneBad   =6;

  public int numValues(){ return 7; }
  private static final TextList myText = TrueEnum.nameVector(FTstate.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final FTstate Prop=new FTstate();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
