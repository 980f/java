// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/EnterprisesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class EnterprisesFormatEnum extends TrueEnum {
  public final static int EditCol          =0;
  public final static int EnterpriseNameCol=1;
  public final static int StoreNameCol     =2;
  public final static int StorePhoneCol    =3;

  public int numValues(){ return 4; }
  private static final String[ ] myText = TrueEnum.nameVector(EnterprisesFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final EnterprisesFormatEnum Prop=new EnterprisesFormatEnum();//for accessing class info
  public EnterprisesFormatEnum(){
    super();
  }
  public EnterprisesFormatEnum(int rawValue){
    super(rawValue);
  }
  public EnterprisesFormatEnum(String textValue){
    super(textValue);
  }
  public EnterprisesFormatEnum(EnterprisesFormatEnum rhs){
    this(rhs.Value());
  }
  public EnterprisesFormatEnum setto(EnterprisesFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static EnterprisesFormatEnum CopyOf(EnterprisesFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new EnterprisesFormatEnum(rhs) : new EnterprisesFormatEnum();
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

