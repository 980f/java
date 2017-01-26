// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/StoreAuthsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class StoreAuthsFormatEnum extends TrueEnum {
  public final static int StoreAuthidCol  =0;
  public final static int StoreidCol      =1;
  public final static int PaytypeCol      =2;
  public final static int InstCol         =3;
  public final static int MaxTxnLimitCol  =4;
  public final static int AuthidCol       =5;
  public final static int AuthMerchidCol  =6;
  public final static int SettleidCol     =7;
  public final static int SettleMerchidCol=8;

  public int numValues(){ return 9; }
  private static final String[ ] myText = TrueEnum.nameVector(StoreAuthsFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final StoreAuthsFormatEnum Prop=new StoreAuthsFormatEnum();//for accessing class info
  public StoreAuthsFormatEnum(){
    super();
  }
  public StoreAuthsFormatEnum(int rawValue){
    super(rawValue);
  }
  public StoreAuthsFormatEnum(String textValue){
    super(textValue);
  }
  public StoreAuthsFormatEnum(StoreAuthsFormatEnum rhs){
    this(rhs.Value());
  }
  public StoreAuthsFormatEnum setto(StoreAuthsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static StoreAuthsFormatEnum CopyOf(StoreAuthsFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new StoreAuthsFormatEnum(rhs) : new StoreAuthsFormatEnum();
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

