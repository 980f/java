// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/jpos/Terminal/EventType.Enum]
package net.paymate.jpos.Terminal;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class EventType extends TrueEnum {
  public final static int CheckAcquired =0;
  public final static int KeyStroke     =1;
  public final static int KeyedValue    =2;
  public final static int CardAcquired  =3;
  public final static int SigAcquired   =4;
  public final static int PinAcquired   =5;
  public final static int FormButtonData=6;
  public final static int FormKeyedData =7;
  public final static int Jape          =8;

  public int numValues(){ return 9; }
  private static final TextList myText = TrueEnum.nameVector(EventType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final EventType Prop=new EventType();
  public EventType(){
    super();
  }
  public EventType(int rawValue){
    super(rawValue);
  }
  public EventType(String textValue){
    super(textValue);
  }
  public EventType(EventType rhs){
    this(rhs.Value());
  }
  public EventType setto(EventType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
