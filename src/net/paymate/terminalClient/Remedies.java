// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/terminalClient/Remedies.Enum]
package net.paymate.terminalClient;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class Remedies extends TrueEnum {
  public final static int Void   =0;
  public final static int Done   =1;
  public final static int Retry  =2;
  public final static int Reprint=3;

  public int numValues(){ return 4; }
  private static final TextList myText = TrueEnum.nameVector(Remedies.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final Remedies Prop=new Remedies();
  public Remedies(){
    super();
  }
  public Remedies(int rawValue){
    super(rawValue);
  }
  public Remedies(String textValue){
    super(textValue);
  }
  public Remedies(Remedies rhs){
    this(rhs.Value());
  }
  public Remedies setto(Remedies rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
