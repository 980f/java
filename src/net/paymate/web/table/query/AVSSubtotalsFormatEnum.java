// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/AVSSubtotalsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class AVSSubtotalsFormatEnum extends TrueEnum {
  public final static int InstitutionCol   =0;
  public final static int AVSCodeCol       =1;
  public final static int AVSDescriptionCol=2;
  public final static int CountCol         =3;
  public final static int SumCol           =4;

  public int numValues(){ return 5; }
  private static final String[ ] myText = TrueEnum.nameVector(AVSSubtotalsFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final AVSSubtotalsFormatEnum Prop=new AVSSubtotalsFormatEnum();//for accessing class info
  public AVSSubtotalsFormatEnum(){
    super();
  }
  public AVSSubtotalsFormatEnum(int rawValue){
    super(rawValue);
  }
  public AVSSubtotalsFormatEnum(String textValue){
    super(textValue);
  }
  public AVSSubtotalsFormatEnum(AVSSubtotalsFormatEnum rhs){
    this(rhs.Value());
  }
  public AVSSubtotalsFormatEnum setto(AVSSubtotalsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static AVSSubtotalsFormatEnum CopyOf(AVSSubtotalsFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new AVSSubtotalsFormatEnum(rhs) : new AVSSubtotalsFormatEnum();
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

