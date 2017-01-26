// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/ISO8583/data/EntrySource.Enum]
package net.paymate.ISO8583.data;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class EntrySource extends TrueEnum {
  public final static int Unknown=0;
  public final static int KeyedIn=1;
  public final static int Swiped =2;
  public final static int BarCode=3;
  public final static int MICRed =4;

  public int numValues(){ return 5; }
  private static final TextList myText = TrueEnum.nameVector(EntrySource.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final EntrySource Prop=new EntrySource();
  public EntrySource(){
    super();
  }
  public EntrySource(int rawValue){
    super(rawValue);
  }
  public EntrySource(String textValue){
    super(textValue);
  }
  public EntrySource(EntrySource rhs){
    this(rhs.Value());
  }
  public EntrySource setto(EntrySource rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
