// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/CSVTransactionFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class CSVTransactionFormatEnum extends TrueEnum {
  public final static int TimeCol       =0;
  public final static int AssociateCol  =1;
  public final static int MerchRefCol   =2;
  public final static int StanCol       =3;
  public final static int TraceCol      =4;
  public final static int StatusCol     =5;
  public final static int ApprovalCol   =6;
  public final static int PayTypeCol    =7;
  public final static int InstitutionCol=8;
  public final static int AcctNumCol    =9;
  public final static int SaleCol       =10;
  public final static int ReturnCol     =11;
  public final static int VoidChgCol    =12;
  public final static int NetCol        =13;

  public int numValues(){ return 14; }
  private static final String[ ] myText = TrueEnum.nameVector(CSVTransactionFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final CSVTransactionFormatEnum Prop=new CSVTransactionFormatEnum();//for accessing class info
  public CSVTransactionFormatEnum(){
    super();
  }
  public CSVTransactionFormatEnum(int rawValue){
    super(rawValue);
  }
  public CSVTransactionFormatEnum(String textValue){
    super(textValue);
  }
  public CSVTransactionFormatEnum(CSVTransactionFormatEnum rhs){
    this(rhs.Value());
  }
  public CSVTransactionFormatEnum setto(CSVTransactionFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static CSVTransactionFormatEnum CopyOf(CSVTransactionFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new CSVTransactionFormatEnum(rhs) : new CSVTransactionFormatEnum();
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

