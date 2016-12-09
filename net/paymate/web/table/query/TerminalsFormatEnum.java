// DO NOT EDIT!  MACHINE GENERATED FILE! [net/paymate/web/table/query/TerminalsFormatEnum.Enum]
package net.paymate.web.table.query;

import net.paymate.util.TrueEnum;
import net.paymate.util.TextList;

public class TerminalsFormatEnum extends TrueEnum {
  public final static int TerminalNameCol =0;
  public final static int ModelCodeCol    =1;
  public final static int LastCloseTimeCol=2;
  public final static int ApprCountCol    =3;
  public final static int ApprAmountCol   =4;

  public int numValues(){ return 5; }
  private static final TextList myText = TrueEnum.nameVector(TerminalsFormatEnum.class);
  protected final TextList getMyText() {
    return myText;
  }
  public static final TerminalsFormatEnum Prop=new TerminalsFormatEnum();
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

}
//$Id: makeenum.java,v 1.18 2001/07/19 01:06:45 mattm Exp $
