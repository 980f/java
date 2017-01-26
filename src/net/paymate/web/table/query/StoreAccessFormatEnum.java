// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/StoreAccessFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class StoreAccessFormatEnum extends TrueEnum {
  public final static int StoreaccessidCol=0;
  public final static int AssociateidCol  =1;
  public final static int StoreidCol      =2;
  public final static int EnsaleCol       =3;
  public final static int EnreturnCol     =4;
  public final static int EnvoidCol       =5;
  public final static int EnclosedrawerCol=6;

  public int numValues(){ return 7; }
  private static final String[ ] myText = TrueEnum.nameVector(StoreAccessFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final StoreAccessFormatEnum Prop=new StoreAccessFormatEnum();//for accessing class info
  public StoreAccessFormatEnum(){
    super();
  }
  public StoreAccessFormatEnum(int rawValue){
    super(rawValue);
  }
  public StoreAccessFormatEnum(String textValue){
    super(textValue);
  }
  public StoreAccessFormatEnum(StoreAccessFormatEnum rhs){
    this(rhs.Value());
  }
  public StoreAccessFormatEnum setto(StoreAccessFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static StoreAccessFormatEnum CopyOf(StoreAccessFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new StoreAccessFormatEnum(rhs) : new StoreAccessFormatEnum();
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

