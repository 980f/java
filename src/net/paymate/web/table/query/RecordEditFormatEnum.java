// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/RecordEditFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class RecordEditFormatEnum extends TrueEnum {
  public final static int nameCol    =0;
  public final static int valueCol   =1;
  public final static int oldValueCol=2;
  public final static int typeCol    =3;
  public final static int nullableCol=4;
  public final static int defaultCol =5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(RecordEditFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final RecordEditFormatEnum Prop=new RecordEditFormatEnum();//for accessing class info
  public RecordEditFormatEnum(){
    super();
  }
  public RecordEditFormatEnum(int rawValue){
    super(rawValue);
  }
  public RecordEditFormatEnum(String textValue){
    super(textValue);
  }
  public RecordEditFormatEnum(RecordEditFormatEnum rhs){
    this(rhs.Value());
  }
  public RecordEditFormatEnum setto(RecordEditFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static RecordEditFormatEnum CopyOf(RecordEditFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new RecordEditFormatEnum(rhs) : new RecordEditFormatEnum();
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

