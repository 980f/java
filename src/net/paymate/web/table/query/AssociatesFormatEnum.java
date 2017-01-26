// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AssociatesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class AssociatesFormatEnum extends TrueEnum {
  public final static int NameCol         =0;
  public final static int LoginnameCol    =1;
  public final static int EnterpriseACLCol=2;
  public final static int AssociateidCol  =3;
  public final static int ColorschemeidCol=4;
  public final static int EnabledCol      =5;
  public final static int EnauthmsgviewCol=6;
  public final static int EncodedpwCol    =7;
  public final static int EndbCol         =8;

  public int numValues(){ return 9; }
  private static final String[ ] myText = TrueEnum.nameVector(AssociatesFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AssociatesFormatEnum Prop=new AssociatesFormatEnum();//for accessing class info
  public AssociatesFormatEnum(){
    super();
  }
  public AssociatesFormatEnum(int rawValue){
    super(rawValue);
  }
  public AssociatesFormatEnum(String textValue){
    super(textValue);
  }
  public AssociatesFormatEnum(AssociatesFormatEnum rhs){
    this(rhs.Value());
  }
  public AssociatesFormatEnum setto(AssociatesFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AssociatesFormatEnum CopyOf(AssociatesFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new AssociatesFormatEnum(rhs) : new AssociatesFormatEnum();
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

