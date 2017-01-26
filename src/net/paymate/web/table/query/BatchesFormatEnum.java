// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/BatchesFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class BatchesFormatEnum extends TrueEnum {
  public final static int TimeCol        =0;
  public final static int TermCol        =1;
  public final static int AuthCol        =2;
  public final static int AuthrespmsgCol =3;
  public final static int BatchseqCol    =4;
  public final static int TermbatchnumCol=5;
  public final static int CountCol       =6;
  public final static int AmountCol      =7;
  public final static int CSVCol         =8;

  public int numValues(){ return 9; }
  private static final String[ ] myText = TrueEnum.nameVector(BatchesFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final BatchesFormatEnum Prop=new BatchesFormatEnum();//for accessing class info
  public BatchesFormatEnum(){
    super();
  }
  public BatchesFormatEnum(int rawValue){
    super(rawValue);
  }
  public BatchesFormatEnum(String textValue){
    super(textValue);
  }
  public BatchesFormatEnum(BatchesFormatEnum rhs){
    this(rhs.Value());
  }
  public BatchesFormatEnum setto(BatchesFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static BatchesFormatEnum CopyOf(BatchesFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new BatchesFormatEnum(rhs) : new BatchesFormatEnum();
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

