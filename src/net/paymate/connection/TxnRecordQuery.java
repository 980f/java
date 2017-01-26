// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/TxnRecordQuery.Enum]
package net.paymate.connection;

import net.paymate.lang.TrueEnum;

public class TxnRecordQuery extends TrueEnum {
  public final static int Exact    =0;
  public final static int AnyAmount=1;

  public int numValues(){ return 2; }
  private static final String[ ] myText = TrueEnum.nameVector(TxnRecordQuery.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TxnRecordQuery Prop=new TxnRecordQuery();//for accessing class info
  public TxnRecordQuery(){
    super();
  }
  public TxnRecordQuery(int rawValue){
    super(rawValue);
  }
  public TxnRecordQuery(String textValue){
    super(textValue);
  }
  public TxnRecordQuery(TxnRecordQuery rhs){
    this(rhs.Value());
  }
  public TxnRecordQuery setto(TxnRecordQuery rhs){
    setto(rhs.Value());
    return this;
  }
  public static TxnRecordQuery CopyOf(TxnRecordQuery rhs){//null-safe cloner
    return (rhs!=null)? new TxnRecordQuery(rhs) : new TxnRecordQuery();
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

