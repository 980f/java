// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/database/TableType.Enum]
package net.paymate.database;

import net.paymate.lang.TrueEnum;

public class TableType extends TrueEnum {
  public final static int cfg=0;
  public final static int log=1;

  public int numValues(){ return 2; }
  private static final String[ ] myText = TrueEnum.nameVector(TableType.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TableType Prop=new TableType();//for accessing class info
  public TableType(){
    super();
  }
  public TableType(int rawValue){
    super(rawValue);
  }
  public TableType(String textValue){
    super(textValue);
  }
  public TableType(TableType rhs){
    this(rhs.Value());
  }
  public TableType setto(TableType rhs){
    setto(rhs.Value());
    return this;
  }
  public static TableType CopyOf(TableType rhs){//null-safe cloner
    return (rhs!=null)? new TableType(rhs) : new TableType();
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

