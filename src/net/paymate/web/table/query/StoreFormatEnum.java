// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/StoreFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class StoreFormatEnum extends TrueEnum {
  public final static int NameCol    =0;
  public final static int PhoneCol   =1;
  public final static int Address1Col=2;
  public final static int Address2Col=3;
  public final static int CityCol    =4;
  public final static int StateCol   =5;
  public final static int ZipcodeCol =6;
  public final static int CountryCol =7;
  public final static int TypeCol    =8;
  public final static int TimeZoneCol=9;

  public int numValues(){ return 10; }
  private static final String[ ] myText = TrueEnum.nameVector(StoreFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final StoreFormatEnum Prop=new StoreFormatEnum();//for accessing class info
  public StoreFormatEnum(){
    super();
  }
  public StoreFormatEnum(int rawValue){
    super(rawValue);
  }
  public StoreFormatEnum(String textValue){
    super(textValue);
  }
  public StoreFormatEnum(StoreFormatEnum rhs){
    this(rhs.Value());
  }
  public StoreFormatEnum setto(StoreFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static StoreFormatEnum CopyOf(StoreFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new StoreFormatEnum(rhs) : new StoreFormatEnum();
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

