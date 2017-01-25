// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AssociatesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class AssociatesFormatEnum extends TrueEnum {
  public final static int LoginnameCol    =0;
  public final static int LastNameCol     =1;
  public final static int FirstNameCol    =2;
  public final static int MICol           =3;
  public final static int EnterpriseACLCol=4;
  public final static int ColorSchemeCol  =5;

  public int numValues(){ return 6; }
  private static final TextList myText = TrueEnum.nameVector(AssociatesFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final AssociatesFormatEnum Prop=new AssociatesFormatEnum();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
