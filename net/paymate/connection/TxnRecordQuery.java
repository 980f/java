// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/connection/TxnRecordQuery.Enum]
package net.paymate.connection;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class TxnRecordQuery extends TrueEnum {
  public final static int Exact    =0;
  public final static int AnyAmount=1;

  public int numValues(){ return 2; }
  private static final TextList myText = TrueEnum.nameVector(TxnRecordQuery.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TxnRecordQuery Prop=new TxnRecordQuery();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
