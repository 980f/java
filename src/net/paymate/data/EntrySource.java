// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/data/EntrySource.Enum]
package net.paymate.data;

import net.paymate.lang.TrueEnum;

public class EntrySource extends TrueEnum {
  public final static int Unknown=0;
  public final static int Manual =1;
  public final static int Machine=2;

  public int numValues(){ return 3; }
  private static final String[ ] myText = TrueEnum.nameVector(EntrySource.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final EntrySource Prop=new EntrySource();//for accessing class info
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
  public static EntrySource CopyOf(EntrySource rhs){//null-safe cloner
    return (rhs!=null)? new EntrySource(rhs) : new EntrySource();
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

