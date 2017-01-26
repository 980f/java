// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/UnsettledTransactionFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class UnsettledTransactionFormatEnum extends TrueEnum {
  public final static int TimeCol       =0;
  public final static int MerchRefCol   =1;
  public final static int StanCol       =2;
  public final static int TraceCol      =3;
  public final static int StatusCol     =4;
  public final static int ApprovalCol   =5;
  public final static int AVSCodeCol    =6;
  public final static int PayTypeCol    =7;
  public final static int InstitutionCol=8;
  public final static int AcctNumCol    =9;
  public final static int SaleCol       =10;
  public final static int ReturnCol     =11;
  public final static int VoidChgCol    =12;
  public final static int NetCol        =13;

  public int numValues(){ return 14; }
  private static final String[ ] myText = TrueEnum.nameVector(UnsettledTransactionFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final UnsettledTransactionFormatEnum Prop=new UnsettledTransactionFormatEnum();//for accessing class info
  public UnsettledTransactionFormatEnum(){
    super();
  }
  public UnsettledTransactionFormatEnum(int rawValue){
    super(rawValue);
  }
  public UnsettledTransactionFormatEnum(String textValue){
    super(textValue);
  }
  public UnsettledTransactionFormatEnum(UnsettledTransactionFormatEnum rhs){
    this(rhs.Value());
  }
  public UnsettledTransactionFormatEnum setto(UnsettledTransactionFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static UnsettledTransactionFormatEnum CopyOf(UnsettledTransactionFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new UnsettledTransactionFormatEnum(rhs) : new UnsettledTransactionFormatEnum();
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

