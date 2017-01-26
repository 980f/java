// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/PMTermEventType.Enum]
package net.paymate.database;

import net.paymate.lang.TrueEnum;
import net.paymate.util.TextList;

public class PMTermEventType extends TrueEnum {

  public int numValues(){ return 0; }
  private static final TextList myText = TrueEnum.nameVector(PMTermEventType.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final PMTermEventType Prop=new PMTermEventType();
  public PMTermEventType(){
    super();
  }
  public PMTermEventType(int rawValue){
    super(rawValue);
  }
  public PMTermEventType(String textValue){
    super(textValue);
  }
  public PMTermEventType(PMTermEventType rhs){
    this(rhs.Value());
  }
  public PMTermEventType setto(PMTermEventType rhs){
    setto(rhs.Value());
    return this;
  }

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
