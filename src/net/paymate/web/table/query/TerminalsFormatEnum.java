// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/TerminalsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.lang.TrueEnum;

public class TerminalsFormatEnum extends TrueEnum {
  public final static int TerminalNameCol =0;
  public final static int ModelCodeCol    =1;
  public final static int ApplianceCol    =2;
  public final static int LastCloseTimeCol=3;
  public final static int LastTxnTimeCol  =4;
  public final static int ApprCountCol    =5;
  public final static int ApprAmountCol   =6;
  public final static int CSVCol          =7;

  public int numValues(){ return 8; }
  private static final String[ ] myText = TrueEnum.nameVector(TerminalsFormatEnum.class);
  protected final String[ ] getMyText() {
    return myText;
  }
  public static final TerminalsFormatEnum Prop=new TerminalsFormatEnum();//for accessing class info
  public TerminalsFormatEnum(){
    super();
  }
  public TerminalsFormatEnum(int rawValue){
    super(rawValue);
  }
  public TerminalsFormatEnum(String textValue){
    super(textValue);
  }
  public TerminalsFormatEnum(TerminalsFormatEnum rhs){
    this(rhs.Value());
  }
  public TerminalsFormatEnum setto(TerminalsFormatEnum rhs){
    setto(rhs.Value());
    return this;
  }
  public static TerminalsFormatEnum CopyOf(TerminalsFormatEnum rhs){//null-safe cloner
    return (rhs!=null)? new TerminalsFormatEnum(rhs) : new TerminalsFormatEnum();
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

