// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/TermAuthsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class TermAuthsFormatEnum extends TrueEnum {
  public final static int TermAuthidCol  =0;
  public final static int TerminalidCol  =1;
  public final static int AuthidCol      =2;
  public final static int AuthTermidCol  =3;
  public final static int AuthSeqCol     =4;
  public final static int TermBatchnumCol=5;

  public int numValues(){ return 6; }
  private static final String[ ] myText = TrueEnum.nameVector(TermAuthsFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TermAuthsFormatEnum Prop=new TermAuthsFormatEnum();//for accessing class info
  public TermAuthsFormatEnum(){
    super();
  }
  public TermAuthsFormatEnum(int rawValue){
    super(rawValue);
  }
  public TermAuthsFormatEnum(String textValue){
    super(textValue);
  }
  public TermAuthsFormatEnum(TermAuthsFormatEnum rhs){
    this(rhs.Value());
  }
  public TermAuthsFormatEnum setto(TermAuthsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static TermAuthsFormatEnum CopyOf(TermAuthsFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new TermAuthsFormatEnum(rhs) : new TermAuthsFormatEnum();
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

